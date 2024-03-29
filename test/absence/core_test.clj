(ns absence.core-test
  (:require [clojure.test :refer :all]
            [absence.notification :as r]
            [absence.utils :as u]))

(deftest message-test
  (are [x y]
    (= x y)
    (r/inner-drt "2022-11-30", "1230-20230104,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart (u/current-year-with-md "12-30"), :holidayend (u/current-year-with-md "01-04")}

    (r/inner-drt "2022-03-10", "20230403,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2023-04-03", :holidayend "2023-04-03"}

    (r/inner-drt "2022-03-10", "20220403,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-04-03", :holidayend "2022-04-03"}

    (r/inner-drt "2022-03-10", "20220403-20220405,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart "2022-04-03", :holidayend "2022-04-05"}

    (r/inner-drt "2022-03-10", "0403-0405,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart (u/current-year-with-md "04-03"), :holidayend (u/current-year-with-md "04-05")}

    (r/inner-drt "2022-03-10", "0403-0405,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart (u/current-year-with-md "04-03"), :holidayend (u/current-year-with-md "04-05")}

    (r/inner-drt "2022-03-10", "0403,all day,vacation")
    {:times "all day", :reason  "vacation", :holidaystart (u/current-year-with-md "04-03"), :holidayend (u/current-year-with-md "04-03")}

    (r/inner-drt "2022-03-10", "0403,,vacation")
    {:times "all day", :reason  "vacation", :holidaystart (u/current-year-with-md "04-03"), :holidayend (u/current-year-with-md "04-03")}

    (r/inner-drt "2022-03-10", "20230403,2pm~,soccer")
    {:times "2pm~", :reason  "soccer", :date "2023-04-03"}

    (r/inner-drt "2022-03-10", "0403,2pm~,soccer")
    {:times "2pm~", :reason  "soccer", :date (u/current-year-with-md "04-03")}

    ; this properly uses the sent date ...
    (r/inner-drt "2022-04-03", "2pm~,soccer")
    {:times "2pm~", :reason  "soccer", :date "2022-04-03"}

    ))