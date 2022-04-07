
(require '[absence.persistence :as p])
(p/get-fruits "2022-03-22")

(defn get-potatoes
  ([month]
   (query db [(str "select * from fruit where holidaystart <= '2022-03-31' and holidayend >= '2022-03-01' order by timesent desc group by email")])))

(doseq [a (query db [(str "select name,email, holidaystart from fruit where holidaystart <= '2022-04-31' and holidayend >= '2022-04-01' group by email")])] (println a))

; (doseq [a (query db [(str "select name,email, holidaystart from fruit where holidaystart <= '2022-04-31' and holidayend >= '2022-04-01'")])] (println a))

(query db [(str "select * from fruit where holidaystart <= '2022-04-31' and holidayend >= '2022-04-01'")])

; ({:email "hellonico@gmail.com", :date nil, :timesent "2022-04-04 17:16", :name "MODRZYK HERVE NICOLAS", :holidayend "2022-04-03", :times "ALL DAY", :reason "Hello1", :holidaystart "2022-04-01", :id 4779} {:email "hellonico@gmail.com", :date nil, :timesent "2022-04-04 17:16", :name "MODRZYK HERVE NICOLAS", :holidayend "2022-04-08", :times "ALL DAY", :reason "Hello1", :holidaystart "2022-04-05", :id 4780})

(defn holidays-for[user]
  {:days (map {0 false 1 true} (take 31 (repeatedly #(rand-int 2))))})
(->> (ldap/get-users)
     (map #(clojure.set/rename-keys % {:mail :email}))
     (map #(merge % (holidays-for (:email %)))))
                  

(map {0 false 1 true} (take 10 (repeatedly #(rand-int 2))))


(def hs (query-holidays (java.time.YearMonth/parse "2022-04") "hellonico@gmail.com"))
(def lse (map #(vector (to-local (% :holidaystart)) (to-local (% :holidayend))) hs))

(defn is-in [])
; ( 
; {:description "タナヤ・パティル", 
;   :userAccountControl "66048", :objectClass #{"top" "user" "person" "organizationalPerson"}, :sAMAccountType "805306368", :whenCreated "20211222042030.0Z", :email "tanaya-patil@jpx-otc.com", :objectCategory "CN=Person,CN=Schema,CN=Configuration,DC=example,DC=local", :instanceType "4", :dSCorePropagationData "16010101000000.0Z", :displayName "Tanaya Patil", :whenChanged "20220404013302.0Z", :name "Tanaya Patil", :sAMAccountName "tanaya", :userPrincipalName "tanaya@example.local", :objectSid "H詔z���hf3\n", :uSNChanged "1284094", :dn "CN=Tanaya Patil,OU=OTC,DC=example,DC=local", :objectGUID "!��R@�C�:B�h� ^", :givenName "Tanaya", :sn "PATIL", :memberOf ["CN=ABS,OU=OTC,DC=example,DC=local" "CN=Domain Users,CN=Users,DC=example,DC=local"], :distinguishedName "CN=Tanaya Patil,OU=OTC,DC=example,DC=local", :lastLogonTimestamp "132935095829097802", :uSNCreated "1000585", :primaryGroupID "1317", :cn "Tanaya Patil"}
; )
