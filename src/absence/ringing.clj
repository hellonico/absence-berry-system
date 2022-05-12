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
    [sentry-clj.ring :refer [wrap-report-exceptions wrap-sentry-tracing]]
    [clojure.data.json :as json]
    [sentry-clj.core :as sentry]
    [ring.util.response :as ring]
    [compojure.core :refer [GET POST defroutes]]
    ; [compojure.handler :as handler]
    [compojure.route :as route])
  (:import (java.time Month)))

(defroutes my-routes
           (GET "/delete/:id" [id]
             (prn "delete " id)
             (p/delete-by-id id)
             (ring/redirect "/holidays/now"))

           (GET "/users" []
             (let [users
                   (->> (ldap/get-users)
                        ;(map #(set/rename-keys % {:mail :email :displayName :name}))
                        ; to move to ldap ?
                        (map #(select-keys % [:email :name]))
                        (map #(p/last-for-email %))
                        (map #(p/last-for-email %))
                        (map #(merge {:late (nil? (% :holidaystart))} %))
                        )]
               (h/render-html "users" {:users users})))

           (GET "/holidays/:month" [month]
             (let [ymmonth (u/to-yearmonth month)
                   users
                   (->> (ldap/get-users)
                        (map #(set/rename-keys % {:mail :email}))
                        (pmap #(merge % (p/query-holidays ymmonth (:email %))))
                        (map #(merge {:late (nil? (% :holidaystart))} %))
                        )]

               (h/render-html "holidays"
                              (h/handle-month users ymmonth))))

           (POST "/form/post" {raw :body}
             (let [body (ring.util.codec/form-decode (slurp raw))
                   entry (h/process-one-entry (body "name") (body "email") (body "dates") (body "reason") (body "times"))]
               (h/render-html "new" entry)))

           (GET "/excel/abs.xlsx" []
             (h/handle-excel))

           (GET "/email/:email" [email]
             (h/render-html
               "fruitsbyemail"
               (h/handle-email email)))

           (GET "/month" []
             (h/render-html
               "fruitsbyemail"
               {:month  true
                :email  (u/current-month)
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
           (GET "/hello/:reason/:user/:email/:month/:d1/:d2/:times" [reason user email month d1 d2 times]
             (let [_month (format "%02d" (.getValue (Month/valueOf month)))
                   dates (if (= d1 d2) (str _month d1) (str _month d1 "-" _month d2))
                   entry (h/process-one-entry user email dates reason times)]
               (println entry)
               entry))

           ;(GET "/picture/:user.jpg" [user]
           (GET "/picture/:user" [user]
               (ring.util.response/file-response
                 (ldap/get-user-pic user)))

           (GET "/last/:n" [n]
             (let [_n (try (Integer/parseInt n) (catch Exception e 10))
                   fruits (p/get-last-fruits _n)]
               (h/render-html
                 "fruitsbyemail"
                 {:month  true
                  :email  (str "all, last " _n " entries")
                  :fruits
                  (map #(merge {:late (nil? (% :holidaystart))} %) fruits)}
               )))

           ; http://localhost:3000/json/teleworktoday/cbuckley@royalnavy.mod.uk
           (GET "/json/teleworktoday/:email" [email]
             {:body (json/write-str {:email email :telework (p/telework-today email)})})

           ; debug routes in debug mode
           (if (-> env :debug)
           (GET "/debug/date/:date" [date]
             {:body
              (apply
                str
                {:data   (p/get-fruits date)
                 :config env})}))

           (if (-> env :debug)
           (GET "/debug/users" []
             {:body
              (ldap/get-users)}))

           (if (-> env :debug)
             (GET "/error" []
               (throw (Exception. "Hello"))
               ))

           (route/resources "/")
           (route/not-found "<h1>Page not found</h1>"))
;
;(defn wrap-fallback-exception
;  [handler]
;  (fn [request]
;    (try
;      (handler request)
;      (catch Exception e
;        (sentry/send-event
;          {:message (.getMessage e)
;           :throwable e})
;        {:status 500 :body "Something isn't quite right..."}))))

(def handler
  (-> my-routes
      wrap-sentry-tracing
      (wrap-report-exceptions {})
      ))

(defn wrap-fallback-exception
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (sentry/send-event
          {:message (.getMessage e)
           :throwable e})
        {:status 500 :body "Something isn't quite right..."}))))

(def handler
  (-> my-routes wrap-fallback-exception))

(defn init []
  (sentry/init! (-> env :sentry :project) (-> env :sentry :options))

  (println "Starting..." (u/now) " on port: " (-> env :server :port)))

(defn -main
  [& args]
  (init)
  (jetty/run-jetty handler {:port (-> env :server :port)}))