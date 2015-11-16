(ns marketing.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as response]
            [ring.middleware.json :refer [wrap-json-body]]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [marketing.env :as env]
            [marketing.store :as store]))

(defroutes main-routes
  (GET "/" [] (response/resource-response "index.html"))

  (POST "/" {body :body} 
        (async/go (async/>! store/store-chan (:email body)))
        (response/response ""))

  (route/resources "/" {:root ""})

  (route/not-found (response/resource-response "404.html"))

  (let [web-flush-command (env/web-flush-command)]
    (when (seq web-flush-command)
      (log/info "Web flush command is available through" (str "/" web-flush-command))
      (POST (str "/" web-flush-command) [] 
            (async/go (store/flush))
            (response/response "")))))

(def app 
  (do
    ; if called as 'lein ring server-headless' (so skipping the -main), we need to initialize the storage as well
    (force store/init)
    (->  main-routes
        (wrap-json-body {:keywords? true}))))
