(ns event-dispatch.boundary.wire.schemas.notification-schema-test
  (:require [clojure.test :refer [deftest is testing]]
            [event-dispatch.boundary.wire.schemas.notification-schema :as schema]
            [schema.core :as s]))

(deftest notification-create-schema-test
  (testing "Valid notification create schema"
    (let [data {:recipient "user@example.com"
                :message "Test message"
                :type "email"}
          result (s/validate schema/notification-create-schema data)]
      (is (= data result)))))

(deftest notification-create-schema-missing-field-test
  (testing "Invalid notification create schema - missing field"
    (let [data {:recipient "user@example.com"
                :message "Test message"}]
      (is (thrown? Exception (s/validate schema/notification-create-schema data))))))

(deftest notification-create-schema-extra-fields-test
  (testing "Valid notification create schema - extra fields ignored"
    (let [data {:recipient "user@example.com"
                :message "Test message"
                :type "email"
                :extra-field "ignored"}
          result (s/validate schema/notification-create-schema data)]
      (is (contains? result :recipient))
      (is (contains? result :message))
      (is (contains? result :type)))))

(deftest notification-response-schema-test
  (testing "Valid notification response schema"
    (let [now (java.util.Date.)
          data {:id "123"
                :recipient "user@example.com"
                :message "Test"
                :type "email"
                :status "pending"
                :created-at now}
          result (s/validate schema/notification-response-schema data)]
      (is (= data result)))))

(deftest notification-list-response-schema-test
  (testing "Valid notification list response schema"
    (let [now (java.util.Date.)
          data {:total 1
                :limit 10
                :offset 0
                :results [{:id "1"
                          :recipient "test@example.com"
                          :message "Test"
                          :type "email"
                          :status "pending"
                          :created-at now}]}
          result (s/validate schema/notification-list-response-schema data)]
      (is (= data result)))))
