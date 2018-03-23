(ns absence.ringing
    (:require 
        [absence.persistence :as p]
        [absence.utils :as u]
        [config.core :refer [env]]
        [compojure.core :refer :all]
        [compojure.route :as route]
        [clostache.parser :as m]))

(defroutes handler
    (GET "/" [] 
        (m/render-resource "index.mustache" 
            {:email (-> env :store :user)
             :today (u/today) 
             :tomorrow (u/tomorrow) 
             :yesterday (u/yesterday) }))
    (GET "/abs" []
        (m/render-resource "fruits.mustache" 
            {:today (u/today) 
             :yesterday (u/yesterday) 
             :fruits (p/get-fruits)}))
    (GET "/abs/:date" [date]
        (m/render-resource "fruits.mustache" 
            {:today date 
             :fruits (p/get-fruits date)}))   
     (route/resources "/")
     (route/not-found "<h1>Page not found</h1>"))
