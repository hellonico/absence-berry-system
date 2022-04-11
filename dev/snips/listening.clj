#!/usr/bin/env inlein

'{:dependencies [[clojurewerkz/machine_head "1.0.0"] [cheshire "5.9.0"] ]}

(require '[clojurewerkz.machine-head.client :as mh])
(require '[cheshire.core :refer :all])
(import '[java.time LocalDate])

(defn fetch []
	(->> (LocalDate/now) 
  (str "http://abs.otc/debug/")
		(slurp)
		(read-string)
		(second)
		(:fruits)
		(map #(select-keys % [:name :email :times]) ) 
  ))

(defn handle-intent [ json]

 (println json)
 (println (fetch))

 )

  (let [conn (mh/connect "tcp://172.16.5.107:1883")]
    (mh/subscribe conn {"hello/#" 0} (fn [^String topic _ ^bytes payload]
                                   (println topic ">" (String. payload "UTF-8"))
                                   ; (mh/disconnect conn)
                                   ))

    (mh/subscribe conn {"/hermes/intent/#" 0} (fn [^String topic _ ^bytes payload]
                                   (handle-intent (parse-string (String. payload "UTF-8")))
                                   ))
    
    (mh/publish conn "/hermes/intent/yeah" (generate-string {:foo "bar"}))
    (mh/publish conn "hello/1" "Hello, world")
    (mh/publish conn "hello/nico" "Hello, again")

    
    )
(Thread/sleep 500)
(System/exit 0)