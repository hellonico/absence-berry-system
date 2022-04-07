;; {
;;         'dn':'CN=clcal01y,OU=OTCSystem,DC=example,DC=local',
;;         'password':'Passw0rd',
;;         'ldap':'ldap://172.16.1.95:3268',
;;         'base_dn':'OU=OTC,DC=example,DC=local',
;;         'filter':'(& (!(useraccountcontrol=66050))(!(useraccountcontrol=66082))',
;;         'attrs':['sn','sAMAccountName']
;; }
(def cursor (.search ldap "OU=OTC,DC=example,DC=local" "(objectclass=*)" org.apache.directory.api.ldap.model.message.SearchScope/ONELEVEL "*"))
(def cursor (.lookup ldap  "(objectclass=*)" ))

(def dn (org.apache.directory.api.ldap.model.name.Dn. (into-array String  ["OU=OTC,DC=example,DC=local"])))
(def cursor (.search ldap dn "(objectclass=*)" org.apache.directory.api.ldap.model.message.SearchScope/ONELEVEL "*"))

; ssh -L 3268:172.16.1.95:3268 bagi

; (ldap/get ldap-server "OU=OTC,DC=example,DC=local")
;(ldap/search ldap-server "OU=OTC,DC=example,DC=local" )

; #object[org.apache.directory.api.ldap.model.entry.DefaultEntry 0x7f844e22 "Entry\n    dn: CN=Erina Ookawara,OU=OTC,DC=example,DC=local\n    objectClass: top\n    objectClass: person\n    objectClass: organizationalPerson\n    objectClass: user\n    mail: ookawara-erina@jpx-otc.com\n    uSNCreated: 12797\n    description: 大河原 江里奈\n    whenChanged: 20200629121945.0Z\n    primaryGroupID: 513\n    givenName: Erina\n    objectGUID: 0xF2 0x4E 0xDE 0xA7 0xF1 0xCD 0x55 0x48 0xB3 0x84 0xA9 0xE3 0x85 0x10 0x4C 0x5A \n    instanceType: 4\n    objectSid: 0x01 0x05 0x00 0x00 0x00 0x00 0x00 0x05 0x15 0x00 0x00 0x00 0x48 0xE8 0xA9 0x94 ...\n    whenCreated: 20190930073617.0Z\n    sn: OOKAWARA\n    dSCorePropagationData: 16010101000000.0Z\n    userAccountControl: 66082\n    lastLogonTimestamp: 132339793830923399\n    cn: Erina Ookawara\n    sAMAccountName: ookawara\n    sAMAccountType: 805306368\n    userPrincipalName: ookawara@example.local\n    displayName: Erina Ookawara\n    name: Erina Ookawara\n    distinguishedName: CN=Erina Ookawara,OU=OTC,DC=example,DC=local\n    objectCategory: CN=Person,CN=Schema,CN=Configuration,DC=example,DC=local\n    memberOf: CN=Application,OU=OTC,DC=example,DC=local\n    uSNChanged: 181624\n"]
 (ns example
   (:require [clj-ldap.client :as ldap]))


(def ldap-server (ldap/connect {:host "localhost:3268" :bind-dn "CN=clcal01y,OU=OTCSystem,DC=example,DC=local" :password "Passw0rd"}))


(ldap/search ldap-server "OU=OTC,DC=example,DC=local" {:filter "sn=*" :attributes [:displayName :useraccountcontrol]})
(ldap/search ldap-server "OU=OTC,DC=example,DC=local" {:filter "useraccountcontrol=66048" :attributes [:displayName :useraccountcontrol]})

(require '[clojure.java.io :as io])
(require '[excel-clj.core :as excel])

(def data (ldap/search ldap-server "OU=OTC,DC=example,DC=local" {:filter "(objectClass=person)" :attributes [:memberOf :primaryGroupId :displayName :useraccountcontrol]}))
(let [
      workbook {"People" (excel/table-grid data )}
      temp-file (io/file "people.xlsx")]
  (excel/write! workbook temp-file)
  (java.io.FileInputStream. temp-file))
  
 ; sdk install java 17.0.2-tem  

 (defn which?
   "Checks if any of elements is included in coll and says which one
  was found as first. Coll can be map, list, vector and set"
   [coll & rest]
   (let [ncoll (if (map? coll) (keys coll) coll)]
     (reduce
      #(or %1  (first (filter (fn [a] (= a %2))
                              ncoll))) nil rest)))

 ; (def data (ldap/search ldap-server "OU=OTC,DC=example,DC=local" {:filter "(memberOf=CN=ABS,OU=OTC,DC=example,DC=local)"}))
 (def data (ldap/search ldap-server "OU=OTC,DC=example,DC=local" {:filter "(memberOf=CN=ABS,OU=OTC,DC=example,DC=local)"}))
 {:description "西田 二郎", :userAccountControl "66048", :objectClass #{"top" "user" "person" "organizationalPerson"}, :sAMAccountType "805306368", :whenCreated "20141020024340.0Z", :objectCategory "CN=Person,CN=Schema,CN=Configuration,DC=example,DC=local", :instanceType "4", :dSCorePropagationData "16010101000000.0Z", :displayName "Nicolas Modrzyk", :whenChanged "20220328081525.0Z", :name "Nicolas Modrzyk", :sAMAccountName "nico", :mail "nicolas-modrzyk@jpx-otc.com", :userPrincipalName "nico@example.local", :objectSid "H詔z���hfr", :uSNChanged "1268774", :dn "CN=Nicolas Modrzyk,OU=OTC,DC=example,DC=local", :objectGUID "��ㆥ�VK�EA��y", :givenName "Nicolas", :sn "MODRZYK", :memberOf "CN=Application,OU=OTC,DC=example,DC=local", :distinguishedName "CN=Nicolas Modrzyk,OU=OTC,DC=example,DC=local", :lastLogonTimestamp "132924177814927670", :uSNCreated "12716", :primaryGroupID "513", :cn "Nicolas Modrzyk"}