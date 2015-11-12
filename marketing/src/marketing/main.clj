(ns marketing.main
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [marketing.web :as web]
            [marketing.env :as env]
            [marketing.store :as store]
            [clojure.java.io :as io]))

(defn -main [& args] 
  (if (or 
        (.exists (io/file store/local-repo-path))
        (env/repo))
    (let [port (env/port) 
          host (env/host)] 
      (jetty/run-jetty web/app {:port port, :host host})))
  (binding [*out* *err*]
    (println "Repository URL where to store the data is required. Please set MARKETING_REPO environment variable")))
