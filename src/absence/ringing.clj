(ns absence.ringing
  (:gen-class)
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.util.codec]
   [absence.persistence :as p]
   [absence.receive :as r]
   [absence.utils :as u]
   [absence.exceling :as ex]
   [absence.ldap :as ldap]
   [config.core :refer [env]]
   [ring.util.response :as ring]
   [compojure.core :refer [GET POST defroutes]]
   [compojure.route :as route]
   [clostache.parser :as m]))

;
; ROUTES HELPERS
; 
(defn- handle-date [date]
  {:today date
   :today-str (u/date-to-dayoftheweek date)
   :daybefore (u/day-before date)
   :dayafter (u/day-after date)
   :fruits (p/get-fruits2 date)})

(defn- handle-email [email]
  {:fruits (p/get-fruits-by-email email)
   :month false 
  ;;  :email (str email " / " (.getMonth (java.time.YearMonth/now)))
   :email (str email)
   })

(defn- list-of-days[ymmonth]
  (map
   #(hash-map :l (.getDayOfMonth %) :klass (u/get-klass %))
   (u/month-range-as-localdates ymmonth)))

(defn- handle-month [users ymmonth]
  {:month (.getMonth ymmonth)
   :next (.plusMonths ymmonth 1)
   :prev (.minusMonths ymmonth 1)
   :users users
   :days (list-of-days ymmonth)})

(defn- handle-home []
  {:email (-> env :store :user)
   :today (u/today)
   :tomorrow (u/tomorrow)
   :yesterday (u/yesterday)
   :links (-> env :front :links)})

(defn- handle-excel []
  {:status 200
   :headers {"Content-Type" "application/vnd.ms-excel"}
   :filename "abs.xlsx"
   :body (ex/get-excel)})

(defn render-html [_template _map]
  (m/render-resource
   (str _template ".mustache")
   _map
   {:header (m/render-resource "_header.mustache")
    :footer (m/render-resource "_footer.mustache")}))

;
; ROUTES
;
(defn process-one-entry[_name _email _dates _reason]
  (let [msg {:from [{:name _name :address _email}]
             :subject (str _dates ",," _reason)
             :date-sent (java.util.Date.)}
        entry (r/parse-msg msg)]
    (p/insert-one entry)
    entry))

(defroutes handler
  (GET "/delete/:id" [id]
    (prn "delete " id)
    (p/delete-by-id id)
    (ring/redirect "/holidays/now"))

  (GET "/holidays/:month" [month]
    (let [ymmonth (u/to-yearmonth month)
          users
          (->> (ldap/get-users)
               (map #(clojure.set/rename-keys % {:mail :email}))
               (map #(merge % (p/query-holidays ymmonth (:email %)))))]

      (render-html "holidays"
                   (handle-month users ymmonth))))

  (POST "/form/post" {raw :body}
    (let [body  (ring.util.codec/form-decode (slurp raw))
          entry (process-one-entry (body "name") (body "email") (body "dates") (body "reason"))]
      (render-html "holiday" entry)))

  (GET "/excel/abs.xlsx"  []
    (handle-excel))

  (GET "/email/:email" [email]
      (render-html
       "fruitsbyemail"
       (handle-email email)))
  
  (GET "/month" []
      (render-html 
       "fruitsbyemail"
       {:month true 
        :email (.getMonth (java.time.YearMonth/now)) 
        :fruits (p/get-fruits-by-month)}))

  (GET "/" []
    (render-html 
     "index" 
     (handle-home)))

  (GET "/abs" []
    (render-html "fruits" (handle-date (u/today))))
  (GET "/abs/:date" [date]
    (render-html "fruits" (handle-date date)))
  
  ; fetch request
  (GET "/hello/:user/:email/:month/:d1/:d2" [user email month d1 d2]
    (let [_month (format "%02d" (.getValue (java.time.Month/valueOf month))) 
          dates (str _month d1 "-" _month d2)
          entry (process-one-entry user email dates "Scheduled Vacation")]
      (println entry)
      ;(process-one-entry entry
      entry))
  
  (GET "/debug/:date" [date]
    {:body
     (apply
      str
      {:data (p/get-fruits2 date) 
       :config env})})

  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

(defn init []
  (println "Starting..." (u/now)))

(defn -main
  [& args]
  (jetty/run-jetty handler {:port 3000}))