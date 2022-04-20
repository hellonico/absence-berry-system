(ns absence.recover
  (:require [clojure-mail.core :refer :all]
            [absence.notification :as r]
            [config.core :refer [env]]
            [absence.persistence :as p]
            [clojure-mail.message :refer (read-message)]))

(defn for-date [gmail-store date]
  (let[
    ;my-messages (all-messages absence.receive/gmail-store "inbox")
    my-messages
     (search-inbox gmail-store [:received-on date])
    ]
    (doseq [m my-messages]
    (try
    (let [msg (r/parse-msg (read-message m))]
    (println "recovering")
    (clojure.pprint/pprint msg)
    (doall (p/insert-one msg)))
      (catch Exception e
      (println e))))))

(defn -main[ & args]
  (let [gmail-store
        (store
          (-> env :store :imap)
          (-> env :store :user)
          (-> env :store :pwd))
        date (or (first args) :today )]
  (println "Recovering for " date)
  (for-date date)))
