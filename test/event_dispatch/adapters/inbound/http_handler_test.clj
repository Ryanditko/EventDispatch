(ns event-dispatch.adapters.inbound.http-handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [event-dispatch.adapters.inbound.http-handler :as handler]
            [ring.mock.request :as mock]
            [cheshire.core :as json]))

(deftest health-check-test
  (testing "Health check endpoint returns 200 OK"
    (let [response (handler/health-check (mock/request :get "/health"))]
      (is (= 200 (:status response)))
      (is (= "application/json" (get-in response [:headers "Content-Type"]))))))

(deftest health-check-response-body-test
  (testing "Health check response contains status ok"
    (let [response (handler/health-check (mock/request :get "/health"))
          body (json/parse-string (:body response) true)]
      (is (= "ok" (:status body))))))

(deftest not-found-test
  (testing "Not found endpoint returns 404"
    (let [response (handler/not-found (mock/request :get "/unknown"))]
      (is (= 404 (:status response)))
      (is (= "application/json" (get-in response [:headers "Content-Type"]))))))

(deftest not-found-response-body-test
  (testing "Not found response contains error message"
    (let [response (handler/not-found (mock/request :get "/unknown"))
          body (json/parse-string (:body response) true)]
      (is (= "Not found" (:error body))))))

(deftest wrap-error-handling-test
  (testing "Error handling middleware catches exceptions"
    (let [handler-fn (fn [_] (throw (Exception. "Test error")))
          wrapped (handler/wrap-error-handling handler-fn)
          response (wrapped (mock/request :get "/test"))]
      (is (= 500 (:status response)))
      (is (= "application/json" (get-in response [:headers "Content-Type"]))))))
