(ns absence.persistence
  (:require
   [clojure.string :as s]
   [absence.utils :as u]
   [config.core :refer [env]]
   [clojure.java.jdbc :refer [delete! insert! query]]))

(def db {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname (-> env :database-file)})

(defn findme [target objects _default]
  (let [ks  (keys objects)]
  (loop [ k (first ks) ks ks ]
    (if (nil? k) _default
     (if  (s/includes? target k)
      (objects k)
      (recur (first ks) (rest ks)))))))

(defn add-icon [fruit src target _default]
  (let [
    tt (s/lower-case (src fruit))
    icon  (findme tt (-> env :icons src) _default)
    ]
    (conj {target icon} fruit)))

(defn add-times-icon [fruit]
  (add-icon fruit :times :times_icon "blank"))

(defn add-reason-icon [fruit]
  (add-icon fruit :reason :reason_icon "blank"))

(defn check-empty-name-fields [fruit]
  (if (empty? (:name fruit))
    (merge fruit {:name (get (:people env) (:email fruit))} )
    fruit))

(defn get-fruits-by-month
  []
    (->> 
     (query db [
      (str
      "select id,name,email,reason,holidaystart,holidayend from fruit where
      telework = false and
      ( holidaystart IS NOT NULL and holidayend >= '" (u/first-day-of-month) "' and holidayend <= '" (u/last-day-of-month) "') "
      " order by holidaystart desc") ]   )))


(defn delete-by-id [id]
    (delete! db :fruit ["id = ?" id]))

(defn get-fruits-by-email
  [email]
  
      (->> 
     
     (query db [
      (str
      "select * from fruit where
      telework = false and
      email = '" email "'
      and
      holidayend >= '" (u/today) "'
      order by holidaystart desc") ]   )

      (map add-reason-icon))
      )
    
(defn- get-fruits
  "get fruits internal method ; raw from database"
  ([] (get-fruits (u/today)))
  ([today]
    (query db [
      (str
      "select * from fruit where
      telework = false and
      date = '" today "'
      or
      holidaystart <= '" today "' and holidayend >= '" today "'
      order by timesent desc")])))


(defn get-fruits2
  "get fruits but enhance the values from get-fruits"
  ([] (get-fruits2 (u/today)))
  ([today]
    (let [f  (get-fruits today)]
    {:fruits 
      (->> f
      (filter #(nil? (:holidaystart %)))
      (map add-times-icon)
      (map check-empty-name-fields)
      (map add-reason-icon))
     :holiday (filter #(not (nil? (:holidaystart %))) f)})))

(defn insert-one [ abs ]
  (insert! db :fruit abs) abs)

;
; HOLIDAYS
;

(defn- is-between-one [day vec_]
  (let [n day]
    (and
     (or (.isEqual n (first vec_)) (.isAfter n (first vec_)))
     (or (.isEqual n (second vec_)) (.isBefore n (second vec_))))))

(defn- is-between-any [day lse]
  (if (some true? (map #(is-between-one day %) lse)) 1 0))

(defn- is-between [entries days]
  (let [lse (map #(vector (u/to-local (:holidaystart %)) (u/to-local (:holidayend %))) entries)]
    (map #(is-between-any % lse) days)))

(defn- query-db-days 
  ([ym email] (query-db-days ym email false))
  ([ym email telework]
  (let [s (str (.atDay ym 1))
        e (str (.atEndOfMonth ym))]
    (query db [(str
      "select * from fruit where
      telework = " telework " and
      email = '" email "'
      and (holidaystart >= '" s "'
      or holidayend >= '" s "')
      order by holidaystart desc")]))))

(defn- real-days [ym email]
  (let [days (u/month-range-as-localdates ym)
        h (query-db-days ym email)]
    (is-between h days)))

(defn query-holidays [ym email]
  (let [user-days-off (real-days ym email)]
    {:days (map #(hash-map :h %) (map {0 false 1 true} user-days-off))}))