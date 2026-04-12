(ns event-dispatch.adapters.inbound.http-handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [event-dispatch.domain.controllers.notification-controller :as controller]
            [event-dispatch.boundary.wire.schemas.notification-schema :as schemas]
            [schema.core :as s]))

(defn wrap-error-handling
  "Middleware to handle exceptions and return proper HTTP responses"
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch clojure.lang.ExceptionInfo e
        (let [data (ex-data e)]
          (log/warn (str "Validation error: " (.getMessage e)))
          {:status 400
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string {:error "Validation error"
                                       :details (str data)})}))
      (catch Exception e
        (log/error (str "Unexpected error: " (.getMessage e)))
        {:status 500
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string {:error "Internal server error"})}))))

(defn health-check
  "Health check endpoint"
  [_request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string {:status "ok"})})

(defn create-notification-handler
  "Create handler for creating notifications"
  [context]
  (fn [request]
    (try
      (let [body (json/parse-string (slurp (:body request)) true)
            validated (s/validate schemas/notification-create-schema body)]
        (log/info (str "Creating notification for: " (:recipient validated)))
        (let [result (controller/create-notification context
                                                     (:recipient validated)
                                                     (:message validated)
                                                     (:type validated))]
          {:status 201
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string result)}))
      (catch clojure.lang.ExceptionInfo e
        (log/warn (str "Validation error: " (.getMessage e)))
        {:status 400
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string {:error "Validation error"})})
      (catch Exception e
        (log/error (str "Error creating notification: " (.getMessage e)))
        {:status 500
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string {:error "Internal server error"})}))))

(defn get-notification-handler
  "Get a specific notification"
  [context]
  (fn [request]
    (try
      (let [id (get-in request [:route-params :id])]
        (log/debug (str "Fetching notification: " id))
        (if-let [notification (controller/get-notification context id)]
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string notification)}
          {:status 404
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string {:error "Notification not found"})}))
      (catch Exception e
        (log/error (str "Error fetching notification: " (.getMessage e)))
        {:status 500
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string {:error "Internal server error"})}))))

(defn list-notifications-handler
  "List all notifications with pagination"
  [context]
  (fn [request]
    (try
      (let [limit (Integer/parseInt (get-in request [:query-params "limit"] "10"))
            offset (Integer/parseInt (get-in request [:query-params "offset"] "0"))]
        (log/debug (str "Listing notifications - limit: " limit ", offset: " offset))
        (let [notifications (controller/list-notifications context limit offset)]
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string {:data notifications
                                       :limit limit
                                       :offset offset})}))
      (catch Exception e
        (log/error (str "Error listing notifications: " (.getMessage e)))
        {:status 500
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string {:error "Internal server error"})}))))

(defn not-found
  "Handle not found requests"
  [_request]
  {:status 404
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string {:error "Not found"})})

(defroutes app-routes
  (GET "/health" [] health-check)
  (route/not-found not-found))

(defn create-routes
  "Create application routes with context (repository and publisher)"
  [context]
  (fn [request]
    (let [routes [(GET "/health" [] health-check)
                  (POST "/notifications" [] (create-notification-handler context))
                  (GET "/notifications/:id" [] (get-notification-handler context))
                  (GET "/notifications" [] (list-notifications-handler context))
                  (route/not-found not-found)]]
      ((apply compojure.core/routes routes) request))))

(defn wrap-application
  "Wrap the application with middleware"
  [routes]
  (-> routes
      wrap-json-response
      (wrap-json-body {:keywords? true})
      wrap-error-handling))
