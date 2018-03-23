(ns absence.utils)

(defn fmt [java-date]
    (.format
    (java.text.SimpleDateFormat. "yyyy-MM-dd")
    java-date))

(defn short-date-to-date [ short-date]
     (let[
        cal (java.util.Calendar/getInstance)
        day  (Integer/parseInt (apply str (drop 2 short-date)))
        month (dec (Integer/parseInt (apply str (take 2 short-date))) )
        ]
        (.set cal java.util.Calendar/DAY_OF_MONTH day)
        (.set cal java.util.Calendar/MONTH month)
        (fmt (.getTime cal))))

(defn fmt-time [java-date]
    (.format
    (java.text.SimpleDateFormat. "HH:mm")
    java-date))

(defn fmt-utc-time [java-date]
    (let [ cal (java.util.Calendar/getInstance)]
        (.setTime cal java-date)
        (.add cal java.util.Calendar/HOUR_OF_DAY 9)
        (fmt-time (.getTime cal))))

(defn yesterday[]
    (let[ cal (java.util.Calendar/getInstance)]
        (.add cal java.util.Calendar/DATE -1)
        (fmt (.getTime cal))))

(defn today[]
   (fmt
    (java.util.Date.)))

(defn tomorrow[]
    (let[ cal (java.util.Calendar/getInstance)]
        (.add cal java.util.Calendar/DATE 1)
        (fmt (.getTime cal))))
