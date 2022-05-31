(ns absence.routehelpers
  (:require
    [ring.util.codec]
    [absence.send :as send]
    [absence.persistence :as p]
    [absence.ldap :as ldap]
    [absence.calendars :as cals]
    [absence.notification :as n]
    [absence.utils :as u]
    [clojure.java.shell :as sh]
    [absence.exceling :as ex]
    [config.core :refer [env]]
    [clostache.parser :as m]
    [clojure.string :as string]))

(defn render-html
  "render with header and footer"
  [_template _map]
  (m/render-resource
    (str _template ".mustache")
    _map
    {:header      (m/render-resource "_header.mustache")
     :smallfruits (m/render-resource "_smallfruits.mustache" _map)
     :submenu     (m/render-resource "_submenu.mustache" _map)
     :footer      (m/render-resource "_footer.mustache" {:git (:out (sh/sh "bash" "-c" "git log | head -n 3"))})}))

(defn handle-date [date]
  (merge
    {:today     date
     :today-str (u/date-to-dayoftheweek date)
     :daybefore (u/day-before date)
     :dayafter  (u/day-after date)}
    (p/get-fruits2 date)))

(defn handle-email [email]
  {:fruits (p/get-fruits-by-email email)
   :month  false
   :email  (str email)})

(defn handle-month [users ymmonth]
  {:month     (.getMonth ymmonth)
   :next      (.plusMonths ymmonth 1)
   :prev      (.minusMonths ymmonth 1)
   :users     users
   :calendars (cals/make-calendars ymmonth)
   }
  )

(defn handle-home []
  {:email     (-> env :store :user)
   :today     (u/today)
   :tomorrow  (u/tomorrow)
   :yesterday (u/yesterday)
   :links     (-> env :front :links)})

(defn handle-excel []
  {:status   200
   :headers  {"Content-Type" "application/vnd.ms-excel"}
   :filename "abs.xlsx"
   :body     (ex/get-excel)})

(defn handle-users [template]
  (let [users
        (->> (ldap/get-users)
             ;(map #(set/rename-keys % {:mail :email :displayName :name}))
             ; to move to ldap ?
             (map #(select-keys % [:email :name]))
             (map #(p/last-for-email %))
             ;(map #(p/last-for-email %))
             (map #(merge {:late (nil? (% :holidaystart))} %))
             )]
    (render-html template {:users users})))

(defn process-one-entry
  ([line]
   (let [[_name _email _dates _reason _times] (string/split line #",")]
     (process-one-entry _name _email _dates _reason _times)))
  ([_name _email _dates _reason _times]
   (let [msg {:from      [{:name _name :address _email}]
              :subject   (str _dates "," _times "," _reason)
              :date-sent (java.util.Date.)}
         entry (n/parse-msg msg)]
     (->>
       entry
       send/dump-before-send
       p/insert-one
       send/abs-ack-send))))

(defn upload [request]
  (println (:params request))
  (let [tmpfilepath (:path (bean (get-in request [:params "file" :tempfile])))]
    (println "reading:" tmpfilepath)
    (with-open [rdr (clojure.java.io/reader tmpfilepath)]
      (doall (map #(try (process-one-entry %) (catch Exception e (println e))) (line-seq rdr))))))
