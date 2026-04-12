(ns event-dispatch.domain.controllers.notification-controller
  (:require [event-dispatch.domain.logic.notification-logic :as logic]
            [clojure.tools.logging :as log]))

(defn create-notification-handler
  "Controller: handle notification creation"
  [context request]
  (log/info "Handling create notification request")
  (let [repository (:repository context)
        publisher (:publisher context)]
    (logic/create-and-publish-notification repository publisher request)))

(defn get-notification-handler
  "Controller: handle get notification request"
  [context id]
  (log/info (str "Handling get notification request for id: " id))
  (let [repository (:repository context)]
    (logic/retrieve-notification repository id)))

(defn list-notifications-handler
  "Controller: handle list notifications request"
  [context limit offset]
  (log/info (str "Handling list notifications request - limit: " limit ", offset: " offset))
  (let [repository (:repository context)]
    (logic/list-notifications repository limit offset)))
