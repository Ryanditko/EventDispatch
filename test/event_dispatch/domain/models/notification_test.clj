(ns event-dispatch.domain.models.notification-test
  (:require [clojure.test :refer [deftest is testing]]
            [event-dispatch.domain.models.notification :as notification]))

(deftest create-notification-test
  (testing "Creating a new notification"
    (let [notification (notification/create-notification 
                        "user@example.com"
                        "Hello World"
                        "email")]
      (is (some? (:id notification)))
      (is (= "user@example.com" (:recipient notification)))
      (is (= "Hello World" (:message notification)))
      (is (= "email" (:type notification)))
      (is (= "pending" (:status notification)))
      (is (some? (:created-at notification))))))

(deftest mark-as-delivered-test
  (testing "Mark notification as delivered"
    (let [notification (notification/create-notification 
                        "user@example.com"
                        "Hello"
                        "email")
          delivered (notification/mark-as-delivered notification)]
      (is (= "delivered" (:status delivered)))
      (is (= (:id notification) (:id delivered))))))

(deftest mark-as-failed-test
  (testing "Mark notification as failed"
    (let [notification (notification/create-notification 
                        "user@example.com"
                        "Hello"
                        "email")
          failed (notification/mark-as-failed notification "Connection timeout")]
      (is (= "failed" (:status failed)))
      (is (= "Connection timeout" (:reason failed)))
      (is (= (:id notification) (:id failed))))))

(deftest notification-fields-test
  (testing "All notification fields are present"
    (let [notification (notification/create-notification
                        "test@example.com"
                        "Test message"
                        "sms")]
      (is (contains? notification :id))
      (is (contains? notification :recipient))
      (is (contains? notification :message))
      (is (contains? notification :type))
      (is (contains? notification :status))
      (is (contains? notification :created-at)))))

(deftest notification-is-immutable-test
  (testing "Notifications are immutable (original unmodified)"
    (let [original (notification/create-notification 
                    "user@example.com"
                    "Original"
                    "email")
          modified (notification/mark-as-delivered original)]
      (is (= "pending" (:status original)))
      (is (= "delivered" (:status modified))))))
