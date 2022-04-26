(ns absence.ldap
   (:require
     [clojure.set :as set]
     [clojure.core.memoize :as memo]
    [config.core :refer [env]]
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