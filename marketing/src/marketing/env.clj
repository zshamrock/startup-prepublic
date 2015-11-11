(ns marketing.env)

(defn get
  ([kw] (env kw nil))
  ([kw default]
   (or (System/getenv (name kw)) default)))

(defn port []
  (env :MARKETING_PORT 5000))

(defn host []
  (env :MARKETING_HOST "localhost"))

(defn store []
  (env :MARKETING_STORE "/var/vautoservice/emails.txt"))

(defn repo []
  (env :MARKETING_REPO))
