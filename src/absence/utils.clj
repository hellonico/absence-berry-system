(ns absence.utils
  (:import
    [java.time LocalDate YearMonth]
    [java.time.format DateTimeFormatter]
    [java.io File FileWriter]
    (java.text SimpleDateFormat)
    (java.util Calendar Date)
    (java.time.temporal TemporalAdjusters))
    (:require [clojure.pprint]))

(def date-format "yyyy-MM-dd")

(defn fmt [java-date]
    (.format
    (SimpleDateFormat. date-format)
    java-date))


(defn short-date-to-date [ _short-date ]
  (let[
       cal (Calendar/getInstance)
       this-year (.get cal Calendar/YEAR)
       short-date (if (= (.length _short-date) 4) (str this-year _short-date) _short-date)
       year (Integer/parseInt (apply str (take 4 short-date)))
       day  (Integer/parseInt (apply str (drop 6 short-date)))
       month (dec (Integer/parseInt (apply str (take 2 (drop 4 short-date))) ))

       ]
    (.set cal Calendar/DAY_OF_MONTH day)
    (.set cal Calendar/MONTH month)
    (.set cal Calendar/YEAR year)
    (fmt (.getTime cal))))



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
  (try (YearMonth/parse date-as-string) (catch Exception e (do (println (.getMessage e)) (YearMonth/now)))))

(defn to-local
  "convert string to local date"
  ([d] (to-local d date-format))
  ([d dft]
  (LocalDate/parse d (DateTimeFormatter/ofPattern  dft ))))

(defn month-range-as-localdates [ym]
  (map #(.atDay ym %) (month-day-range ym)))


;
;
;
(defn current-month []
  (.getMonth (YearMonth/now)))
(defn current-year []
  (.getYear (YearMonth/now)))
(defn current-year-with-md [md]
  (str (current-year) "-" md))
(def cymd current-year-with-md)

;
;
;


(defn record->map
  [record]
  (let [f #(if (record? %) (record->map %) %)
        ks (keys record)
        vs (map f (vals record))]
    (zipmap ks vs)))