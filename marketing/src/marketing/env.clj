(ns marketing.env
  (:refer-clojure :exclude [get])
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]))

(def ^:private all-env-vars [{:name ::PORT :default 3000 :int? true}
                             {:name ::HOST :default "localhost"}
                             {:name ::REPO_URL}
                             {:name ::WEB_FLUSH_COMMAND :default "flush"}
                             {:name ::WEB_FLUSH_COMMAND_ENABLED :default 1 :int? true}
                             {:name ::FLUSH_INTERVAL_MINS :default 10 :int? true}
                             {:name ::FLUSH_THRESHOLD :default 10000 :int? true}
                             {:name ::FLUSH_DRY_RUN :default 0 :int? true}]) 

(def ^:private env-var-prefix "MARKETING_") 

(defn env-var-name [env-var-kw]
  (str env-var-prefix (name env-var-kw)))

(defn get
  ([kw] (get kw nil))
  ([kw default]
   (let [add-prefix? (some #{kw} (map :name all-env-vars))]
     (or (System/getenv (if add-prefix? (env-var-name kw) kw)) default))))

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
                             ((if env-var-int? get-int get) env-var-kw env-var-default)))
    (intern *ns* (with-meta def-name {:defn defn-name}) env-var-kw)))


(def ^:private defenv-all (delay (defenv all-env-vars)))

(force defenv-all)

; all this complexity due to be able to print all env vars, without code duplications
; it works, next time when there is a need to modify this code, might revisit and come with a better implementation
(defn log-all []
  (doseq [env-var all-env-vars :let [env-var-kw (:name env-var)
                                     ns-sym (symbol (namespace env-var-kw))
                                     env-var-resolved (ns-resolve ns-sym (symbol (name env-var-kw)))
                                     env-var-defn (-> env-var-resolved meta :defn)]]
    (log/info (env-var-name env-var-kw) "=" (or ((ns-resolve ns-sym env-var-defn)) ""))))
