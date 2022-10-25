(ns absence.core-test
  (:require [clojure.test :refer :all]
            [absence.notification :as r]
            [absence.utils :as u]))

(deftest a-test
  (is (= java.time.YearMonth (class (u/to-yearmonth "2022-04-06")))))

(deftest message-test
  (are [x y]
    (= x y)
    (r/inner-drt "2022-11-30", "1230-20230104,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-12-30", :holidayend "2023-01-04"}

    (r/inner-drt "2022-03-10", "20230403,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2023-04-03", :holidayend "2023-04-03"}

    (r/inner-drt "2022-03-10", "20220403,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-04-03", :holidayend "2022-04-03"}

    (r/inner-drt "2022-03-10", "20220403-20220405,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-04-03", :holidayend "2022-04-05"}

    (r/inner-drt "2022-03-10", "0403-0405,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-04-03", :holidayend "2022-04-05"}

    (r/inner-drt "2022-03-10", "0403-0405,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-04-03", :holidayend "2022-04-05"}

    (r/inner-drt "2022-03-10", "0403,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-04-03", :holidayend "2022-04-03"}

    (r/inner-drt "2022-03-10", "0403,,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-04-03", :holidayend "2022-04-03"}

    (r/inner-drt "2022-03-10", "20230403,2pm~,soccer")
    {:times "2pm~", :reason  "soccer", :date "2023-04-03"}

    (r/inner-drt "2022-03-10", "0403,2pm~,soccer")
    {:times "2pm~", :reason  "soccer", :date "2022-04-03"}

    (r/inner-drt "2022-04-03", "2pm~,soccer")
    {:times "2pm~", :reason  "soccer", :date "2022-04-03"}

    ))