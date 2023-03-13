(ns absence.persist_test
  (:require [clojure.test :refer :all]
            ;[absence.notification :as r]
            [absence.routehelpers :as h]
            [absence.utils :as u]))



(defn record->map
  [record]
  (let [f #(if (record? %) (record->map %) %)
        ks (keys record)
        vs (map f (vals record))]
    (zipmap ks vs)))

(deftest process-test
  (is
    (=
      (hash-map
        :name "nico"
         ;:timesent "2022-05-31 23:06"
         :email "hellonico@gmail.com"
         :reason "workation"
         :times "all day"
         :date nil
         :telework false
         :holidaystart (u/current-year-with-md "06-03")
         :holidayend (u/current-year-with-md "06-05"))

        (dissoc
          (record->map
           (h/process-one-entry "nico" "hellonico@gmail.com" "0603-0605" "workation" "all day"))
          :timesent
          ))))