(ns marketing.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as response]
            [ring.middleware.json :refer [wrap-json-body]]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [marketing.validate :as validate]
            [marketing.env :as env]
            [marketing.store :as store]))

(defroutes main-routes
  (GET "/" [] (response/resource-response "web/index.html"))

  (POST "/" {body :body} 
        (let [email (:email body) ok-response (response/response "")]
          (if (validate/possibly-email? email)
            (do
              (async/go (async/>! store/store-chan (clojure.string/trim email)))
              ok-response)
            (response/status ok-response 400))))

  (route/resources "/" {:root "/web"})

  (route/not-found (response/resource-response "web/404.html"))

  (let [web-flush-command (env/web-flush-command)]
    (when (seq web-flush-command)
      (log/info "Web flush command is available through" (str "/" web-flush-command))
      (POST (str "/" web-flush-command) [] 
            (async/go (store/flush :web))
            (response/response "")))))

(def app 
  (do
    ; if called as 'lein ring server-headless' (so skipping the -main), we need to initialize the storage as well
    (force store/init)
    (->  main-routes
        (wrap-json-body {:keywords? true}))))
