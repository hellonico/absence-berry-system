(ns absence.ringing
    (:require
        [absence.persistence :as p]
        [absence.utils :as u]
        [clojure.java.shell :as sh]
        [config.core :refer [env]]
        [compojure.core :refer :all]
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

(defroutes handler
    (GET "/net" []
        (if (is-imap-listening)
            {:status 200}
            {:status 404}))
    (GET "/" []
        (m/render-resource "index.mustache"
            {:email (-> env :store :user)
             :today (u/today)
             :tomorrow (u/tomorrow)
             :yesterday (u/yesterday)
             :links (-> env :front :links)}))
    (GET "/abs" []
        (m/render-resource "fruits.mustache"
            (handle-date (u/today))))
    (GET "/debug/:date" [date]
     {:body
      (apply 
        str 
        {:data (p/get-fruits2 date) :config env}
        )})
    (GET "/abs/:date" [date]
        (m/render-resource "fruits.mustache" (handle-date date)))
     (route/resources "/")
     (route/not-found "<h1>Page not found</h1>"))
