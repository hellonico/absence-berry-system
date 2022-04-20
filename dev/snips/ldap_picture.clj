(require '[absence.ldap :as ldap])
(require '[clojure.java.io :as io])
(def a (nth (ldap/get-users) 10))
(with-open [w (io/output-stream (str (:sn a) ".jpg"))] (.write w (:jpegPhoto a)))