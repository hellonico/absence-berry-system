(defproject absence "0.1.0-SNAPSHOT"
  :plugins [[lein-ring "0.12.3"]]
  :jvm-opts ["-Duser.timezone=Asia/Tokyo"]
  :ring {:handler absence.ringing/handler}
  :dependencies [
  [org.clojure/clojure "1.8.0"]

  [io.forward/clojure-mail "1.0.7"]
  [com.draines/postal "2.0.2"]
  [org.clojure/core.async "0.4.474"]

  [de.ubercode.clostache/clostache "1.4.0"]
  [org.clojure/java.jdbc "0.7.5"]
  [org.xerial/sqlite-jdbc "3.21.0.1"]

  [ring "1.6.3"]
  [compojure "1.6.0"]
  [yogthos/config "0.8"]

  ])
