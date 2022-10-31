(ns absence.jobs
  (:require
    [chime.core :as chime ]
    [chime.core-async :as ca ]
    [clojure.core.async :as a]
    [config.core :refer [env]]
    ;[clj-http.client :as client]
    )
  (:import [java.time Instant Duration])
  )


(defn honey-checks[]
  (let [
        times (-> (chime/periodic-seq (Instant/now) (Duration/ofMinutes (-> env :honey-b :checkin :interval))))
        chimes (ca/chime-ch times)]
    (a/<!! (a/go-loop []
                    (when-let [time (a/<! chimes)]
                    (let [url (-> env :honey-b :checkin :url)]
                      (println "HoneyB Checkin" time " > " url)
                      (slurp url)))))))