(ns absence.persistence
    (:require
      [absence.utils :as u]
      [config.core :refer [env]]
      [clojure.java.jdbc :refer :all]))

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
