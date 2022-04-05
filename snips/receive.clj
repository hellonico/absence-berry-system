
(comment
  (stop-manager)

  (recover)

  (def my-inbox-messages
    (take 1 (all-messages gmail-store "inbox")))

  (def results
    (search-inbox gmail-store [:received-on "2018-04-03"]))
  (count results)

  (def first-message
    (first my-inbox-messages))

  (p/insert-one (parse-msg (read-message first-message)))

  (clojure.pprint/pprint (read-message  first-message))
    ; (parse-msg (read-message first-message)))
  )
