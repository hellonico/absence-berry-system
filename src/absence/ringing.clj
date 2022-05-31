(ns absence.ringing
  (:gen-class)
  (:require
    [ring.adapter.jetty :as jetty]
    [ring.util.codec]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]
    [absence.persistence :as p]
    [absence.utils :as u]
    [absence.routehelpers :as h]
    [absence.middleware :as m]
    [ring.logger :as logger]
    [absence.ldap :as ldap]
    [clojure.set :as set]
    [config.core :refer [env]]
    [sentry-clj.ring :refer [wrap-report-exceptions wrap-sentry-tracing]]
    [clojure.data.json :as json]
    [sentry-clj.core :as sentry]
    [ring.util.response :as ring]
    [compojure.core :refer [GET POST context routes defroutes]]
    ; [compojure.handler :as handler]
    [compojure.route :as route])
  (:import (java.time Month)))


(defroutes base-routes
           (GET "/delete/:id" [id]
             (prn "delete " id)
             (p/delete-by-id id)
             (ring/redirect "/holidays/now"))

           (GET "/faces2" []
             (h/handle-users "faces2"))

           (GET "/faces" []
                (h/handle-users "faces"))

           (GET "/users" []
             (h/handle-users "users"))

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
             (println "fetch:" reason user email month d1 d2 times)
             (let [_month (format "%02d" (.getValue (Month/valueOf month)))
                   dates (if (= d1 d2) (str _month d1) (str _month d1 "-" _month d2))
                   entry (h/process-one-entry user email dates reason times)]
               (println entry)
               entry))

           ;(GET "/picture/:user.jpg" [user]
           (GET "/picture/:user" [user]
             (try
               (ring.util.response/file-response
                 (ldap/get-user-pic user))
               (catch Exception e
                 (ring.util.response/file-response
                   "resources/public/images/default_pic.jpg"))))

           (GET "/last/:n" [n]
             (let [_n (try (Integer/parseInt n) (catch Exception e 10))
                   fruits (p/get-last-fruits _n)]
               (h/render-html
                 "fruitsbyemail"
                 {:month true
                  :email (str "all, last " _n " entries")
                  :fruits
                  (map #(merge {:late (nil? (% :holidaystart))} %) fruits)}
                 )))

           (POST "/uploadHTML" [:as request]

             )

           (POST "/upload" [:as request]
             ; https://github.com/ring-clojure/ring/wiki/File-Uploads
             ; https://medium.com/@dashora.rajnish/how-to-create-apis-in-clojure-supporting-file-upload-and-data-transformation-using-ring-and-ad40fc3ca2d0
             (println (:params request))

             (let [tmpfilepath (:path (bean (get-in request [:params "file" :tempfile])))
                   nb (atom 0)]

               (println "reading:" tmpfilepath)
               (with-open [rdr (clojure.java.io/reader tmpfilepath)]
                 (doseq [line (line-seq rdr)]
                   (println line)
                   (try (h/process-one-entry line) (catch Exception e (println e)))
                   (swap! nb inc)))
               (str "Processed: " @nb)))

           ; http://localhost:3000/json/teleworktoday/cbuckley@royalnavy.mod.uk
           (GET "/json/teleworktoday/:email" [email]
             {:body (json/write-str {:email email :telework (p/telework-today email)})})


           (route/resources "/")
           (route/not-found "<h1>Page not found</h1>"))

(defroutes debug-routes
           (context "/debug" []

             (GET "/refresh-config" []
               (println "config refreshed")
               (config.core/reload-env)
               "config refreshed")

             (GET "/date/:date" [date]
               {:body
                (apply
                  str
                  {:data   (p/get-fruits date)
                   :config env})})
             ;
             ;(GET "/hello" []
             ;  "hello")
             ;

             (GET "/users" []
               {:body
                (ldap/get-users)})
             (GET "/error" []
               (throw (Exception. "Hello")))))

(def my-routes
    (if (-> env :debug)
      (routes debug-routes base-routes)
      base-routes))

(def handler
  (-> my-routes
      (wrap-multipart-params)
      (m/wrap-nocache)
      (m/wrap-nocache)
      ;(wrap-sentry-tracing)
      ;(wrap-report-exceptions {})
      (logger/wrap-with-logger)
      (logger/wrap-log-request-start)))

(defn init []
  (sentry/init! (-> env :sentry :project) (-> env :sentry :options))
  (println "Debug mode is on:" (-> env :debug))
  (println "Starting..." (u/now) " on port: " (-> env :server :port)))

(defn -main
  [& args]
  (init)
  (jetty/run-jetty handler {:port (-> env :server :port)}))