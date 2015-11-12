(ns marketing.store
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async]
            [marketing.env :as env]
            [clj-jgit.porcelain :refer :all])
  (:import (java.util.concurrent TimeUnit)))

(defonce local-repo-path (str (env/get "HOME") java.io.File/separator ".marketing"))

(defonce repo (if (.exists (io/file local-repo-path))
                  (load-repo local-repo-path)
                  (git-clone-full (env/repo) local-repo-path)))

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

; will it overwrite the existing content?
(defn flush []
  (println "Flushing data")
  (locking emails
    (.flush emails)
    (with-open [emails (io/reader (env/store))]
      (let [unique-emails (-> (line-seq emails)
                              distinct
                              sort
                              )]
        (spit (str local-repo-path java.io.File/separator "emails.txt") (clojure.string/join "\n" unique-emails))
        (println (type repo))
        (git-add repo "emails.txt")
        (git-commit repo (str "Added " (count unique-emails) " collected emails."))
        (git-push repo)))))
