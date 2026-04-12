(ns event-dispatch.adapters.outbound.datomic-repository-test
  (:require [clojure.test :refer [deftest is testing]]
            [event-dispatch.adapters.outbound.datomic-repository :as repo]))

(deftest notification-schema-definition-test
  (testing "Notification schema is properly defined"
    (is (vector? repo/notification-schema))
    (is (> (count repo/notification-schema) 0))
    (let [schema repo/notification-schema
          idents (map :db/ident schema)]
      (is (some #(= :notification/id %) idents))
      (is (some #(= :notification/recipient %) idents))
      (is (some #(= :notification/message %) idents))
      (is (some #(= :notification/type %) idents))
      (is (some #(= :notification/status %) idents))
      (is (some #(= :notification/created-at %) idents))
      (is (some #(= :notification/updated-at %) idents)))))

(deftest repository-map-structure-test
  (testing "Repository map has required operations"
    (let [mock-conn (atom nil)
          repo-map (repo/->repository mock-conn)]
      (is (contains? repo-map :save))
      (is (contains? repo-map :find-by-id))
      (is (contains? repo-map :list-all))
      (is (fn? (:save repo-map)))
      (is (fn? (:find-by-id repo-map)))
      (is (fn? (:list-all repo-map))))))
