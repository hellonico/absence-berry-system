(ns absence.calendars
  (:import
    [java.time LocalDate]
    [com.github.holidayjp.jdk8 HolidayJp])
  (:require [clojure.data.json :as json]
            [absence.utils :as u]))

(defn is-holiday [ldate]
  (or
    (HolidayJp/isHoliday ^LocalDate ldate)
    (= 6 (.getValue (.getDayOfWeek ldate)))
    (= 7 (.getValue (.getDayOfWeek ldate)))))

(defn- calendar [ymmonth fn_]
  (map
    fn_
    (u/month-range-as-localdates ymmonth)))

(defn- calendar-2 [ymmonth fn1 fn2 fn3 ]
  (calendar
    ymmonth
    (fn[d]
      (hash-map :title (fn1 d) :label (fn2 d) :klass (fn3 d)))))

(defn list-of-days [ymmonth]
  (calendar-2
    ymmonth
    #(.getDayOfMonth %)
    #(.getDayOfMonth %)
    #(cond (= % (LocalDate/now)) "today" (is-holiday %) "weekend" :else "")))

(defn get-days_ []
      (let [cal (-> "https://usgen-api.tw.otc/api/all_schedule" slurp (json/read-str :key-fn keyword))
            days (map #(hash-map :date (u/to-local (:release_date %) "yyy/MM/dd") :event (str (:env %) " - " (:version %))) cal)]
        (group-by :date days)))

(def get-days
  (memoize get-days_))

(defn releases [ymmonth]
  (let [days (get-days)]
  (calendar-2 ymmonth
              (fn[d] (apply str (interpose "\n" (map :event (days d)))))
              (fn[d] "")
              #(cond (contains? days %) "today" :else ""))))

(defn make-calendars [ymmonth]
  [{:name "Days" :days (list-of-days ymmonth)}
   {:name "Releases" :days (releases ymmonth)}])