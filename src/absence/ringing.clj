(ns absence.ringing
    (:require
     [absence.persistence :as p]
     [absence.receive :as r]
     [absence.utils :as u]
     [absence.exceling :as ex]
     [clojure.java.shell :as sh]
     [config.core :refer [env]]
     [ring.util.response :as ring]
     [compojure.core :refer [GET POST defroutes]]
     [compojure.route :as route]
     [clostache.parser :as m]))

(defn- expected-connected-ips []
    (drop 1
        (clojure.string/split
            (:out (sh/sh "bash" "-c" (str "dig +short " (-> env :store :imap)) )) #"\n")))

(defn is-listening [ip]
    (let [
        command (sh/sh "bash" "-c" (str "netstat -an | grep '" ip "'"))
        netstat (:out command)]
    (< 1 (count (clojure.string/split netstat #"\n")))
    ))

(defn is-imap-listening[]
    (let [
        status (map is-listening (expected-connected-ips))
        c (count (filter true? status))
        result (<= 1 c)
        ]
        (println status)
        (println c)
        (println result)
        result
    ))

(defn handle-date [date]
        {:today date
         :today-str (u/date-to-dayoftheweek date)
         :daybefore (u/day-before date)
         :dayafter (u/day-after date)
         :fruits (p/get-fruits2 date)})

(defn handle-email [email]
        {:fruits (p/get-fruits-by-email email)})

(defn render-html[_template _map] 
  (m/render-resource 
   (str _template ".mustache")
   _map
   {:header (m/render-resource "_header.mustache")
    :footer (m/render-resource "_footer.mustache")
    }
   ))

(defroutes handler
    (GET "/delete/:id" [id]
      (prn "delete " id)
      (p/delete-by-id id)
      (ring/redirect "/month"))
  
    (POST "/form/post" {raw :body}
        (let [body  (ring.util.codec/form-decode (slurp raw))
              msg {:from [{:name (body "name") :address (body "email")}] :subject (str (body "dates") ",," (body "reason")) :date-sent (java.util.Date.)}
              entry (r/parse-msg msg)]
          (prn  entry)
          (p/insert-one entry)
          (render-html "holiday" entry)))
  
    (GET "/excel/abs.xlsx"  []
       {:status 200
        :headers {"Content-Type" "application/vnd.ms-excel"}
        :filename "abs.xlsx"
        :body (ex/get-excel)})
  
    (GET "/email/:email" [email]
    (let [fruits (handle-email email)]
    ;(prn fruits)
        (render-html "fruitsbyemail"
                           (merge fruits {:today (u/today) :email email}))))
    (GET "/month" []
      (let [fruits (p/get-fruits-by-month)]
    ;(prn fruits)
        (render-html "fruitsbyemail"
            {:today (u/today) :email (u/today) :fruits fruits}
             )))
    ;; (GET "/net" []
    ;;     (if (is-imap-listening)
    ;;         {:status 200}
    ;;         {:status 404}))
    (GET "/" []
        (render-html "index"
            {:email (-> env :store :user)
             :today (u/today)
             :tomorrow (u/tomorrow)
             :yesterday (u/yesterday)
             :links (-> env :front :links)}))
    
    (GET "/abs" []
        (render-html "fruits" (handle-date (u/today))))

    (GET "/debug/:date" [date]
     {:body
      (apply 
        str 
        {:data (p/get-fruits2 date) :config env}
        )})
    
    (GET "/abs/:date" [date]
        (render-html "fruits" (handle-date date)))
    
     (route/resources "/")
     (route/not-found "<h1>Page not found</h1>"))

(defn init[]
  (println "Starting..." (u/now)))

