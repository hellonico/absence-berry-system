(ns absence.date-test
  (:require [clojure.test :refer :all]
            [absence.notification :as r]
            [absence.utils :as u]))

(deftest short-dates
  (let [ short-date "0302"]
  (is (=
        (u/short-date-to-date short-date)
        (str (u/current-year) "-03-02")))))