(require '[excel-clj.core :as excel])

(def table-data
  [{"Date" #inst"2018-01-01" "% Return" 0.05M "USD" 1500.5005M}
   {"Date" #inst"2018-02-01" "% Return" 0.04M "USD" 1300.20M}
   {"Date" #inst"2018-03-01" "% Return" 0.07M "USD" 2100.66666666M}])

(let [;; A workbook is any [key value] seq of [sheet-name, sheet-grid].
      ;; Convert the table to a grid with the table-grid function.
      workbook {"My Generated Sheet" (excel/table-grid table-data)}]
  (excel/quick-open! workbook))


; ring answer
 {:status 200
  :headers {"Content-Type" "application/pdf"}
  :body (FileInputStream. "file.pdf")}