(ns event-dispatch.core
  (:require [event-dispatch.config.loader :as config]
            [event-dispatch.adapters.inbound.http-handler :as handler]
            [event-dispatch.adapters.outbound.datomic-repository :as datomic-repo]
            [event-dispatch.adapters.outbound.kafka-publisher :as kafka-pub]
            [ring.adapter.jetty :as jetty]
            [clojure.tools.logging :as log]
            [datomic.client.api :as d])
  (:gen-class))

(defonce datomic-client (atom nil))
(defonce datomic-conn (atom nil))
(defonce kafka-publisher (atom nil))
(defonce http-server (atom nil))

(defn initialize-datomic
  "Initialize Datomic client and database"
  [_config]
  (try
    (log/info "Initializing Datomic...")
    (let [client (d/client {:server-type :peer-server
                           :system "datomic-system"})]
      (reset! datomic-client client)
      
      ;; Create database if it doesn't exist
      (try
        (d/create-database client {:db-name "eventdispatch"})
        (log/info "Database created or already exists")
        (catch Exception e
          (log/debug (str "Database creation info: " (.getMessage e)))))
      
      ;; Get connection
      (let [conn (d/connect client {:db-name "eventdispatch"})]
        (reset! datomic-conn conn)
        
        ;; Initialize schema
        (datomic-repo/initialize-schema conn)
        (log/info "Datomic initialized successfully")))
    (catch Exception e
      (log/error (str "Error initializing Datomic: " (.getMessage e)))
      (throw e))))

(defn initialize-kafka
  "Initialize Kafka publisher"
  [config]
  (try
    (log/info "Initializing Kafka...")
    (let [kafka-config (get config :kafka)
          bootstrap-servers (:bootstrap-servers kafka-config)
          topic (:topic kafka-config)
          publisher (kafka-pub/->publisher bootstrap-servers topic)]
      (reset! kafka-publisher publisher)
      (log/info "Kafka initialized successfully"))
    (catch Exception e
      (log/error (str "Error initializing Kafka: " (.getMessage e)))
      (throw e))))

(defn create-context
  "Create application context with all adapters"
  []
  (let [repo (datomic-repo/->repository @datomic-conn)
        pub @kafka-publisher]
    {:repository repo
     :publisher pub
     :config-atom (atom {})}))

(defn start-server
  "Start the HTTP server"
  [config]
  (try
    (let [server-config (get config :server)
          port (:port server-config)
          host (:host server-config)
          context (create-context)
          wrapped-app (handler/wrap-application (handler/create-routes context))]
      (log/info (str "Starting HTTP server on " host ":" port))
      (let [server (jetty/run-jetty wrapped-app {:port port
                                                  :host host
                                                  :join? false})]
        (reset! http-server server)
        (log/info "HTTP server started successfully")
        server))
    (catch Exception e
      (log/error (str "Error starting HTTP server: " (.getMessage e)))
      (throw e))))

(defn shutdown
  "Gracefully shutdown the application"
  []
  (try
    (log/info "Shutting down EventDispatch...")
    
    (when @http-server
      (log/info "Stopping HTTP server...")
      (.stop @http-server)
      (reset! http-server nil))
    
    (when @kafka-publisher
      (log/info "Closing Kafka publisher...")
      ((:close @kafka-publisher))
      (reset! kafka-publisher nil))
    
    (when @datomic-conn
      (log/info "Closing Datomic connection...")
      (reset! datomic-conn nil))
    
    (when @datomic-client
      (log/info "Releasing Datomic client...")
      (reset! datomic-client nil))
    
    (log/info "Shutdown complete")
    (catch Exception e
      (log/error (str "Error during shutdown: " (.getMessage e))))))

(defn -main
  "Application entry point"
  []
  (log/info "Starting EventDispatch service...")
  (try
    (let [app-config (config/load-config)]
      (log/info "Configuration loaded successfully")
      
      ;; Initialize all components
      (initialize-datomic app-config)
      (initialize-kafka app-config)
      
      ;; Start HTTP server
      (start-server app-config)
      
      ;; Add shutdown hook
      (.addShutdownHook (Runtime/getRuntime)
                       (Thread. shutdown))
      
      (log/info "EventDispatch service started successfully")
      
      ;; Keep the application running
      @(promise))
    (catch Exception e
      (log/error (str "Fatal error: " (.getMessage e)))
      (shutdown)
      (System/exit 1))))
