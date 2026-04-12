(ns event-dispatch.adapters.outbound.datomic-repository(ns event-dispatch.adapters.outbound.datomic-repository)
  (:require [event-dispatch.ports.notification-repository-port :as port]
            [clojure.tools.logging :as log]
            [datomic.client.api :as d]))

(deftype DatomicNotificationRepository [conn]
  port/NotificationRepository
  (save [this notification]
    (log/debug (str "Saving notification: " (:id notification)))
    (let [db (d/db conn)
          result (d/transact conn {:tx-data [(assoc notification :db/id "datomic.entity")]})
          tx-result (:db-after result)]
      (log/info (str "Notification saved: " (:id notification)))
      notification))

  (find-by-id [this id]
    (log/debug (str "Finding notification: " id))
    (let [db (d/db conn)
          result (d/q '[:find (pull ?e [*]) :where [?e :id ?id]] db {:id id})]
      (first (flatten result))))

  (list-all [this limit offset]
    (log/debug (str "Listing notifications - limit: " limit ", offset: " offset))
    (let [db (d/db conn)
          result (d/q '[:find (pull ?e [*]) :where [?e :id]] db)]
      (vec (drop offset (take (+ limit offset) result))))))

(defn create-datomic-repository
  "Create a Datomic-based notification repository"
  [conn]
  (DatomicNotificationRepository. conn))
