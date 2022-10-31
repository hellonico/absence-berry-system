(ns absence.jobs
  (:require
    [chime.core :as chime]
    [config.core :refer [env]])
  (:import [java.time Instant Duration])
  )


(defn honey-checks[]
  (chime/chime-at (-> (chime/periodic-seq (Instant/now) (Duration/ofMinutes (-> env :honey-b :checkin :interval))))
                  (fn [time]
                    (let [url
                          (-> env :honey-b :checkin :url)
                          ]
                      (println "HoneyB Checkin" time " > " url)
                      (slurp url)))))




;(defn honey-checks[]
;  (let [
;        times (-> (chime/periodic-seq (Instant/now) (Duration/ofMinutes (-> env :honey-b :checkin :interval))))
;        chimes (ca/chime-ch times)]
;    (a/<!! (a/go-loop []
;                      (when-let [time (a/<! chimes)]
;                        (let [url (-> env :honey-b :checkin :url)]
;                          (println "HoneyB Checkin" time " > " url)
;                          (slurp url)))))))