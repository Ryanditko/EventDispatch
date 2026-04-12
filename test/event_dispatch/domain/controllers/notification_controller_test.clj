(ns event-dispatch.domain.controllers.notification-controller-test
  (:require [clojure.test :refer [deftest is testing]]
            [event-dispatch.domain.controllers.notification-controller :as controller]))

(defn mock-context
  "Create a mock context for testing"
  []
  (let [mock-repo {:save (fn [n] (assoc n :persisted true))
                   :find-by-id (fn [id]
                                 (when (= id "existing-id")
                                   {:id id :status "pending"}))
                   :list-all (fn [_l _o]
                               [{:id "1"} {:id "2"} {:id "3"}])}
        mock-pub {:publish (fn [e] (assoc e :published true))}]
    {:repository mock-repo
     :publisher mock-pub}))

(deftest create-notification-test
  (testing "Creating notification through controller"
    (let [context (mock-context)
          result (controller/create-notification context 
                                                 "user@example.com"
                                                 "Hello"
                                                 "email")]
      (is (some? result))
      (is (some? (:id result))))))

(deftest get-notification-test
  (testing "Retrieving notification through controller"
    (let [context (mock-context)
          result (controller/get-notification context "existing-id")]
      (is (some? result))
      (is (= "existing-id" (:id result))))))

(deftest get-notification-not-found-test
  (testing "Retrieving non-existent notification"
    (let [context (mock-context)
          result (controller/get-notification context "non-existent")]
      (is (contains? result :error)))))

(deftest list-notifications-test
  (testing "Listing notifications through controller"
    (let [context (mock-context)
          result (controller/list-notifications context 10 0)]
      (is (vector? result))
      (is (> (count result) 0)))))

(deftest context-structure-test
  (testing "Context has required keys"
    (let [context (mock-context)]
      (is (contains? context :repository))
      (is (contains? context :publisher)))))
