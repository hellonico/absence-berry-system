(ns absence.exceling
  (:require 
   [clojure.java.io :as io]
   [absence.persistence :as p]
   [excel-clj.core :as excel]))

(def empty-table-data
  [{:id	"" :name	"" :email ""	:reason "" :holidaystart ""
   :holidayend ""}])

(defn get-excel[]
(let [data (p/get-fruits-by-month)
      workbook {"ABS" (try
                        (excel/table-grid data )
                        (catch Error e (excel/table-grid empty-table-data)))}
      temp-file (io/file "abs.xlsx")]
  (excel/write! workbook temp-file)
  (java.io.FileInputStream. temp-file)))
  