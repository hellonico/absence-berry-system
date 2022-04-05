(ns absence.utils
    (:import 
        [java.time LocalDate]
        [java.time.format DateTimeFormatter]
        [java.io File FileWriter])
    (:require [clojure.pprint]))

(defn fmt [java-date]
    (.format
    (java.text.SimpleDateFormat. "yyyy-MM-dd")
    java-date))

(defn short-date-to-date [ short-date ]
     (let[
        cal (java.util.Calendar/getInstance)
        day  (Integer/parseInt (apply str (drop 2 short-date)))
        month (dec (Integer/parseInt (apply str (take 2 short-date))) )
        ]
        (.set cal java.util.Calendar/DAY_OF_MONTH day)
        (.set cal java.util.Calendar/MONTH month)
        (fmt (.getTime cal))))

(defn string-date-to-cal [short-date]
    (let[
        cal (java.util.Calendar/getInstance)]
    (.setTime cal
    (.parse
        (java.text.SimpleDateFormat. "yyyy-MM-dd")
        short-date))
        cal))

(defn date-to-dayoftheweek [date]
    (-> date
    (LocalDate/parse (DateTimeFormatter/ofPattern "yyyy-MM-dd"))
    (.getDayOfWeek)
    (str) 
    (clojure.string/lower-case)))

(defn fmt-date-time [java-date]
    (.format
    (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm")
    java-date))

(defn now []
    (fmt-date-time (java.util.Date.)))

(defn day-before [ string-date ] 
    (let [cal (string-date-to-cal string-date)]
    (.add cal java.util.Calendar/DATE -1)
    (fmt (.getTime cal))))
(defn day-after [string-date]
    (let [cal (string-date-to-cal string-date)]
    (.add cal java.util.Calendar/DATE 1)
    (fmt (.getTime cal))))

(defn first-day-of-month []
   (.with (java.time.LocalDate/now) (java.time.temporal.TemporalAdjusters/firstDayOfMonth)))

(defn last-day-of-month []
  (.with (java.time.LocalDate/now) (java.time.temporal.TemporalAdjusters/lastDayOfMonth)))

(defn month-day-range[m] ; "2022-03"
  (let [
    ; m (java.time.YearMonth/parse yearMonth)
        one  1
        _last (inc (.getDayOfMonth (.atEndOfMonth m)))]
(range one _last)))

(defn today[]
   (fmt
    (java.util.Date.)))

(defn yesterday[]
    (day-before (today)))
        
(defn tomorrow[]
    (day-after (today)))

(defn write-msg-to-file [ file-name msg ]
  (with-open [w (FileWriter. (File. file-name))]
    (binding [*out* w *print-dup* true] 
        (clojure.pprint/pprint msg))))