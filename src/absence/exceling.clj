(ns absence.exceling
  (:require 
   [clojure.java.io :as io]
   [absence.persistence :as p]
   [excel-clj.core :as excel]))

;; (def table-data
;;   [{"Date" #inst"2018-01-01" "% Return" 0.05M "USD" 1500.5005M}
;;    {"Date" #inst"2018-02-01" "% Return" 0.04M "USD" 1300.20M}
;;    {"Date" #inst"2018-03-01" "% Return" 0.07M "USD" 2100.66666666M}])

(defn get-excel[]
(let [data (p/get-fruits-by-month)
      ;data table-data
      workbook {"ABS" (excel/table-grid data )}
      temp-file (io/file "abs.xlsx")]
  ;(prn data)
  (excel/write! workbook temp-file)
  (java.io.FileInputStream. temp-file)))
  