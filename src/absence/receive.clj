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

(defn day-reason-times [ msg ]
  (let [
    subject (:subject msg)
    drt (str/split  subject #"," )
    date-sent (u/fmt (:date-sent msg))
    date-field (first drt)
    date-split (str/split  date-field #"-")
    is-holiday (> (count date-split) 1)
    ]
    (if (= 2 (count drt)) ;two-field-mode
    {:date date-sent
     :times (first drt)
     :reason  (second drt)
    }
    (if is-holiday
    {
     :times "ALL DAY"
     :reason (nth drt 2)
     :holidaystart (u/short-date-to-date (first date-split))
     :holidayend (u/short-date-to-date (second date-split))
    }
    {:date (u/short-date-to-date date-field)
     :times (second drt)
     :reason (nth drt 2)
    })
    )))

(defrecord entry
  [name timesent email reason times date])

(defn notification [msg drt]
  (map->entry
   {
   :name (-> msg :from first :name)
   :timesent (u/fmt-date-time (:date-sent msg))
   :email (-> msg :from first :address)
   :reason (:reason drt)
   :times  (:times drt)
   :date (:date drt)
   :holidaystart (:holidaystart drt)
   :holidayend (:holidayend drt)
   })
  )

(defn parse-msg[ msg ]
  (let [ ;raw (select-keys msg [:date-sent :from :subject] )
         drt (day-reason-times msg)
         ]
  (notification msg drt)
  ))

(defn dump-raw-msg [msg]
  (println "< " (u/now) "\t" (-> msg :from first :address))
  ; (u/write-msg-to-file
  ;   (str
  ;     (-> env :inbox)
  ;     (System/currentTimeMillis)
  ;     " < "
  ;     (-> msg :from first :address))
  ;   msg)
  msg)

(defn insert-new-mails[e]
  (try
  (doall
  (->> e
    :messages
    (map read-message)
    (map dump-raw-msg)
    (map parse-msg)
    (map p/insert-one)
    (map s/abs-ack-send)
    ))
    (catch Exception e (.printStackTrace e))))

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
  (events/stop @manager)
  (reset! manager nil))


(defn -main [& args]
  (start-manager gmail-store)
  (println "Listener started..." (-> env :user-timezone)))

(comment
    (stop-manager)

    (recover)

  (def my-inbox-messages
    (take 1 (all-messages gmail-store "inbox")))

  (def results
   (search-inbox gmail-store [:received-on "2018-04-03"]))
  (count results)

  (def first-message
    (first my-inbox-messages))

  (p/insert-one (parse-msg (read-message first-message)))

  (clojure.pprint/pprint (read-message  first-message))
    ; (parse-msg (read-message first-message)))
  )
