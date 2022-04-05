(ns absence.persistence
    (:require
      [absence.utils :as u]
      [config.core :refer [env]]
      ;[next.jdbc :refer :all]
      [clojure.java.jdbc :refer :all]
     ))

(def db
  {:classname   "org.sqlite.JDBC"
    :subprotocol "sqlite"
    :subname     (-> env :database-file)
    })



(defn findme [target objects _default]
  (let [ks  (keys objects)]
  (loop [ k (first ks) ks ks ]
    (if (nil? k) _default
     (if  (clojure.string/includes? target k)
      (objects k)
      (recur (first ks) (rest ks)))))))

(defn add-icon [fruit src target _default]
  (let [
    tt (clojure.string/lower-case (src fruit))
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

; MOVE ME
;; (defn holidays-for [month user]
;;   ; (println "Holiday for:" user)
;;   {:days (map #(hash-map :h %) (map {0 false 1 true} (take 31 (repeatedly #(rand-int 2)))))})


(defn- to-local 
  "convert string to local date" 
  [d]
  (java.time.LocalDate/parse d (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd")))

(defn- is-between-one [day vec_]
  (let [
        ;n (to-local day)
        n day
        ]
    (and 
     (or (.isEqual n (first vec_)) (.isAfter n (first vec_)))
     (or (.isEqual n (second vec_)) (.isBefore n (second vec_))))))

(defn- is-between-any [day lse]
  (if (some true? (map #(is-between-one day %) lse)) 1 0))

(defn is-between [entries days]
  (let [
        lse (map #(vector (to-local (:holidaystart %)) (to-local (:holidayend %))) entries)
        ]
    ;(map (fn [_] 1) days)

    (map #(is-between-any % lse) days)
    ))

(defn real-days [ym email]
  (let [
      days (range 1 (inc (.getDayOfMonth (.atEndOfMonth ym))))
      days_ (map #(.atDay ym %) days)
      
      s (str (.atDay ym 1))
      e (str (.atEndOfMonth ym))
      
      h (query db [(str
                    "select * from fruit where
      email = '" email "'
      and holidaystart >= '" s "'
      and holidayend >= '" s "'
      and holidaystart <= '" e "'
      order by holidaystart desc")])
        
      user-days-off (is-between h days_)]
    (println email ym user-days-off)
    user-days-off))

(defn query-holidays[ym email]
  (let [
      user-days-off (real-days ym email)
      return-value {:days (map #(hash-map :h %) (map {0 false 1 true} user-days-off))}
  ]
    return-value
  ))

(defn get-fruits-by-month-and-email [ym email]
      (->> 
       query ym email))

(defn get-fruits-by-month
  []
    (->> 
     (query db [
      (str
      "select id,name,email,reason,holidaystart,holidayend from fruit where
      ( holidaystart IS NOT NULL and holidayend >= '" (u/first-day-of-month) "')
      order by holidaystart desc") ]   )
     ; (map add-reason-icon)
     ))

(defn delete-by-id [id]
    (delete! db :fruit ["id = ?" id])
  )

(defn get-fruits-by-email
  [email]
  
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
  ([] (get-fruits (u/today)))
  ([today]
    (query db [
      (str
      "select * from fruit where
      date = '" today "'
      or
      holidaystart <= '" today "' and holidayend >= '" today "'
      order by timesent desc"
        )])))


(defn get-fruits2
  ([] (get-fruits2 (u/today)))
  ([today]
    (let [f  (get-fruits today)]
    {:fruits 
      (->> f
      (filter #(nil? (:holidaystart %)))
      (map add-times-icon)
      (map check-empty-name-fields)
      (map add-reason-icon))
     :holiday (filter #(not (nil? (:holidaystart %))) f)
   })))


(defn insert-one [ abs ]
  (insert! db :fruit abs)
  abs
  )


(comment

(defn create-db []
  (try (db-do-commands db
    (create-table-ddl :fruit
                    [[:name :text]
                    [:appearance :text]
                    [:cost :integer]]))
         (catch Exception e (println e))))

(create-db)

(insert-multi! db :fruit
[{:name "Apple" :appearance "rosy" :cost 24}
    {:name "Orange" :appearance "round" :cost 49}])

(insert! db :fruit {:name "Raspberry" :appearance "rosy" :cost 30})

(def output
  (query db "select * from fruit "))

  (count output)

  (keys (first output))
  (:cost (first output))

  (query db
    ["select * from fruit where cost < ?" "100"]
    {:name :cost})

)
