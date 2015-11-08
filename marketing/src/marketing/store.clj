(ns marketing.store
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async])
  (:import (java.util.concurrent TimeUnit)))


(defonce emails 
  (let [path (or 
               (System/getenv "MARKETING_STORE") 
               "/var/vautoservice/emails.txt")]
    (io/writer path)))

(def open? (atom true))

(defn- register-shutdown-hook []
  (.addShutdownHook (Runtime/getRuntime)  
    (Thread. #(do (
                   (print "Closing emails store...")
                   (reset! open? false)
                   (locking emails
                     (doto emails
                       .flush
                       .close)))
                  (println "emails store is closed!")))))

(register-shutdown-hook)

(defonce store-chan (async/chan 100))

(defn- store-email [email]
  (locking emails
    (println "Ready to store" email)
    (doto emails
      (.write email)
      .newLine)))

(defn- start-go [num]
  (dotimes [_ num]
    (async/go (while @open? 
                (let [email (async/<! store-chan)] 
                  (store-email email)))))
  (println "All go workers are started."))

(start-go 5)
