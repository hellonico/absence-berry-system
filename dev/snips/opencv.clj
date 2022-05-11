(require '[absence.ldap :as ldap])
(ldap/get-user-pic "cbuckley@royalnavy.mod.uk")

(require
  '[opencv4.utils :as cvu]
  '[opencv4.core :as cv])


(def pic ldap/get-user-pic "cbuckley@royalnavy.mod.uk")
; (-> pic cv/imread (cv/edge-preserving-filter! 1 60 0.7) (cv/imwrite pic))

(->
  "cbuckley@royalnavy.mod.uk"
  (ldap/get-user-pic)
  cv/imread
  ;(cv/edge-preserving-filter! 1 60 0.7)
  (cv/detail-enhance! 10 0.15)
  (cv/imwrite pic))
