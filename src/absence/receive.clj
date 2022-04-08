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

(def fullday 
  (set ["fullday" "allday"]))

(defn inner-drt[date-sent subject]
    (let [
    drt (str/split  subject #"," )
    date-field (first drt)
    date-split (str/split  date-field #"-")
    ]
    (if (= 2 (count drt)) ;two-field-mode
    {:date date-sent :times (first drt) :reason  (second drt)}
      
    (let [
          times (nth drt 1) 
          _times (if (str/blank? times) "all day" times) ; times are empty
          __times (str/replace (str/lower-case _times) #"\s" "") 
          is-fullday (or (> (count date-split) 1)  (contains? fullday __times))

          reason (nth drt 2)

          d1 (first date-split) 
          d2 (if (< 1 (count date-split)) (second date-split) d1)

          ]
      ; (println subject ":" is-fullday ">" reason ">" _times)
    ; 3 fields mode
    (if is-fullday
    {
     :times _times
     :reason reason
     :holidaystart (u/short-date-to-date d1)
     :holidayend (u/short-date-to-date d2)
    }
    {:date (u/short-date-to-date date-field)
     :times times
     :reason reason
    }
      )))))

(defn day-reason-times [ msg ]
  (inner-drt (u/fmt (:date-sent msg)) (:subject msg) ))

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
  ;raw (select-keys msg [:date-sent :from :subject] )
  (let [ drt (day-reason-times msg) ]
  (notification msg drt)))

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