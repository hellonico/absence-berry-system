(ns absence.middleware
  ;(:require [sentry-clj.core :as sentry])
  )

(defn wrap-nocache [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers  "Pragma"] "no-cache"))))

;(defn wrap-honeybadger [handler]
;  (fn [request]
;    (try
;      (let [response (handler request)]  response)
;      (catch Exception e
;        (do
;          (println "Honey badging:" (-> env :honey-b))
;          (hb/notify (-> env :honey-b)  (Exception. (.getMessage e)))
;          {:status 500 :body (.getMessage e)}
;          )))))

; https://www.baeldung.com/clojure-ring

;(defn wrap-sentry
;  [handler]
;  (fn [request]
;    (try
;      (handler request)
;      (catch Exception e
;        (sentry/send-event
;          {:message   (.getMessage e)
;           :throwable e})
;        {:status 500 :body "Something isn't quite right..."}))))