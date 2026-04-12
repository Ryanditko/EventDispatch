(ns event-dispatch.integration.http-integration-test
  (:require [clojure.test :refer [deftest is testing]]
            [event-dispatch.adapters.inbound.http-handler :as handler]
            [ring.mock.request :as mock]
            [cheshire.core :as json]))

(defn mock-context
  "Create mock context with in-memory repository and publisher"
  []
  (let [notifications (atom {})
        events (atom [])
        
        repository {:save (fn [n]
                           (swap! notifications assoc (:id n) n)
                           n)
                   :find-by-id (fn [id]
                                (@notifications id))
                   :list-all (fn [limit offset]
                              (vec (drop offset (take (+ limit offset)
                                                      (vals @notifications)))))}
        
        publisher {:publish (fn [e]
                             (swap! events conj e)
                             e)
                  :flush (fn [] true)
                  :close (fn [] true)}]
    {:repository repository
     :publisher publisher
     :notifications notifications
     :events events}))

(deftest create-notification-integration-test
  (testing "Full flow: Create notification via HTTP"
    (let [ctx (mock-context)
          routes (handler/create-routes (:repository ctx) (:publisher ctx))
          request (mock/request :post "/notifications"
                               (json/generate-string 
                                {:recipient "user@example.com"
                                 :message "Hello"
                                 :type "email"}))
          response (routes request)]
      (is (= 201 (:status response)))
      (let [body (json/parse-string (:body response) true)]
        (is (some? (:id body)))
        (is (= "pending" (:status body)))))))

(deftest get-notification-integration-test
  (testing "Full flow: Create then retrieve notification"
    (let [ctx (mock-context)
          routes (handler/create-routes (:repository ctx) (:publisher ctx))
          
          ;; Create notification
          create-req (mock/request :post "/notifications"
                                  (json/generate-string 
                                   {:recipient "user@example.com"
                                    :message "Hello"
                                    :type "email"}))
          create-resp (routes create-req)
          created (json/parse-string (:body create-resp) true)
          notification-id (:id created)
          
          ;; Retrieve notification
          get-req (mock/request :get (str "/notifications/" notification-id))
          get-resp (routes get-req)]
      (is (= 200 (:status get-resp)))
      (let [body (json/parse-string (:body get-resp) true)]
        (is (= notification-id (:id body)))
        (is (= "user@example.com" (:recipient body)))))))

(deftest list-notifications-integration-test
  (testing "Full flow: Create multiple and list notifications"
    (let [ctx (mock-context)
          routes (handler/create-routes (:repository ctx) (:publisher ctx))]
      
      ;; Create 3 notifications
      (dotimes [i 3]
        (let [req (mock/request :post "/notifications"
                               (json/generate-string 
                                {:recipient (str "user" i "@example.com")
                                 :message (str "Message " i)
                                 :type "email"}))
              resp (routes req)]
          (is (= 201 (:status resp)))))
      
      ;; List notifications
      (let [list-req (mock/request :get "/notifications?limit=10&offset=0")
            list-resp (routes list-req)]
        (is (= 200 (:status list-resp)))
        (let [body (json/parse-string (:body list-resp) true)]
          (is (= 3 (count (:data body))))
          (is (= 10 (:limit body)))
          (is (= 0 (:offset body))))))))

(deftest error-handling-integration-test
  (testing "Full flow: Error handling for invalid request"
    (let [ctx (mock-context)
          routes (handler/create-routes (:repository ctx) (:publisher ctx))
          
          ;; Missing required field
          req (mock/request :post "/notifications"
                           (json/generate-string 
                            {:recipient "user@example.com"}))
          resp (routes req)]
      (is (= 400 (:status resp)))
      (let [body (json/parse-string (:body resp) true)]
        (is (contains? body :error))))))

(deftest health-check-integration-test
  (testing "Health check endpoint works"
    (let [ctx (mock-context)
          routes (handler/create-routes (:repository ctx) (:publisher ctx))
          req (mock/request :get "/health")
          resp (routes req)]
      (is (= 200 (:status resp)))
      (let [body (json/parse-string (:body resp) true)]
        (is (= "ok" (:status body)))))))
