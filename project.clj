(defproject event-dispatch "0.1.0-SNAPSHOT"
  :description "Event-driven notification orchestration service built with Clojure, Kafka, and Datomic"
  :url "https://github.com/Ryanditko/EventDispatch"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.logging "1.2.4"]
                 [ch.qos.logback/logback-classic "1.4.11"]
                 [ring/ring-core "1.10.0"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [compojure "1.7.0"]
                 [org.apache.kafka/kafka-clients "3.6.0"]
                 [com.datomic/client-pro "1.0.71"]
                 [cheshire "5.11.0"]
                 [aero "1.1.6"]
                 [integrant "0.9.0"]
                 [prismatic/schema "1.4.1"]]
  :main event-dispatch.core
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[midje "1.10.9"]
                                  [ring/ring-mock "0.4.0"]]
                   :plugins [[lein-midje "3.2.2"]]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
