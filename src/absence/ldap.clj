(ns absence.ldap
   (:require [clj-ldap.client :as ldap]))


(def ldap-server 
  (ldap/connect {:host "localhost:3268" :bind-dn "CN=clcal01y,OU=OTCSystem,DC=example,DC=local" :password "Passw0rd"}))
(defn get-users[]
  (ldap/search ldap-server "OU=OTC,DC=example,DC=local" {:filter "(memberOf=CN=ABS,OU=OTC,DC=example,DC=local)"}))