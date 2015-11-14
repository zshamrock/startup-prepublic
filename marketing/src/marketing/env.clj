(ns marketing.env)

(defn get
  ([kw] (get kw nil))
  ([kw default]
   (or (System/getenv (name kw)) default)))

(defn port []
  (get :MARKETING_PORT 5000))

(defn host []
  (get :MARKETING_HOST "localhost"))
; make it inside .marketing as .emails.txt
(defn store []
  (get :MARKETING_STORE "/var/vautoservice/emails.txt"))

(defn repo []
  (get :MARKETING_REPO))

(defn flush-interval-mins []
  (get :MARKETING_FLUSH_INTERVAL_MINS 10))
