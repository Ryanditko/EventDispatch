(ns event-dispatch.domain.models.notification
  (:import [java.util UUID]
           [java.time Instant]))

(defrecord Notification
  [id recipient message type status created-at])

(defn create-notification
  "Create a new notification"
  [recipient message type]
  (->Notification
   (str (UUID/randomUUID))
   recipient
   message
   type
   "pending"
   (Instant/now)))

(defn mark-as-delivered
  "Mark notification as delivered"
  [notification]
  (assoc notification :status "delivered"))

(defn mark-as-failed
  "Mark notification as failed"
  [notification reason]
  (assoc notification :status "failed" :reason reason))