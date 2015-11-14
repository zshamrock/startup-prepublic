(ns marketing.store
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async]
            [marketing.env :as env]
            [clj-jgit.porcelain :refer :all])
  (:import [java.util.concurrent TimeUnit]
           [java.time LocalDateTime]
           [java.time.temporal ChronoUnit]))

(defonce local-repo-path (str (env/get "HOME") java.io.File/separator ".marketing"))

(defonce repo (if (.exists (io/file local-repo-path))
                  (load-repo local-repo-path)
                  (git-clone-full (env/repo) local-repo-path)))

; verify that reopening the file keeps the old content
(defonce emails 
  (let [path (env/store)]
    (io/writer path)))

(def running? (atom true))

(defn- register-shutdown-hook []
  (.addShutdownHook (Runtime/getRuntime)  
    (Thread. #(do (
                   (print "Closing emails store...")
                   (reset! running? false)
                   (locking emails
                     ; call store/flush
                     (doto emails
                       .flush
                       .close)))
                  (println "emails store is closed!")))))

(register-shutdown-hook)

(defn- start-flush-scheduler []
  (async/thread 
    (loop [flushed-at (LocalDateTime/now)]
      (when @running?
        (let [elapsed-mins-from-last-flush (.between ChronoUnit/MINUTES (LocalDateTime/now) flushed-at)
              sleep-mins (max 0 (- (env/flush-interval-mins) elapsed-mins-from-last-flush))]
          (.sleep TimeUnit/MINUTES sleep-mins)
          (recur
            (if (zero? sleep-mins)
              (do
                (flush)
                (LocalDateTime/now)))
            flushed-at))))))

(start-flush-scheduler)

(defonce store-chan (async/chan 100))

(defn- store-email [email]
  (locking emails
    (doto emails
      (.write email)
      .newLine)))

(defn- start-go [num]
  (dotimes [_ num]
    (async/go (while @running?
                (let [email (async/<! store-chan)] 
                  (store-email email)))))
  (println "All go workers are started."))

(start-go 5)

; will it overwrite the existing content?
; protect flush from being called multiple only once per configured interval?
(defn flush []
  (println "Flushing data")
  (locking emails
    (.flush emails)
    (with-open [emails (io/reader (env/store))]
      (let [emails-path (str local-repo-path java.io.File/separator "emails.txt")
            unique-emails (-> (concat (line-seq emails) (clojure.string/split (slurp emails-path) #"\n"))
                              distinct
                              sort)]
        (spit (clojure.string/join "\n" unique-emails))
        (println (type repo))
        ; do add, commit and push only if there is a difference between what there is currently in file and the new content
        (git-add repo "emails.txt")
        (git-commit repo (str "Added " (count unique-emails) " collected emails."))
        (let [push-cmd (.push repo)]
          (-> push-cmd
            (.setDryRun true)
            (.call)))))))
