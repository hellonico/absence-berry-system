(ns absence.calendars
  (:import
    [java.time LocalDate]
    [com.github.holidayjp.jdk8 HolidayJp])
  (:require [clojure.data.json :as json]
            [config.core :refer [env]]
            [clojure.core.memoize :as memo]
            [clojure.java.io :as io]
            [absence.utils :as u]
            [clojure.string :as str]))

;
;
;
(defn is-holiday [ldate]
  (or
    (HolidayJp/isHoliday ^LocalDate ldate)
    (= 6 (.getValue (.getDayOfWeek ldate)))
    (= 7 (.getValue (.getDayOfWeek ldate)))))

;
;
;
(defn calendar [ymmonth title-fn label-fn klass-fn]
  (map
    (fn [d]
      (hash-map :title (title-fn d) :label (label-fn d) :klass (klass-fn d)))
    (u/month-range-as-localdates ymmonth)))

(defn calendar-2
  ([ymmonth title-fn label-fn klass-fn]
  (calendar
    ymmonth
    title-fn label-fn klass-fn)))

(defn list-of-days [ymmonth]
  (calendar-2
    ymmonth
    #(.getDayOfMonth %)
    #(.getDayOfMonth %)
    #(cond (= % (LocalDate/now)) "today" (is-holiday %) "weekend" :else "")))

;
;
;

(defn load-calendar
  ([_map] (load-calendar (_map :url) (_map :date-field) (_map :date-format) (_map :event-format-fn) ))
  ([ url date-field date-format event-format-fn]
  (let [cal (-> url slurp (json/read-str :key-fn keyword))
        ;_ (println cal)
        days
        (map #(hash-map :date (u/to-local (% date-field) date-format) :event (event-format-fn %)) cal)]
    (group-by :date days))))

(defn from-config_ [ymmonth file]
  (let [cal-map (load-file file)
        ;_ (clojure.pprint/pprint cal-map)
        days (load-calendar cal-map)
        ;_ (println days)
        ]
    (calendar-2 ymmonth
                (partial (cal-map :title-fn) days)
                (partial (cal-map :label-fn) days)
                (partial (cal-map :klass-fn) days))))

(def from-config
  (memoize from-config_))

(defn make-calendars_ [ymmonth]
  (conj
    (map #(hash-map
            :name (first (str/split  (.getName (io/as-file %)) #"\."))
            :days (from-config ymmonth %)) (-> env :calendars))
    {:name "Days" :days (list-of-days ymmonth)}))

(def make-calendars
  (memo/ttl make-calendars_ {} :ttl/threshold 3600))