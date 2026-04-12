(ns event-dispatch.adapters.outbound.kafka-publisher
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log])
  (:import [org.apache.kafka.clients.producer KafkaProducer ProducerRecord Callback RecordMetadata]
           [java.util Properties]
           [java.util.concurrent Future]))

;; Kafka producer callback for handling async send results
(defn create-callback
  "Create a Kafka callback for handling send results"
  [event-id]
  (reify Callback
    (onCompletion [this metadata exception]
      (if exception
        (log/error (str "Error publishing event " event-id ": " (.getMessage exception)))
        (log/info (str "Event " event-id " published to partition "
                      (.partition ^RecordMetadata metadata)
                      " with offset " (.offset ^RecordMetadata metadata)))))))

;; Publisher operations map for dependency injection
(defn ->publisher
  [bootstrap-servers topic]
  "Create a publisher map with publish and publish-batch operations"
  (let [props (Properties.)]
    (.put props "bootstrap.servers" bootstrap-servers)
    (.put props "key.serializer" "org.apache.kafka.common.serialization.StringSerializer")
    (.put props "value.serializer" "org.apache.kafka.common.serialization.StringSerializer")
    (.put props "acks" "all")
    (.put props "retries" "3")
    (.put props "max.in.flight.requests.per.connection" "1")
    
    (let [producer (KafkaProducer. props)]
      {:publish
       (fn [event]
         (try
           (log/debug (str "Publishing event: " (:id event)))
           (let [record (ProducerRecord. topic
                                        (str (:notification-id event))
                                        (json/generate-string event))
                 callback (create-callback (:id event))]
             (.send producer record callback)
             (log/info (str "Event published: " (:id event)))
             event)
           (catch Exception e
             (log/error (str "Error publishing event: " (.getMessage e)))
             (throw e))))
       
       :publish-batch
       (fn [events]
         (try
           (log/debug (str "Publishing " (count events) " events"))
           (doseq [event events]
             (let [record (ProducerRecord. topic
                                          (str (:notification-id event))
                                          (json/generate-string event))
                   callback (create-callback (:id event))]
               (.send producer record callback)))
           (log/info (str "Batch of " (count events) " events published"))
           events)
           (catch Exception e
             (log/error (str "Error publishing batch: " (.getMessage e)))
             (throw e))))
       
       :flush
       (fn []
         (try
           (log/debug "Flushing Kafka producer")
           (.flush producer)
           (log/info "Kafka producer flushed")
           (catch Exception e
             (log/error (str "Error flushing producer: " (.getMessage e)))
             (throw e))))
       
       :close
       (fn []
         (try
           (log/debug "Closing Kafka producer")
           (.close producer)
           (log/info "Kafka producer closed")
           (catch Exception e
             (log/error (str "Error closing producer: " (.getMessage e)))
             (throw e))))
       
       :producer
       producer
       :topic
       topic})))

(defn create-kafka-publisher
  "Deprecated: Use ->publisher instead"
  [bootstrap-servers topic]
  (->publisher bootstrap-servers topic))
