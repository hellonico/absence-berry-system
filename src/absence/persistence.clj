(ns absence.persistence
    (:require
      [absence.utils :as u]
      [clojure.java.jdbc :refer :all]))

(def db
  {:classname   "org.sqlite.JDBC"
    :subprotocol "sqlite"
    :subname     (-> env :database-file)
    })

(defn get-fruits
  ([] (get-fruits (u/today)))
  ([today]
    (query db ["select * from fruit where date = ? order by timesent desc" today ])))

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
