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

(def ^:private local-repo-path (str (env/get "HOME") File/separator ".marketing"))

(def ^:private store-file (str local-repo-path (File/separator) "emails.txt"))

(def ^:private repo (delay (if (.exists (io/file local-repo-path))
                   (load-repo local-repo-path)
                   (:repo (git-clone-full (env/repo-url) local-repo-path)))))

(def store-chan (async/chan 100))

(def ^{:doc "In-memory emails storage for collecting 'working' emails until they are flushed into the persistence Git-based storage"} 
  memory-emails (atom #{}))

(def ^:private running? (atom true))

(def ^:private flush-running? (atom false))

(defn- git-store [emails reason]
  (locking store-file
    (spit store-file (clojure.string/join "\n" emails))
    (git-add @repo "emails.txt")
    (let [msg (str "Store " (count emails) " collected emails (" reason ").")]
      (git-commit @repo msg {:name "Marketing Bot" :email ""}))
    (let [push-cmd (.push @repo)
          dry-run? (not (zero? (env/flush-dry-run)))]
      (when-not dry-run? 
        (.call push-cmd)))))

(defn flush 
  "Flush in-memory emails into file and git store them,
  reason is just an arbitrary keyword to describe the reason of the flush,
  available values so far are: :web, :scheduler, :shutdown, :threshold."
  [reason]
  (when-not @flush-running?
    (log/info "Flushing data" reason)
    ; only flush if there is something to flush
    (if (seq @memory-emails)
      (locking store-file
        (when @running?
          (try
            (reset! flush-running? true)
            (force repo)
            (let [stored-emails (if (.exists (io/file store-file)) 
                                  (clojure.string/split (slurp store-file) #"\n")
                                  [])
                  new-emails @memory-emails
                  emails-to-store (-> (concat stored-emails new-emails)
                                      distinct
                                      sort)]
              (if-not (= (count emails-to-store) (count stored-emails))
                (do 
                  (git-store emails-to-store reason)
                  (log/info "Data is flushed"))
                (log/info "Nothing to flush, no new unique emails"))
              ; clean up flushed emails from memory-emails
              (swap! memory-emails #(apply disj % new-emails)))
            (finally (reset! flush-running? false)))))
      (log/info "Nothing to flush, memory emails is empty"))))

(defn- register-shutdown-hook []
  (log/info "Register shutdown hook")
  (.addShutdownHook (Runtime/getRuntime)  
                    (Thread. #(do 
                                (log/info "Shutdown...")
                                ; waiting for any currently running store call to finish
                                (locking store-file
                                  (flush :shutdown)
                                  (reset! running? false))))))

(defn- start-flush-scheduler []
  (async/thread
    (log/info "Flush scheduler started")
    (loop [last-flushed-at (LocalDateTime/now)]
      (when @running?
        (let [now (LocalDateTime/now)
              elapsed-mins-since-last-flush (.between ChronoUnit/MINUTES last-flushed-at now)
              sleep-mins (max 0 (- (env/flush-interval-mins) elapsed-mins-since-last-flush))]
          (log/trace "Elapsed time since last flush" elapsed-mins-since-last-flush "mins")
          (log/trace "Sleeping for" sleep-mins "minutes")
          (.sleep TimeUnit/MINUTES sleep-mins)
          (recur
            (if (zero? sleep-mins)
              (do
                (flush :scheduler)
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

(defn- flush-on-threshold [key _ _ emails]
  (when (>= (count emails) (env/flush-threshold))
    (log/debug "Threshold is reached, flushing")
    (async/go (flush :threshold))))

(add-watch memory-emails ::threshold flush-on-threshold)
