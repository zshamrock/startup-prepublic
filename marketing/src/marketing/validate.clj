(ns marketing.validate)

(def ^{:private true :doc "See http://davidcel.is/posts/stop-validating-email-addresses-with-regex/ and 
              https://github.com/plataformatec/devise/blob/master/lib/devise.rb#L109"} 
  email-regex #"\A[^@\s]+@([^@\s]+\.)+[^@\W]+\z")

(def ^:private email-max-length-allowed 50)

(defn possibly-email? [email]
  (and 
    (seq email) 
    (seq (re-matches email-regex email)) 
    (<= (count email) email-max-length-allowed)))
