#!/usr/bin/env inlein

'{:dependencies [[clojurewerkz/machine_head "1.0.0"] [cheshire "5.9.0"]  [clj-fuzzy "0.4.1"] ]}

(require '[cheshire.core :refer [parse-string]])
(require '[clojure.pprint :refer [pprint]])

; parse json and get raw value from SNIP message
(def json (parse-string (slurp "msg.json") true))
(clojure.pprint/pprint (-> json :slots first :rawValue))

; parse answer from api
(clojure.pprint/pprint (read-string (slurp "today.edn")))

(use '[clj-fuzzy.metrics])
(println (mra-comparison "Niko" "Nico"))
; {:minimum 4
;  :similarity 5
;  :codex ["BYRN" "BRN"]
;  :match true}