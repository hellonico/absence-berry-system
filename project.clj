(defproject absence "0.1.0-SNAPSHOT"
  :plugins [[lein-ring "0.12.6"]]
  :main absence.ringing
  :repositories [["bintray" "https://jcenter.bintray.com/"]]
  :jvm-opts ["-Duser.timezone=Asia/Tokyo" "-Dfile.encoding=UTF-8" "-Dclojure.tools.logging.factory=clojure.tools.logging.impl/jul-factory"]
  :profiles {:mail {:aot [absence.receive] :main absence.receive :uberjar-name "abs-mail.jar"}
             :ring {:aot [absence.ringing] :main absence.ringing :uberjar-name "abs-ring.jar"}
             :dev {
                   ;:jvm-opts ["-DsocksProxyHost=localhost" "-DsocksProxyPort=10090" ]
                   :plugins [[com.jakemccrary/lein-test-refresh "0.25.0"]]}}
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
                 [ring-json-response "0.2.0"]
                 [ring-logger "1.1.1"]
                 [compojure "1.6.2"]
                 [yogthos/config "1.2.0"]

                 [org.clojars.mjdowney/excel-clj "2.1.0"]
                 [org.apache.logging.log4j/log4j-core "2.17.2"]

                 [com.github.holidayjp/holidayjp-jdk8 "2.0.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/core.memoize "1.0.257"]

                 [org.clojars.pntblnk/clj-ldap "0.0.17"]

                 ; NEW FEATURES
                 [origami/origami "4.5.1-0"]
                 ;[io.sentry/sentry-clj "5.7.178"]
                 [camdez/honeybadger "0.4.1"]

                 ; cron for HB reporting
                 [jarohen/chime "0.3.3"]
                 ;[clj-http "3.12.3"]
                 ])
