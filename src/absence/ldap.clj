(ns absence.ldap
   (:require
     [clojure.set :as set]
     [clojure.core.memoize :as memo]
     [config.core :refer [env]]
     [opencv4.core :as cv]
     [clojure.java.io :as io]
    [clj-ldap.client :as ldap]))

(def ldap-server 
  (ldap/connect  
   (-> env :ldap :config)))

(defn get-users_ []
  (->>
  (ldap/search
   ldap-server
   (-> env :ldap :query :base)
   (-> env :ldap :query :params))
  (map #(set/rename-keys % (-> env :ldap :mappings)))))

;(def get-users
;  (memoize get-users_))

(def get-users
  (memo/ttl get-users_ {} :ttl/threshold 3600))

(defn get-user-by-filter [fil]
  (->>
    (ldap/search
      ldap-server
      (-> env :ldap :query :base)
      {:byte-valued [:jpegPhoto] :filter fil})
    (first)
    (#(set/rename-keys % (-> env :ldap :mappings)))))

;(defn get-user-by-id [id]
;  (get-user-by-filter (str "(uid=" id ")")))

(defn get-user-by-email [email]
  (get-user-by-filter (str "(mail=" email ")")))

(defn get-user-pic_ [user]
  (let [ld (get-user-by-email user) fp (str "/tmp/" user ".jpg")]
    (with-open [w (io/output-stream fp)] (.write w (:jpegPhoto ld))
    fp)))

(defn get-user-pic__ [user]
  (let [pic  (get-user-pic_ user)]
    (-> pic
      cv/imread
      ;(cv/edge-preserving-filter! 1 60 0.7)
      (cv/detail-enhance! 10 0.15)
      (cv/imwrite pic)
    )
    pic
    ))

(def get-user-pic
  (memo/ttl get-user-pic__ {} :ttl/threshold (* 24 3600)))

