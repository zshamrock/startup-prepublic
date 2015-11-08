(ns marketing.main
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [marketing.web :as web]))

(defn -main [& args] 
  (let [port (or 
               (System/getenv "MARKETING_PORT")
               5000)
        host (or 
               (System/getenv "MARKETING_HOST")
               "localhost")] 
    (jetty/run-jetty web/app {:port port, :host host})))
