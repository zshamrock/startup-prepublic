(ns marketing.env)

(defn get
  ([kw] (get kw nil))
  ([kw default]
   (or (System/getenv (name kw)) default)))

(defn port []
  (get :MARKETING_PORT 5000))

(defn host []
  (get :MARKETING_HOST "localhost"))

(defn store []
  (get :MARKETING_STORE "/var/vautoservice/emails.txt"))

(defn repo []
  (get :MARKETING_REPO))
