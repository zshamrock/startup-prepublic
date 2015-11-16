(ns marketing.main
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [marketing.web :as web]
            [marketing.env :as env]
            [marketing.store :as store]
            [clojure.java.io :as io]))

(defn -main [& args] 
  (let [port (env/port) 
        host (env/host)] 
    (force store/init)
    (jetty/run-jetty web/app {:port port, :host host})))
