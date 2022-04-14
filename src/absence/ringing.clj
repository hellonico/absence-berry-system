(ns absence.ringing
  (:gen-class)
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.util.codec]
   [absence.persistence :as p]
   [absence.utils :as u]
   [absence.routehelpers :as h]
   [absence.ldap :as ldap]
   [clojure.set :as set]
   [config.core :refer [env]]
   [ring.util.response :as ring]
   [compojure.core :refer [GET POST defroutes]]
   [compojure.route :as route]))


(defroutes handler
  (GET "/delete/:id" [id]
    (prn "delete " id)
    (p/delete-by-id id)
    (ring/redirect "/holidays/now"))

  (GET "/holidays/:month" [month]
    (let [ymmonth (u/to-yearmonth month)
          users
          (->> (ldap/get-users)
               (map #(set/rename-keys % {:mail :email}))
               (map #(merge % (p/query-holidays ymmonth (:email %)))))]

      (h/render-html "holidays"
                   (h/handle-month users ymmonth))))

  (POST "/form/post" {raw :body}
    (let [body  (ring.util.codec/form-decode (slurp raw))
          entry (h/process-one-entry (body "name") (body "email") (body "dates") (body "reason"))]
      (h/render-html "holiday" entry)))

  (GET "/excel/abs.xlsx"  []
    (h/handle-excel))

  (GET "/email/:email" [email]
      (h/render-html
       "fruitsbyemail"
       (h/handle-email email)))
  
  (GET "/month" []
      (h/render-html 
       "fruitsbyemail"
       {:month true 
        :email (.getMonth (java.time.YearMonth/now)) 
        :fruits (p/get-fruits-by-month)}))

  (GET "/" []
    (h/render-html 
     "index" 
     (h/handle-home)))

  (GET "/abs" []
    (h/render-html "fruits" (h/handle-date (u/today))))
  (GET "/abs/:date" [date]
    (h/render-html "fruits" (h/handle-date date)))
  
  ; js fetch request
  (GET "/hello/:user/:email/:month/:d1/:d2" [user email month d1 d2]
    (let [_month (format "%02d" (.getValue (java.time.Month/valueOf month))) 
          dates (str _month d1 "-" _month d2)
          entry (h/process-one-entry user email dates "Scheduled Vacation")]
      (println entry)
      entry))
  
  ; debug
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