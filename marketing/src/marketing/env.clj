(ns marketing.env
  (:refer-clojure :exclude [get])
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]))

(def ^:private env-var-prefix "MARKETING_")

(defn env-var-name [env-var-kw]
  (str env-var-prefix (name env-var)))

(defn get
  ([kw] (get kw nil))
  ([kw default]
   (or (System/getenv (env-var-name kw)) default)))

(defn get-int [kw default]
  (let [v (get kw default)]
    (if (integer? v)
      v
      (Integer/parseInt v))))

(defn- defenv [vars]
  (doseq [env-var vars :let [env-var-kw (:name env-var)
                             env-var-default (:default env-var)
                             env-var-int? (:int? env-var)
                             def-name (symbol (name env-var-kw))
                             defn-name (-> env-var-kw
                                           (name)
                                           (string/lower-case)
                                           (string/replace \_ \-)
                                           (symbol))]]
    (intern *ns* defn-name (fn [] 
                             (if env-var-int? get-int get) env-var-kw env-var-default))
    (intern *ns* (with-meta def-name {:defn defn-name}) env-var-kw)))


(comment (defmacro defenv [vars]
  (cons `apply
        (for [env-var vars :let [env-var-name (name (:name env-var))
                                 env-var-default (:default env-var)
                                 env-var-int? (:int? env-var)]]
          (conj []
            `(def ~(symbol env-var-name) ~(:name env-var))
            `(defn ~(-> env-var-name 
                        (string/lower-case)
                        (string/replace \_ \-)
                        symbol
                        ) [] 
               (~(symbol (str "marketing.env/get" (if env-var-int? "-int" ""))) ~(:name env-var) ~env-var-default)))))))

(def ^:private all-env-vars [{:name ::PORT :default 5000 :int? true}
                             {:name ::HOST :default "localhost"}
                             {:name ::REPO_URL}
                             {:name ::WEB_FLUSH_COMMAND}
                             {:name ::FLUSH_INTERVAL_MINS :default 10 :int? true}])

(def defenv-all (delay (defenv all-env-vars)))

(force defenv-all)

; all this complexity due to be able to print all env vars, without code duplications
; it works, if there is a better way to do it, would be nice to compare (at least try with simple (resolve) instead (ns-resolve))
(defn log-all []
  (doseq [env-var all-env-vars :let [env-var-kw (:name env-var)
                                     ns-sym (symbol (namespace env-var-kw))
                                     env-var-resolved (ns-resolve ns-sym (symbol (name env-var-kw)))
                                     env-var-defn (-> env-var-resolved meta :defn)]]
    (log/info (env-var-name env-var-kw) "=" (or ((ns-resolve ns-sym env-var-defn)) ""))))
