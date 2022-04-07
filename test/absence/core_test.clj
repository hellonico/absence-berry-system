(ns absence.core-test
  (:require [clojure.test :refer :all]
            [absence.utils :as u]))

(deftest a-test
    (is (= java.time.YearMonth (class (u/to-yearmonth "2022-04-06")))))
