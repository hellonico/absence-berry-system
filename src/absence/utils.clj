(ns absence.utils
  (:import
    [java.time LocalDate YearMonth]
    [java.time.format DateTimeFormatter]
    [com.github.holidayjp.jdk8 HolidayJp]
    [java.io File FileWriter]
    (java.text SimpleDateFormat)
    (java.util Calendar Date)
    (java.time.temporal TemporalAdjusters))
    (:require [clojure.pprint]))

(defn fmt [java-date]
    (.format
    (SimpleDateFormat. "yyyy-MM-dd")
    java-date))

(defn short-date-to-date [ short-date ]
     (let[
        cal (Calendar/getInstance)
        day  (Integer/parseInt (apply str (drop 2 short-date)))
        month (dec (Integer/parseInt (apply str (take 2 short-date))) )
        ]
        (.set cal Calendar/DAY_OF_MONTH day)
        (.set cal Calendar/MONTH month)
        (fmt (.getTime cal))))

(def date-format "yyyy-MM-dd")

(defn string-date-to-cal [short-date]
    (let[
        cal (Calendar/getInstance)]
    (.setTime cal
    (.parse
        (SimpleDateFormat.  date-format)
        short-date))
        cal))

(defn date-to-dayoftheweek [date]
    (-> date
    (LocalDate/parse (DateTimeFormatter/ofPattern date-format))
    (.getDayOfWeek)
    (str) 
    (clojure.string/lower-case)))

(defn fmt-date-time [java-date]
    (.format
    (SimpleDateFormat. "yyyy-MM-dd HH:mm")
    java-date))

(defn now []
    (fmt-date-time (Date.)))

(defn day-before [ string-date ] 
    (let [cal (string-date-to-cal string-date)]
    (.add cal Calendar/DATE -1)
    (fmt (.getTime cal))))

(defn day-after [string-date]
    (let [cal (string-date-to-cal string-date)]
    (.add cal Calendar/DATE 1)
    (fmt (.getTime cal))))

(defn first-day-of-month []
   (.with (LocalDate/now) (TemporalAdjusters/firstDayOfMonth)))

(defn last-day-of-month []
  (.with (LocalDate/now) (TemporalAdjusters/lastDayOfMonth)))

(defn month-day-range [ym] 
  (range 1 (inc (.getDayOfMonth (.atEndOfMonth ym)))))

(defn today[]
   (fmt
     (Date.)))

(defn yesterday[]
    (day-before (today)))
        
(defn tomorrow[]
    (day-after (today)))

(defn write-msg-to-file [ file-name msg ]
  (with-open [w (FileWriter. (File. file-name))]
    (binding [*out* w *print-dup* true] 
        (clojure.pprint/pprint msg))))

(defn to-yearmonth [date-as-string]
  (try (YearMonth/parse date-as-string) (catch Exception _ (YearMonth/now))))

(defn to-local
  "convert string to local date"
  [d]
  (LocalDate/parse d (DateTimeFormatter/ofPattern  date-format )))

(defn month-range-as-localdates [ym]
  (map #(.atDay ym %) (month-day-range ym)))

(defn is-holiday [ldate]
  (or
    (HolidayJp/isHoliday ^LocalDate ldate)
    (= 6 (.getValue (.getDayOfWeek ldate)))
    (= 7 (.getValue (.getDayOfWeek ldate)))))

(defn get-klass 
  "get a css class depending on day of week"
  [ldate]
  (cond
    (= ldate (LocalDate/now)) "today"
    (is-holiday ldate) "weekend"
    :else ""))

(defn current-month []
  (.getMonth (YearMonth/now)))
