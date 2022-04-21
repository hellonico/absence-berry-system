(ns absence.send
  (:require
    [absence.utils :as u]
    [clojure.core.async :as async]
    [config.core :refer [env]]
    [clostache.parser :as m]
    [postal.core :as postal]))

(defn sync-send [message]
  (println "> " (u/now) "\t" (first (:to message)))
  (postal/send-message
    (-> env :postal :sender)
    message))

(defn sending [ message]
  (async/thread
    (sync-send message)))

(defn dump-before-send [msg]
  (u/write-msg-to-file
    (str
      (-> env :inbox)
      (System/currentTimeMillis)
      " > "
      (:email msg))
    msg)
  msg)

(defn abs-ack-send [ abs ]
  (if (-> env :debug)
    (dump-before-send abs))

  ; (println (m/render-resource "email.mustache" abs))
  (sending
    {:from (-> env :ack :sender)
     :to [(:email abs)]
     :subject (-> env :ack :subject)
     :body [{:type "text/html; charset=utf-8"
             :content (m/render-resource "email.mustache" abs)}]}))