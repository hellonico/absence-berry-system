(ns absence.routehelpers
  (:require
   [ring.util.codec]
   [absence.persistence :as p]
   [absence.notification :as n]
   [absence.utils :as u]
   [absence.exceling :as ex]
   [config.core :refer [env]]
   [clostache.parser :as m]))

(defn render-html 
  "render with header and footer"
  [_template _map]
  (m/render-resource
   (str _template ".mustache")
   _map
   {:header (m/render-resource "_header.mustache")
    :footer (m/render-resource "_footer.mustache")}))

(defn handle-date [date]
  {:today date
   :today-str (u/date-to-dayoftheweek date)
   :daybefore (u/day-before date)
   :dayafter (u/day-after date)
   :fruits (p/get-fruits2 date)})

(defn handle-email [email]
  {:fruits (p/get-fruits-by-email email)
   :month false
   :email (str email)})

(defn list-of-days [ymmonth]
  (map
   #(hash-map :l (.getDayOfMonth %) :klass (u/get-klass %))
   (u/month-range-as-localdates ymmonth)))

(defn handle-month [users ymmonth]
  {:month (.getMonth ymmonth)
   :next (.plusMonths ymmonth 1)
   :prev (.minusMonths ymmonth 1)
   :users users
   :days (list-of-days ymmonth)})

(defn handle-home []
  {:email (-> env :store :user)
   :today (u/today)
   :tomorrow (u/tomorrow)
   :yesterday (u/yesterday)
   :links (-> env :front :links)})

(defn handle-excel []
  {:status 200
   :headers {"Content-Type" "application/vnd.ms-excel"}
   :filename "abs.xlsx"
   :body (ex/get-excel)})

(defn process-one-entry [_name _email _dates _reason]
  (let [msg {:from [{:name _name :address _email}]
             :subject (str _dates ",," _reason)
             :date-sent (java.util.Date.)}
        entry (n/parse-msg msg)]
    (p/insert-one entry)
    entry))