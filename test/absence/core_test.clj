(ns absence.core-test
  (:require [clojure.test :refer :all]
            [absence.receive :as r]
            [absence.utils :as u]))

(deftest a-test
    (is (= java.time.YearMonth (class (u/to-yearmonth "2022-04-06")))))

(deftest message-test
  
  (is 
   (= 
    (r/inner-drt "2022-03-10", "0403-0405,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-04-03", :holidayend "2022-04-05"}))
  
  (is
   (=
    (r/inner-drt "2022-03-10", "0403,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-04-03", :holidayend "2022-04-03"}))
  
  (is
   (=
    (r/inner-drt "2022-03-10", "0403,,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-04-03", :holidayend "2022-04-03"}))
  
  (is
   (=
    (r/inner-drt "2022-03-10", "0403,2pm~,soccer")
    {:times "2pm~", :reason  "soccer", :date "2022-04-03"}))
  
  (is
   (=
    (r/inner-drt "2022-04-03", "2pm~,soccer")
    {:times "2pm~", :reason  "soccer", :date "2022-04-03"}))
  
  )