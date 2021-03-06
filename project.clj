(defproject marketing "1.0.0"
  :description "Collect potential users emails"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.2.371"]
                 [compojure "1.4.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [clj-jgit "0.8.8"]
                 [org.slf4j/slf4j-simple "1.7.13"]]
  :main ^:skip-aot marketing.main
  :target-path "target/%s"
  :plugins [[lein-ring "0.8.11"]]
  :profiles {:uberjar {:aot :all}}
  :ring {:handler marketing.web/app})
