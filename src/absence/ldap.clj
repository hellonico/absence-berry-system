(ns absence.ldap
   (:require 
    [config.core :refer [env]]
    [clj-ldap.client :as ldap]))

(def ldap-server 
  (ldap/connect  
   (-> env :ldap :config)))

(defn get-users[]
  (ldap/search
   ldap-server
   (-> env :ldap :query :base)
   (-> env :ldap :query :params)))