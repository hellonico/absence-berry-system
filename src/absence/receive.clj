(ns absence.receive
  (:require [clojure-mail.core :refer :all]
            [clojure-mail.events :as events]
            [absence.persistence :as p]
            [clojure.string :as str]
            [config.core :refer [env]]
            [absence.utils :as u]
            [absence.send :as s]
            [clojure-mail.message :refer (read-message)]))

(def gmail-store
  (store
    (-> env :store :imap)
    (-> env :store :user)
    (-> env :store :pwd)))

(def manager
  (atom nil))

(defn day-reason-times [ subject]
  (let [
    drt (str/split  subject #"," )
    ]
    (if (= 2 (count drt)) ;two-field-mode
    {:reason  (first drt)
     :date (u/today)
     :times (second drt)
    }
    {:reason (nth drt 2)
     :date (u/short-date-to-date (first drt))
     :times (second drt)
    }
    )))

(defn parse-msg[ msg ]
  (if (env :debug)
    (clojure.pprint/pprint msg))
  (let [ raw (select-keys msg [:date-sent :from :subject] )
         drt (day-reason-times (:subject msg))
         ]
  {
   :name (-> msg :from first :name)
   :timesent (u/fmt-utc-time (:date-sent msg))
   :email (-> msg :from first :address)
   :reason (:reason drt)
   :times  (:times drt)
   ; :date (u/today)
   :date (:date drt)
  }))

(defn insert-new-mails[e]
  (try
  (doall
  (->> e
    :messages
    (map read-message)
    (map parse-msg)
    (map p/insert-one)
    (map s/abs-ack-send)
    ))
    (catch Exception e (.printStackTrace e)))
  (println "> " (java.util.Date.)))

(defn mail-removed [e]
  (prn "removed" e))

(defn start-manager [store]
  (let [s (get-session "imaps")
        folder (open-folder store "inbox" :readonly)
        im (events/new-idle-manager s)]
    (events/add-message-count-listener
      insert-new-mails
      mail-removed
      folder
      im)
 (reset! manager im)))

(defn stop-manager []
  (events/stop manager)
  (reset! manager nil))

(defn -main [& args]
  (start-manager gmail-store)
  (println "Listener started...")
  )

(comment
    (stop-manager)

  (def my-inbox-messages
    (take 1 (all-messages gmail-store "inbox")))

  (def first-message
    (first my-inbox-messages))

  (p/insert-one (parse-msg (read-message first-message)))

  (clojure.pprint/pprint (read-message  first-message))
    ; (parse-msg (read-message first-message)))
  )
