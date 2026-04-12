(ns event-dispatch.domain.logic.notification-logic-test
  (:require [clojure.test :refer [deftest is testing]]
            [event-dispatch.domain.logic.notification-logic :as logic]))

(defn mock-repository
  "Create a mock repository for testing"
  []
  {:save (fn [notification]
           (assoc notification :persisted true))
   
   :find-by-id (fn [id]
                 (when (= id "existing-id")
                   {:id id :status "pending" :recipient "test@example.com"}))
   
   :list-all (fn [_limit _offset]
               [{:id "1" :status "pending"}
                {:id "2" :status "delivered"}
                {:id "3" :status "failed"}])})

(defn mock-publisher
  "Create a mock publisher for testing"
  []
  {:publish (fn [event]
              (assoc event :published true))})

(deftest create-and-publish-notification-test
  (testing "Creating and publishing a notification"
    (let [repo (mock-repository)
          pub (mock-publisher)
          request {:recipient "user@example.com"
                   :message "Test message"
                   :type "email"}
          result (logic/create-and-publish-notification repo pub request)]
      (is (some? (:id result)))
      (is (= "pending" (:status result)))
      (is (some? (:created-at result))))))

(deftest create-and-publish-notification-error-test
  (testing "Error handling when creating and publishing"
    (let [broken-repo {:save (fn [_] (throw (Exception. "Save failed")))}
          pub (mock-publisher)
          request {:recipient "user@example.com"
                   :message "Test"
                   :type "email"}
          result (logic/create-and-publish-notification broken-repo pub request)]
      (is (contains? result :error)))))

(deftest retrieve-notification-test
  (testing "Retrieving an existing notification"
    (let [repo (mock-repository)
          result (logic/retrieve-notification repo "existing-id")]
      (is (= "existing-id" (:id result)))
      (is (= "pending" (:status result))))))

(deftest retrieve-notification-not-found-test
  (testing "Retrieving a non-existent notification"
    (let [repo (mock-repository)
          result (logic/retrieve-notification repo "non-existent")]
      (is (contains? result :error)))))

(deftest retrieve-notification-error-test
  (testing "Error handling when retrieving notification"
    (let [broken-repo {:find-by-id (fn [_] (throw (Exception. "Query failed")))}
          result (logic/retrieve-notification broken-repo "some-id")]
      (is (contains? result :error)))))

(deftest list-notifications-test
  (testing "Listing notifications"
    (let [repo (mock-repository)
          result (logic/list-notifications repo 10 0)]
      (is (vector? result))
      (is (= 3 (count result))))))

(deftest list-notifications-error-test
  (testing "Error handling when listing notifications"
    (let [broken-repo {:list-all (fn [_ _] (throw (Exception. "Query failed")))}
          result (logic/list-notifications broken-repo 10 0)]
      (is (contains? result :error)))))
