(ns event-dispatch.domain.logic.notification-logic
  (:require [event-dispatch.domain.models.notification :as notification-model]
            [event-dispatch.domain.models.event :as event-model]
            [clojure.tools.logging :as log]))

(defn create-and-publish-notification
  "Business logic: create notification and publish event"
  [repository publisher request]
  (try
    (let [notification (notification-model/create-notification
                        (:recipient request)
                        (:message request)
                        (:type request))
          saved ((:save repository) notification)
          event (event-model/create-event (:id saved) "created" {:notification saved})]
      ((:publish publisher) event)
      {:id (:id saved)
       :status (:status saved)
       :created-at (:created-at saved)})
    (catch Exception e
      (log/error (str "Error in notification logic: " (.getMessage e)))
      {:error (.getMessage e)})))

(defn retrieve-notification
  "Business logic: retrieve notification by id"
  [repository id]
  (try
    (let [notification ((:find-by-id repository) id)]
      (if notification
        notification
        {:error "Notification not found"}))
    (catch Exception e
      (log/error (str "Error retrieving notification: " (.getMessage e)))
      {:error (.getMessage e)})))

(defn list-notifications
  "Business logic: list all notifications"
  [repository limit offset]
  (try
    ((:list-all repository) limit offset)
    (catch Exception e
      (log/error (str "Error listing notifications: " (.getMessage e)))
      {:error (.getMessage e)})))
