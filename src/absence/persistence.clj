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

        ;"am" (update-in fruit [:class] #(apply str % "am"))
(defn add-times-icon [fruit]
  (let [times (clojure.string/lower-case (:times fruit))]
  (condp #(clojure.string/includes? %2 %1)  times
    "full" (conj {:times_icon "full"} fruit)
    "am" (conj {:times_icon "am"} fruit)    
    "pm" (conj {:times_icon "pm"} fruit)
    "朝" (conj {:times_icon "am"} fruit)
    "午後" (conj {:times_icon "pm"} fruit)
    (conj {:times_icon "blank"} fruit)
    )))

(defn add-reason-icon [fruit]
  (let [reason (clojure.string/lower-case (:reason fruit))  ]
  (condp #(clojure.string/includes? %2 %1)  reason
    "電車" (conj {:reason_icon "train"} fruit)
    "train" (conj {:reason_icon "train"} fruit)
    "shift" (conj {:reason_icon "shift"} fruit)
    "office" (conj {:reason_icon "office"} fruit)
    "帰社" (conj {:reason_icon "office"} fruit)
    "sick" (conj {:reason_icon "sick"} fruit)
    "病気" (conj {:reason_icon "sick"} fruit)
    "痛" (conj {:reason_icon "sick"} fruit)
    (conj {:reason_icon "blank"} fruit)
    )))

(defn get-fruits2
  ([] (get-fruits2 (u/today)))
  ([today]
    (let [f  (get-fruits today)]
    {:fruits 
      (->> f
      (filter #(nil? (:holidaystart %)))
      (map add-times-icon)
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
