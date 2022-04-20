(defproject absence "0.1.0-SNAPSHOT"
  :plugins [[lein-ring "0.12.6"]]
  :main absence.ringing
  :repositories [["bintray" "https://jcenter.bintray.com/"]]
  :jvm-opts ["-Duser.timezone=Asia/Tokyo -Dfile.encoding=UTF-8"]
  :profiles {:mail {:aot [absence.receive] :main absence.receive :uberjar-name "abs-mail.jar"}
             :ring {:aot [absence.ringing] :main absence.ringing :uberjar-name "abs-ring.jar"}
             :dev {:plugins [[com.jakemccrary/lein-test-refresh "0.25.0"]]}}
  :ring {:handler absence.ringing/handler :init absence.ringing/init}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [io.forward/clojure-mail "1.0.8"]
                 [com.draines/postal "2.0.5"]
                 [org.clojure/core.async "1.5.648"]

                 [de.ubercode.clostache/clostache "1.4.0"]
                  [org.clojure/java.jdbc "0.7.12"]
                 ; [com.github.seancorfield/next.jdbc "1.2.772"]
                 ; https://cljdoc.org/d/com.github.seancorfield/next.jdbc/1.2.772/doc/getting-started
                 [org.xerial/sqlite-jdbc "3.36.0.3"]

                 [ring/ring "1.9.5"]
                 [compojure "1.6.2"]
                 [yogthos/config "1.2.0"]

                 [org.clojars.mjdowney/excel-clj "2.1.0"]
                 [org.apache.logging.log4j/log4j-core "2.17.2"]

                 [com.github.holidayjp/holidayjp-jdk8 "2.0.1"]

                 [org.clojars.pntblnk/clj-ldap "0.0.17"]])
