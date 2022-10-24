(ns absence.middleware
  ;(:require [sentry-clj.core :as sentry])
  )

(defn wrap-nocache [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers  "Pragma"] "no-cache"))))

; https://www.baeldung.com/clojure-ring

;(defn wrap-fallback-exception
;  [handler]
;  (fn [request]
;    (try
;      (handler request)
;      (catch Exception e
;        (sentry/send-event
;          {:message   (.getMessage e)
;           :throwable e})
;        {:status 500 :body "Something isn't quite right..."}))))