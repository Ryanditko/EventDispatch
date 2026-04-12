(ns event-dispatch.domain.controllers.notification-controller
  (:require [event-dispatch.domain.logic.notification-logic :as logic]
            [clojure.tools.logging :as log]))

(defn create-notification
  "Controller: handle notification creation"
  [context recipient message type]
  (log/info (str "Creating notification for: " recipient))
  (let [repository (:repository context)
        publisher (:publisher context)
        request {:recipient recipient
                 :message message
                 :type type}]
    (logic/create-and-publish-notification repository publisher request)))

(defn get-notification
  "Controller: handle get notification request"
  [context id]
  (log/info (str "Fetching notification: " id))
  (let [repository (:repository context)]
    (logic/retrieve-notification repository id)))

(defn list-notifications
  "Controller: handle list notifications request"
  [context limit offset]
  (log/info (str "Listing notifications - limit: " limit ", offset: " offset))
  (let [repository (:repository context)]
    (logic/list-notifications repository limit offset)))
