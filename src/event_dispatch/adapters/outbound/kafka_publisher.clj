(ns event-dispatch.adapters.outbound.kafka-publisher(ns event-dispatch.adapters.outbound.kafka-publisher)
  (:require [event-dispatch.ports.event-publisher-port :as port]
            [cheshire.core :as json]
            [clojure.tools.logging :as log])
  (:import [org.apache.kafka.clients.producer KafkaProducer ProducerRecord]
           [java.util Properties]))

(deftype KafkaEventPublisher [producer topic]
  port/EventPublisher
  (publish [this event]
    (log/debug (str "Publishing event: " (:id event)))
    (let [record (ProducerRecord. topic
                                 (:notification-id event)
                                 (json/generate-string event))]
      (.send producer record)
      (log/info (str "Event published: " (:id event)))
      event))

  (publish-batch [this events]
    (log/debug (str "Publishing " (count events) " events"))
    (doseq [event events]
      (port/publish this event))
    (log/info (str "Batch of " (count events) " events published"))))

(defn create-kafka-producer
  "Create a Kafka producer with the given configuration"
  [bootstrap-servers]
  (let [props (Properties.)]
    (.put props "bootstrap.servers" bootstrap-servers)
    (.put props "key.serializer" "org.apache.kafka.common.serialization.StringSerializer")
    (.put props "value.serializer" "org.apache.kafka.common.serialization.StringSerializer")
    (KafkaProducer. props)))

(defn create-kafka-publisher
  "Create a Kafka-based event publisher"
  [bootstrap-servers topic]
  (let [producer (create-kafka-producer bootstrap-servers)]
    (KafkaEventPublisher. producer topic)))

(defn close-publisher
  "Close the Kafka publisher"
  [publisher]
  (.close (:producer publisher))
  (log/info "Kafka publisher closed"))
