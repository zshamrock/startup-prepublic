(ns marketing.store
  (:refer-clojure :exclude [flush])
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async]
            [marketing.env :as env]
            [clojure.tools.logging :as log]
            [clj-jgit.porcelain :refer :all])
  (:import [java.util.concurrent TimeUnit]
           [java.time LocalDateTime]
           [java.time.temporal ChronoUnit]
           [java.io File]))

; http://stuartsierra.com/2015/05/27/clojure-uncaught-exceptions
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (log/error ex "Uncaught exception on" (.getName thread)))))

(def local-repo-path (str (env/get "HOME") File/separator ".marketing"))

(def store-file (str local-repo-path (File/separator) "emails.txt"))

(def repo (delay (if (.exists (io/file local-repo-path))
                   (load-repo local-repo-path)
                   (:repo (git-clone-full (env/repo-url) local-repo-path)))))

(def store-chan (async/chan 100))

(def ^{:doc "In-memory emails storage for collecting 'working' emails until they are flushed into the persistence Git-based storage"} 
  memory-emails (atom #{}))

(def running? (atom true))

(defn- git-store [emails]
  (locking store-file
    (force repo)
    (spit store-file (clojure.string/join "\n" emails))
    (git-add @repo "emails.txt")
    (git-commit @repo (str "Store " (count emails) " collected emails."))
    (let [push-cmd (.push @repo)]
      (-> push-cmd
          (.setDryRun true)
          (.call)))))

(defn flush []
  ; only flush if there is something to flush
  (log/info "Flushing data")
  (if (seq @memory-emails)
    (locking store-file
      (when @running?
        (let [stored-emails (if (or (realized? repo) (.exists (io/file store-file))) 
                              (clojure.string/split (slurp store-file) #"\n")
                              [])
              new-emails @memory-emails
              emails-to-store (-> (concat stored-emails new-emails)
                                  distinct
                                  sort)]
          (if-not (= (count emails-to-store) (count stored-emails))
            (do 
              (git-store emails-to-store)
              ; clean up flushed emails from memory-emails
              (log/info "Data is flushed"))
            (log/info "Nothing to flush, no new unique emails"))
          (swap! memory-emails #(apply disj % new-emails)))))
    (log/info "Nothing to flush, memory emails is empty")))

(defn- register-shutdown-hook []
  (log/info "Register shutdown hook")
  (.addShutdownHook (Runtime/getRuntime)  
                    (Thread. #(do 
                                (log/info "Shutdown...")
                                ; waiting for any currently running store call to finish
                                (locking store-file
                                  (flush)
                                  (reset! running? false))))))

(defn- start-flush-scheduler []
  (async/thread
    (log/info "Flush scheduler started")
    (loop [last-flushed-at (LocalDateTime/now)]
      (when @running?
        (let [now (LocalDateTime/now)
              elapsed-mins-since-last-flush (.between ChronoUnit/MINUTES last-flushed-at now)
              sleep-mins (max 0 (- (env/flush-interval-mins) elapsed-mins-since-last-flush))]
          (log/debug "Elapsed time since last flush" elapsed-mins-since-last-flush "mins")
          (log/debug "Sleeping for" sleep-mins "minutes")
          (.sleep TimeUnit/MINUTES sleep-mins)
          (recur
            (if (zero? sleep-mins)
              (do
                (flush)
                (LocalDateTime/now))
              last-flushed-at)))))))


(defn- start-go-workers [num]
  (dotimes [_ num]
    (async/go (while @running?
                (let [email (async/<! store-chan)] 
                  (swap! memory-emails conj email)))))
  (log/info "All go workers are started"))

(defn- repo-defined? []
  (or (.exists (io/file local-repo-path))
      (env/repo-url)))

(def ^{:doc "Call this function to initialize all necessary routines for store operate correctly. Will terminate if no repository is defined."}
  init 
  (delay
    (when-not (repo-defined?)
      (binding [*out* *err*]
        (log/error "Repository URL where to store the data is required. Please set" (env/env-var-name env/REPO_URL) "environment variable")
        (System/exit 1)))
    (env/log-all) 
    (register-shutdown-hook)
    (start-flush-scheduler)
    (start-go-workers 5)))
