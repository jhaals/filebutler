(defproject filebutler "0.1.0-SNAPSHOT"
  :description "File uploading app. Just for learning"
  :url "http://github.com/jhaals"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]]
  :plugins [[lein-ring "0.8.8"]]
  :ring {:handler filebutler.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]
                        [ring/ring-json "0.2.0"]
                        [revise "0.0.5"]]}})
