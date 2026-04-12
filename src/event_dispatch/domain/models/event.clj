(ns event-dispatch.domain.models.event
  (:import [java.util UUID]
           [java.time Instant]))

(defrecord NotificationEvent
  [id notification-id event-type timestamp details])

(defn create-event
  "Create a new notification event"
  [notification-id event-type details]
  (->NotificationEvent
   (str (UUID/randomUUID))
   notification-id
   event-type
   (Instant/now)
   details))
