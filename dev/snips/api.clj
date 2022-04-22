(require '[clojure.data.json :as json])
(require '[absence.utils :as u])
(def cal
  (->
  "https://usgen-api.tw.otc/api/all_schedule"
  slurp
  (json/read-str :key-fn keyword)))

(def days
  (map #(hash-map :date (u/to-local (:release_date %) "yyy/MM/dd") :event (str (:env %) " - " (:version %))) cal))