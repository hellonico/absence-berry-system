#!/usr/bin/env inlein

'{:dependencies [[org.clojure/clojure "1.8.0"]]}
  
(require '[clojure.pprint])
(import '[java.time LocalDate])

(->> (LocalDate/now) 
  (str "http://abs.otc/debug/")
		(slurp)
		(read-string)
		(second)
		(:fruits)
		(map #(select-keys % [:name :email :times]) ) 
  (clojure.pprint/pprint))