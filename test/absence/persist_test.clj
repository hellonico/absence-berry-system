(ns absence.persist_test
  (:require [clojure.test :refer :all]
            [absence.routehelpers :as h]
            [absence.notification :as n]
            [absence.persistence :as p]
            [absence.utils :as u]))

(def test-entry-line "nico,hellonico@gmail.com,0603-0605,workation,all day")

(deftest double-entry-test
  (h/process-one-entry test-entry-line)
  (h/process-one-entry "nico,hellonico@gmail.com,0603,sleep,am")

  (let [
        fa (p/query-holidays-with-filter "2023-06" "hellonico@gmail.com" "day5")
        ids (-> fa first :id)
        ]
    (clojure.pprint/pprint fa)
    (println ids)
    (is (= 1 (count fa)))
    (p/delete-by-id ids)))

(deftest process-test
  (are [x y]
    (= x y)
    (h/process-one-entry test-entry-line)
    (n/->entry
      "nico"
      (u/now)
      "hellonico@gmail.com"
      "workation"
      "all day"
      nil
      false
      (u/cymd "06-03")
      (u/cymd "06-05")))
  (p/delete-by-id))

(deftest insert-delete-test
  (let [
        h1 (p/query-holidays-test "0603" "hellonico@gmail.com")
        entry (h/process-one-entry test-entry-line)
        h2 (p/query-holidays-test "0603" "hellonico@gmail.com")
        res (p/delete-by-id (:id (last h2)))
        h3 (p/query-holidays-test "0603" "hellonico@gmail.com")
        ]
    (is
      (= (+ 1 (count h1) (count h2)))
      (= (count h1) (count h3)))
    ;(dotimes [i 3]
      (p/delete-by-id)))

(deftest query-holidays-test
  (let [
        [md email] ["0603" "hellonico@gmail.com"]
        entry (h/process-one-entry test-entry-line)
        ;h1 (p/query-holidays-test md email)
        q1 (p/query-holidays (u/to-yearmonth "2023-06") "hellonico@gmail.com")
        f1 (filter #(not (= "day0" (:class %))) (:days q1))
        c1 (count f1)
        res (p/delete-by-id)
        f2 (filter #(not (= "day0" (:class %))) (:days q1))
        c2 (count f2)
        ]
    (is
      (= c1 3)
      (= c2 0))
    (p/delete-by-id)))