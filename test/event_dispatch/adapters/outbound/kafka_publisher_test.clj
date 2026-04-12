(ns event-dispatch.adapters.outbound.kafka-publisher-test
  (:require [clojure.test :refer [deftest is testing]]
            [event-dispatch.adapters.outbound.kafka-publisher :as publisher]))

(deftest create-callback-test
  (testing "Create callback returns a Callback instance"
    (let [callback (publisher/create-callback "test-event-id")]
      (is (some? callback))
      (is (instance? org.apache.kafka.clients.producer.Callback callback)))))

(deftest publisher-map-structure-test
  (testing "Publisher map has required operations"
    (let [publisher-map (publisher/->publisher "localhost:9092" "test-topic")]
      (is (contains? publisher-map :publish))
      (is (contains? publisher-map :publish-batch))
      (is (contains? publisher-map :flush))
      (is (contains? publisher-map :close))
      (is (contains? publisher-map :producer))
      (is (contains? publisher-map :topic))
      (is (fn? (:publish publisher-map)))
      (is (fn? (:publish-batch publisher-map)))
      (is (fn? (:flush publisher-map)))
      (is (fn? (:close publisher-map)))
      (is (= "test-topic" (:topic publisher-map))))))

(deftest publisher-functions-test
  (testing "Publisher functions are callable"
    (let [pub (publisher/->publisher "localhost:9092" "test-topic")]
      (is (ifn? (:publish pub)))
      (is (ifn? (:publish-batch pub)))
      (is (ifn? (:flush pub)))
      (is (ifn? (:close pub))))))
