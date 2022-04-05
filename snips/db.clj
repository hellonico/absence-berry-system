
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
