(ns event-dispatch.core
  (:require [event-dispatch.config.loader :as config]
            [event-dispatch.adapters.inbound.http-handler :as handler]
            [event-dispatch.adapters.outbound.datomic-repository :as datomic-repo]
            [event-dispatch.adapters.outbound.kafka-publisher :as kafka-pub]
            [ring.adapter.jetty :as jetty]
            [clojure.tools.logging :as log]
            [datomic.client.api :as d])
  (:gen-class))

(defn start-server
  "Start the HTTP server"
  [app config]
  (let [server-config (get config :server)
        port (:port server-config)
        host (:host server-config)]
    (log/info (str "Starting server on " host ":" port))
    (jetty/run-jetty app {:port port
                          :host host
                          :join? (:join? server-config)})))

(defn -main
  [& args]
  (log/info "Starting EventDispatch service...")
  (let [app-config (config/load-config)]
    (log/info "Configuration loaded successfully")
    (start-server handler/app-routes app-config)))
