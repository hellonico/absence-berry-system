(ns absence.notification
  (:require
   [clojure.string :as str]
   [absence.utils :as u]))

(def fullday
  (set ["fullday" "allday"]))

(defn inner-drt [date-sent subject]
  (let [drt (str/split  subject #",")
        date-field (first drt)
        date-split (str/split  date-field #"-")]
    (if (= 2 (count drt)) ;two-field-mode
      {:date date-sent :times (first drt) :reason  (second drt)}

      (let [times (nth drt 1)
            _times (if (str/blank? times) "all day" times) ; times are empty
            __times (str/replace (str/lower-case _times) #"\s" "")
            is-fullday (or (> (count date-split) 1)  (contains? fullday __times))

            reason (nth drt 2)

            d1 (first date-split)
            d2 (if (< 1 (count date-split)) (second date-split) d1)]
      ; (println subject ":" is-fullday ">" reason ">" _times)
    ; 3 fields mode
        (if is-fullday
          {:times _times
           :reason reason
           :holidaystart (u/short-date-to-date d1)
           :holidayend (u/short-date-to-date d2)}
          {:date (u/short-date-to-date date-field)
           :times times
           :reason reason})))))

(defn day-reason-times [msg]
  (inner-drt (u/fmt (:date-sent msg)) (:subject msg)))

(defrecord entry
           [name timesent email reason times date telework holidaystart holidayend])

(defn notification [msg drt]
  (map->entry
   {:name (-> msg :from first :name)
    :timesent (u/fmt-date-time (:date-sent msg))
    :email (-> msg :from first :address)
    :reason (:reason drt)
    :times  (:times drt)
    :date (:date drt)
    :holidaystart (:holidaystart drt)
    :holidayend (:holidayend drt)
    :telework (= (str/lower-case (:reason drt)) "telework")}))

(defn parse-msg [msg]
  (let [drt (day-reason-times msg)]
    (notification msg drt)))
