(ns marketing.store
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async]
            [marketing.env :as env]
            [clj-jgit.porcelain :refer :all])
  (:import (java.util.concurrent TimeUnit)))

(defonce repo (let [path (env/get "HOME") ".marketing"] 
                (if (.exists (io/file path))
                  (load-repo path)
                  (git-clone-full (env/repo) path))))

; verify that reopening the file keeps the old content
(defonce emails 
  (let [path (env/store)]
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

(defn flush []
  (println "Flushing data")
  (locking emails
    ()  
    )
  )
