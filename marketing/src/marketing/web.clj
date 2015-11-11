(ns marketing.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as response]
            [ring.middleware.json :refer [wrap-json-body]]
            [clojure.core.async :as async]
            [marketing.store :as store]))

(defroutes main-routes
  (GET "/" [] (response/resource-response "index.html"))
  
  (POST "/" {body :body} 
        (async/go (async/>! store/store-chan (:email body)))
        (response/response ""))
  
  (POST "/flush" (async/go store/flush))

  (route/resources "/" {:root ""})

  (route/not-found (response/resource-response "404.html")))

(def app 
  (->  main-routes
      (wrap-json-body {:keywords? true})))
