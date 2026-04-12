(ns event-dispatch.domain.models.event-test
  (:require [clojure.test :refer [deftest is testing]]
            [event-dispatch.domain.models.event :as event]))

(deftest create-event-test
  (testing "Creating a new event"
    (let [notification-id "550e8400-e29b-41d4-a716-446655440000"
          details {:key "value"}
          evt (event/create-event notification-id "created" details)]
      (is (some? (:id evt)))
      (is (= notification-id (:notification-id evt)))
      (is (= "created" (:event-type evt)))
      (is (= details (:details evt)))
      (is (some? (:timestamp evt))))))

(deftest event-has-unique-id-test
  (testing "Each event gets a unique ID"
    (let [id "550e8400-e29b-41d4-a716-446655440000"
          event1 (event/create-event id "created" {})
          event2 (event/create-event id "created" {})]
      (is (not= (:id event1) (:id event2))))))

(deftest event-types-test
  (testing "Different event types can be created"
    (let [id "550e8400-e29b-41d4-a716-446655440000"
          created (event/create-event id "created" {})
          updated (event/create-event id "updated" {})
          delivered (event/create-event id "delivered" {})]
      (is (= "created" (:event-type created)))
      (is (= "updated" (:event-type updated)))
      (is (= "delivered" (:event-type delivered))))))

(deftest event-preserves-details-test
  (testing "Event details are preserved"
    (let [id "550e8400-e29b-41d4-a716-446655440000"
          details {:status "sent" :attempt 1 :error nil}
          evt (event/create-event id "sent" details)]
      (is (= details (:details evt))))))

(deftest event-fields-test
  (testing "All event fields are present"
    (let [evt (event/create-event "some-id" "created" {})]
      (is (contains? evt :id))
      (is (contains? evt :notification-id))
      (is (contains? evt :event-type))
      (is (contains? evt :timestamp))
      (is (contains? evt :details)))))
