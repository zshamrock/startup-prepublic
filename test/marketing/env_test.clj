(ns marketing.env-test
  (:require [clojure.test :refer :all]
            [marketing.env :as env]))

(deftest env-port
  (is (= 3000 (env/port))))

(deftest env-localhost
  (is (= "localhost" (env/host))))

(deftest env-repo-url
  (is (nil? (env/repo-url))
      (str (env/env-var-name env/REPO_URL) " should not have default value")))

(deftest env-web-flush-command
  (is (= "flush" (env/web-flush-command)))
  (is (= 1 (env/web-flush-command-enabled))))

(deftest env-flush-interval-mins
  (is (= 10 (env/flush-interval-mins))))

(deftest env-flush-threshold
  (is (= 10000 (env/flush-threshold))))

(deftest env-flush-dry-run
  (is (zero? (env/flush-dry-run))))

(deftest env-var-names
  (testing "Environment variables actual names"
    (are [expected actual]
         (= expected (env/env-var-name actual))
         "MARKETING_PORT" env/PORT
         "MARKETING_HOST" env/HOST
         "MARKETING_REPO_URL" env/REPO_URL
         "MARKETING_WEB_FLUSH_COMMAND" env/WEB_FLUSH_COMMAND
         "MARKETING_WEB_FLUSH_COMMAND_ENABLED" env/WEB_FLUSH_COMMAND_ENABLED
         "MARKETING_FLUSH_INTERVAL_MINS" env/FLUSH_INTERVAL_MINS
         "MARKETING_FLUSH_THRESHOLD" env/FLUSH_THRESHOLD
         "MARKETING_FLUSH_DRY_RUN" env/FLUSH_DRY_RUN)))
