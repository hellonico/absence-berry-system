(ns absence.ring-test
  (:require [clojure.test :refer :all]
            [absence.ringing :as ringing]
            [cheshire.core :as json]
            [absence.routehelpers :as h]
            [absence.persistence :as p]
            [absence.persist_test :as pt]
            [ring.mock.request :as mock]))
(defn lastid-with-ring[]
  (Integer/parseInt (:body (ringing/handler (mock/request :get "/api/lastid")))))
(defn delete-by-id-with-ring[id]
  (ringing/handler (mock/request :get (str "/api/delete/" id))))
(defn delete-last-with-ring[]
  (delete-by-id-with-ring (lastid-with-ring)))

(deftest your-json-handler-test
  (let [
        lastid1 (lastid-with-ring)
        ; TODO perform a post via ring instead
        entry (h/process-one-entry pt/test-entry-line)
        ;h2 (p/query-holidays-test "0603" "hellonico@gmail.com")
        ;_ (println h2)
        ;_ "{\"fruits\":[{\"email\":\"hellonico@gmail.com\",\"date\":null,\"telework\":0,\"late\":false,\"timesent\":\"2022-10-25 10:14\",\"name\":\"nico\",\"holidayend\":\"2023-06-05\",\"times\":\"all day\",\"reason\":\"workation\",\"times_icon\":\"blank\",\"holidaystart\":\"2023-06-03\",\"id\":4988,\"reason_icon\":\"blank\"}]}"
        ;res (json/parse-string (str "{\"fruits\":[{\"email\":\"hellonico@gmail.com\",\"date\":null,\"telework\":0,\"late\":false,\"timesent\":\"2023-03-14 11:28\",\"name\":\"nico\",\"holidayend\":\"2023-06-05\",\"times\":\"all day\",\"reason\":\"workation\",\"times_icon\":\"blank\",\"holidaystart\":\"2023-06-03\",\"id\":5007,\"reason_icon\":\"blank\"},{\"email\":\"hellonico@gmail.com\",\"date\":null,\"telework\":0,\"late\":false,\"timesent\":\"2023-03-14 11:26\",\"name\":\"nico\",\"holidayend\":\"2023-06-05\",\"times\":\"all day\",\"reason\":\"workation\",\"times_icon\":\"blank\",\"holidaystart\":\"2023-06-03\",\"id\":5006,\"reason_icon\":\"blank\"},{\"email\":\"hellonico@gmail.com\",\"date\":null,\"telework\":0,\"late\":false,\"timesent\":\"2023-03-14 11:25\",\"name\":\"nico\",\"holidayend\":\"2023-06-05\",\"times\":\"all day\",\"reason\":\"workation\",\"times_icon\":\"blank\",\"holidaystart\":\"2023-06-03\",\"id\":5005,\"reason_icon\":\"blank\"},{\"email\":\"hellonico@gmail.com\",\"date\":null,\"telework\":0,\"late\":false,\"timesent\":\"2023-03-14 11:24\",\"name\":\"nico\",\"holidayend\":\"2023-06-05\",\"times\":\"all day\",\"reason\":\"workation\",\"times_icon\":\"blank\",\"holidaystart\":\"2023-06-03\",\"id\":5004,\"reason_icon\":\"blank\"},{\"email\":\"hellonico@gmail.com\",\"date\":null,\"telework\":0,\"late\":false,\"timesent\":\"2023-03-14 11:23\",\"name\":\"nico\",\"holidayend\":\"2023-06-05\",\"times\":\"all day\",\"reason\":\"workation\",\"times_icon\":\"blank\",\"holidaystart\":\"2023-06-03\",\"id\":5003,\"reason_icon\":\"blank\"},{\"email\":\"hellonico@gmail.com\",\"date\":null,\"telework\":0,\"late\":false,\"timesent\":\"2023-03-14 11:21\",\"name\":\"nico\",\"holidayend\":\"2023-06-05\",\"times\":\"all day\",\"reason\":\"workation\",\"times_icon\":\"blank\",\"holidaystart\":\"2023-06-03\",\"id\":5002,\"reason_icon\":\"blank\"},{\"email\":\"hellonico@gmail.com\",\"date\":null,\"telework\":0,\"late\":false,\"timesent\":\"2023-03-14 11:20\",\"name\":\"nico\",\"holidayend\":\"2023-06-05\",\"times\":\"all day\",\"reason\":\"workation\",\"times_icon\":\"blank\",\"holidaystart\":\"2023-06-03\",\"id\":5001,\"reason_icon\":\"blank\"}]}"))

        lastid2 (lastid-with-ring)
        ;res (p/delete-by-id (:id (last h2)))
        res (delete-last-with-ring)
        ]
  (is
    (= (+ 1 lastid1) lastid2)
    (=
        ;(ringing/handler (-> (mock/request :get "/api/date/2023-06-03")
        ;                   ;(mock/json-body {:foo "bar"})
        ;                      ))
        res
         ;{:status  200
         ; :headers {"content-type" "application/json"}
         ; :body    {:key "your expected result"}}
         {:status  200
          :headers {"Content-Type" "application/json"
                    "Pragma"       "no-cache"}
          :body    []
                    }))))
