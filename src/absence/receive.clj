(ns absence.receive
  (:require [clojure-mail.core :as mail]
            [clojure-mail.events :as events]
            [absence.persistence :as p]
            [config.core :refer [env]]
            [absence.utils :as u]
            [absence.notification :as n]
            [absence.send :as s]
            [clojure-mail.message :refer (read-message)]))

(def manager
  (atom nil))

(defn- dump-raw-msg [msg]
  (println "< " (u/now) "\t" (-> msg :from first :address))
  msg)

(defn- mail-removed [e]
  (prn "removed" e))

(defn- insert-new-mails[e]
  (try
  (doall
  (->> e
    :messages
    (map read-message)
    (map dump-raw-msg)
    (map n/parse-msg)
    (map p/insert-one)
    (map s/abs-ack-send)))
    (catch Exception e (.printStackTrace e))))


(defn start-manager [store]
  (let [s (mail/get-session "imaps")
        folder (mail/open-folder store "inbox" :readonly)
        im (events/new-idle-manager s)]
    (events/add-message-count-listener
      insert-new-mails
      mail-removed
      folder
      im)
 (reset! manager im)))

(defn stop-manager []
  (events/stop @manager)
  (reset! manager nil))

(defn -main [& args]
  (let [gmail-store
          (mail/store
           (-> env :store :imap)
           (-> env :store :user)
           (-> env :store :pwd))]
  (start-manager gmail-store)
  (println "Listener started..." (-> env :user-timezone))))