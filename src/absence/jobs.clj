(ns absence.jobs
  (:require
    [chime.core :as chime]
    [config.core :refer [env]]
    [clj-http.client :as client])
  (:import [java.time Instant Duration])
  )


(defn honey-checks[]
  (chime/chime-at (-> (chime/periodic-seq (Instant/now) (Duration/ofMinutes (-> env :honey-b :checkin :interval))))
                  (fn [time]
                    (let [url (-> env :honey-b :checkin :url)]
                      (println "HoneyB Checkin" time " > " url)
                      (client/get url)
  ))))
