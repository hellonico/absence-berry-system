
(defn- expected-connected-ips []
  (drop 1
        (clojure.string/split
         (:out (sh/sh "bash" "-c" (str "dig +short " (-> env :store :imap)))) #"\n")))

(defn is-listening [ip]
  (let [command (sh/sh "bash" "-c" (str "netstat -an | grep '" ip "'"))
        netstat (:out command)]
    (< 1 (count (clojure.string/split netstat #"\n")))))

(defn is-imap-listening []
  (let [status (map is-listening (expected-connected-ips))
        c (count (filter true? status))
        result (<= 1 c)]
    (println status)
    (println c)
    (println result)
    result))