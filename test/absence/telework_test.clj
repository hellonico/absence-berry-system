(ns absence.telework_test
  (:require [clojure.test :refer :all]
            [absence.routehelpers :as h]
            [absence.notification :as n]
            [absence.persistence :as p]
            [absence.utils :as u]))

(def telework-away "nico,hellonico@gmail.com,0703-0705,telework_SWEDEN,all day")
(def telework-home "nico,hellonico@gmail.com,0723-0725,telework,all day")

(deftest simple-telework-test
  (are [x y]
    (= x y)
    (h/process-one-entry telework-away)
    (n/->entry
      "nico"
      (u/now)
      "hellonico@gmail.com"
      "telework_SWEDEN"
      "all day"
      nil
      true
      (u/cymd "07-03")
      (u/cymd "07-05")))
  (p/delete-by-id)
  )

(deftest insert-delete-test
  (let [
        entry (h/process-one-entry telework-away)
        entry2 (h/process-one-entry telework-home)

        ; get month
        q (p/query-holidays (u/to-yearmonth "2023-07") "hellonico@gmail.com")

        ; 3 days remote telework
        f1 (filter #(= "day11" (:class %)) (:days q))
        c1 (count f1)

        ; 3 days home telework
        f2 (filter #(= "day1" (:class %)) (:days q))
        c2 (count f2)

        ]
    (println q)

    (println f1)
    (println f2)

    ; 3 days of remote AWAY
    (is (= c1 3) "remote telework not found")
    ; 3 days of remote HOME
    (is (= c2 3) "home telework not found")


    ;(p/delete-by-id)
    ;(p/delete-by-id)
    ))