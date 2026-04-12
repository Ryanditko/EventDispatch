(ns event-dispatch.adapters.inbound.http-handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [event-dispatch.domain.models.notification :as notification]
            [event-dispatch.domain.models.event :as event]))

(defn create-notification-handler
  "Create handler for creating notifications"
  [repository publisher]
  (fn [request]
    (try
      (let [body (json/parse-string (slurp (:body request)) true)
            notification (notification/create-notification
                          (:recipient body)
                          (:message body)
                          (:type body))
            saved ((:save repository) repository notification)
            evt (event/create-event (:id notification) "created" {:notification saved})]
        ((:publish publisher) publisher evt)
        {:status 201
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string saved)})
      (catch Exception e
        (log/error (str "Error creating notification: " (.getMessage e)))
        {:status 500
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string {:error "Internal server error"})}))))

(defn health-check
  "Health check endpoint"
  [_request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string {:status "ok"})})

(defn not-found
  "Handle not found requests"
  [_request]
  {:status 404
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string {:error "Not found"})})

(defn create-routes
  "Create application routes"
  [repository publisher]
  (defroutes app-routes
    (GET "/health" [] health-check)
    (POST "/notifications" [] (create-notification-handler repository publisher))
    (route/not-found not-found)))
