(defproject absence "0.1.0-SNAPSHOT"
  :plugins [[lein-ring "0.12.5"]]
  :jvm-opts ["-Duser.timezone=Asia/Tokyo -Dfile.encoding=UTF-8"]
  :ring {:handler absence.ringing/handler :init absence.ringing/init}
  :dependencies [
  [org.clojure/clojure "1.10.0"]

  [io.forward/clojure-mail "1.0.7"]
  [com.draines/postal "2.0.3"]
  [org.clojure/core.async "0.4.490"]

  [de.ubercode.clostache/clostache "1.4.0"]
  [org.clojure/java.jdbc "0.7.8"]
  [org.xerial/sqlite-jdbc "3.25.2"]

  [ring "1.7.1"]
  [compojure "1.6.1"]
  [yogthos/config "1.1.1"]

  ])
