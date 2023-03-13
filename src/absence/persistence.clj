(ns absence.persistence
  (:require
    [clojure.string :as str]
    [clojure.string :as s]
    [absence.utils :as u]
    [config.core :refer [env]]
    [clojure.java.jdbc :refer [delete! insert! query]]))

(def db (-> env :database))

(defn findme [target objects _default]
  (let [ks (keys objects)]
    (loop [k (first ks) ks ks]
      (if (nil? k) _default
                   (if (s/includes? target k)
                     (objects k)
                     (recur (first ks) (rest ks)))))))

(defn add-icon "enhance entry with icons as defined in the environment"
  [fruit src target _default]
  (let [
        tt (s/lower-case (src fruit))
        icon (findme tt (-> env :icons src) _default)
        ]
    (conj {target icon} fruit)))

(defn add-times-icon [fruit]
  (add-icon fruit :times :times_icon "blank"))

(defn add-reason-icon [fruit]
  (add-icon fruit :reason :reason_icon "blank"))

(defn ^:deprecated check-empty-name-fields [fruit]
  (if (empty? (:name fruit))
    (merge fruit {:name (get (:people env) (:email fruit))})
    fruit))

(defn get-fruits-by-month
  []
  (->>
    (query db [
               (str
                 "select id,name,email,reason,holidaystart,holidayend,telework from fruit where "
                 "telework = false and"
                 "( holidaystart IS NOT NULL and holidayend >= '" (u/first-day-of-month) "' and holidayend <= '" (u/last-day-of-month) "') "
                 " order by holidaystart desc")])))

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
      order by holidaystart desc")])

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
   (let [f (get-fruits today)]
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

(defn insert-one [abs]
  (println ">> " abs)
  (insert! db :fruit abs) abs)

(defn last-for-email [_map]
  (let [email (str (:email _map))]
    (merge _map
           (first (query db [(str "select * from fruit where email = '" email "' order by id desc limit 1")])))))

;
; HOLIDAYS
;

(defn query-db-days
  "Retrieve days for given month and given email from the database"
  [ym email]
   (let [s (str (.atDay ym 1))
         e (str (.atEndOfMonth ym))]
     (query db [(str
                  "select * from fruit where
                  email = '" email "'
      and
      ((holidaystart >= '" s "' or holidayend >= '" s "') or (date >= '" s "' and date <= '" e "'))
      order by holidaystart desc")])))

(defn add-metadata-to-entry
  "Add some metadata on the entry retrieved from the database.
  Notably:
  d1: holidaystart or date
  d2: holidayend or date"
  [entry]
  (merge entry
       {:class (str "day"
                    (cond
                      (= (entry :telework) 1) 1
                      (not (empty? (entry :holidaystart))) 2
                      :default 3))}
     (if (not (empty? (entry :holidaystart)))
       {:d1 (u/to-local (:holidaystart entry)) :d2 (u/to-local (:holidayend entry))}
       {:d1 (u/to-local (:date entry)) :d2 (u/to-local (:date entry))})))

(defn- is-between-one [day vec_]
    (and
      (or (.isEqual day (vec_ :d1)) (.isAfter day (vec_ :d1)))
      (or (.isEqual day (vec_ :d2)) (.isBefore day (vec_ :d2)))))

(defn- check-one-day-klass [is-in]
  (let [count-telework (count (filter #(= 1 (% :telework)) is-in))
        count-holidays (count (filter #(not (empty? (% :holidaystart))) is-in))
        count-yoji (count (filter #(= 0 (% :telework)) is-in))]

  (cond
    (or
    (and (< 0 count-telework) (< 0 count-yoji))
    (and (< 0 count-holidays) (< 0 count-yoji))
    (and (< 0 count-holidays) (< 0 count-telework)))
    ; new mixed
    "day5"
    (and (< 0 count-telework) (= 0 count-yoji) (= 0 count-holidays))
    ; telework
    "day1"
    (and (= 0 count-holidays) (= 0 count-telework) (< 0 count-yoji))
    "day3"
    ; beige
    :else "day2" ; holidays
    )
  ))

(defn- check-one-day
  " return a hash-map for one day {:class day0} or {:class day1}"
  [day lse]
  (let [is-in (filter #(is-between-one day %) lse)
        ; _ (println day "> " is-in ":" (count is-in))
        take-first (first is-in)]
    (case (count is-in)
      0 (hash-map :class "day0")
      1 take-first
        (merge
        take-first
             {:class (check-one-day-klass is-in) :times " " :reason (str/join " " (map #(str (:times %) ":" (:reason %)) is-in))})
      )))

(defn- real-days [ym email]
  (let [days (u/month-range-as-localdates ym)
        entries (map add-metadata-to-entry (query-db-days ym email))]
    (map #(check-one-day % entries) days)))

(defn query-holidays
  "Returns a list of days, where each day is a map"
  [ym email]
  (let [user-days-off (real-days ym email)]
    ; (println user-days-off)
    {:days user-days-off}))

(defn items-today [email]
  (let [today (u/today)
        tomorrow (u/tomorrow)]
    (query db [(str
      "select * from fruit where
       email = '" email "'
       and
       holidaystart >= '" today "' and holidayend >= '" today "'
       order by holidaystart desc")])))

(defn telework-today[email]
  (let [items (items-today email) f (filter #(= 1 (% :telework)) items)]
    (>= (count f ) 1)))