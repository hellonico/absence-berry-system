(ns absence.persistence
  (:require
   [clojure.string :as s]
   [absence.utils :as u]
   [config.core :refer [env]]
   [clojure.java.jdbc :refer [delete! insert! query]]))

(def db (-> env :database))

(defn findme [target objects _default]
  (let [ks  (keys objects)]
  (loop [ k (first ks) ks ks ]
    (if (nil? k) _default
     (if  (s/includes? target k)
      (objects k)
      (recur (first ks) (rest ks)))))))

(defn add-icon "enhance entry with icons as defined in the environment"
  [fruit src target _default]
  (let [
    tt (s/lower-case (src fruit))
    icon  (findme tt (-> env :icons src) _default)
    ]
    (conj {target icon} fruit)))

(defn add-times-icon [fruit]
  (add-icon fruit :times :times_icon "blank"))

(defn add-reason-icon [fruit]
  (add-icon fruit :reason :reason_icon "blank"))

(defn ^:deprecated check-empty-name-fields [fruit]
  (if (empty? (:name fruit))
    (merge fruit {:name (get (:people env) (:email fruit))} )
    fruit))

(defn get-fruits-by-month
  []
    (->> 
     (query db [
      (str
      "select id,name,email,reason,holidaystart,holidayend,telework from fruit where
      telework = false and
      ( holidaystart IS NOT NULL and holidayend >= '" (u/first-day-of-month) "' and holidayend <= '" (u/last-day-of-month) "') "
      " order by holidaystart desc") ]   )))


(defn delete-by-id [id]
    (delete! db :fruit ["id = ?" id]))

(defn get-fruits-by-email
  [email]
      ; telework = false and
      (->> 
     
     (query db [
      (str
      "select * from fruit where
      email = '" email "'
      and
      holidayend >= '" (u/today) "'
      order by holidaystart desc") ]   )

      (map add-reason-icon))
      )
    
(defn get-fruits
  "get fruits internal method ; raw from database"
  ([] (get-fruits (u/today)))
  ([today]
    (query db [
      (str
      "select * from fruit where
      telework = false
      and
      (date = '" today "'
      or
      holidaystart <= '" today "' and holidayend >= '" today "')
      order by timesent desc")])))


(defn get-fruits2
  "get fruits but enhance the values from get-fruits: add times and reason icons, add late"
  ([] (get-fruits2 (u/today)))
  ([today]
    (let [f  (get-fruits today)]
    {:fruits 
      (->> f
      ;(filter #(nil? (:holidaystart %)))
      (map add-times-icon)
      ; (map check-empty-name-fields)
      (map add-reason-icon)
      (map #(merge {:late (nil? (% :holidaystart))} %)))
     ;:holiday
     ;(filter #(not (nil? (:holidaystart %))) f)
     })))

(defn get-last-fruits [n]
  (query db [(str "select * from fruit order by id desc limit " n ";")]))

(defn insert-one [ abs ]
  (insert! db :fruit abs) abs)

(defn last-for-email [_map]
  (let [email (str (:email _map))]
  (merge _map
         (first (query db [(str "select * from fruit where email = '" email "' order by id desc limit 1")])))))


;
; HOLIDAYS
;

(defn- is-between-one [day vec_]
  (if
    (and
     (or (.isEqual day (vec_ :d1)) (.isAfter day (vec_ :d1)))
     (or (.isEqual day (vec_ :d2)) (.isBefore day (vec_ :d2))))
    (inc (vec_ :telework))
    0
    ))

(defn- is-between-any [day lse]
  (let [one (map #(is-between-one day %) lse)
        ret (if (empty? one) 0 (apply max one) )
        ]
    ret))

(defn- is-between [entries days]
  (let [lse (map #(merge % {:d1 (u/to-local (:holidaystart %)) :d2 (u/to-local (:holidayend %))}) entries)]
    (map #(is-between-any % lse) days)))

(defn query-db-days
  ([ym email] (query-db-days ym email false))
  ([ym email telework]
  (let [s (str (.atDay ym 1))
        e (str (.atEndOfMonth ym))]
    ; telework = " telework " and

    (query db [(str
      "select * from fruit where
      email = '" email "'
      and (holidaystart >= '" s "'
      or holidayend >= '" s "')
      order by holidaystart desc")]))))

(defn- real-days [ym email]
  (let [days (u/month-range-as-localdates ym)
        h (query-db-days ym email)
        ret (is-between h days)
        ]
    ;(println ret)
    ret
    ))

(defn query-holidays [ym email]
  (let [user-days-off (real-days ym email)]
    {:days (map #(hash-map :h %) user-days-off) }))