(ns absence.import
  (:import [java.util Date])
  (:require
    [absence.notification :as r]
    [absence.persistence :as p]
    ))

(defn import-msg [_name _address _subject ]
  (let [msg (r/parse-msg {
    :subject _subject
    :date-sent (Date.)
    :from [{:name _name :address _address}]
    }) ]
  (println "importing ...")
  (clojure.pprint/pprint msg)
  (p/insert-one msg)))

(defn -main [& args]
  (import-msg (first args) (second args) (nth args 2)))
