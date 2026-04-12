(ns event-dispatch.adapters.outbound.datomic-repository
  (:require [datomic.client.api :as d]
            [clojure.tools.logging :as log]))

;; Datomic schema definition for notifications
(def notification-schema
  [{:db/ident :notification/id
    :db/valueType :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "Unique notification identifier"}
   
   {:db/ident :notification/recipient
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Notification recipient address"}
   
   {:db/ident :notification/message
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Notification message content"}
   
   {:db/ident :notification/type
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/doc "Notification type (email, sms, push)"}
   
   {:db/ident :notification/status
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/doc "Notification status (pending, delivered, failed)"}
   
   {:db/ident :notification/created-at
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc "Creation timestamp"}
   
   {:db/ident :notification/updated-at
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc "Last update timestamp"}])

;; Repository operations map for dependency injection
;; Create a repository map with save, find-by-id, and list-all operations
(defn ->repository [conn]
  {:save
   (fn [notification]
     (try
       (log/debug (str "Saving notification: " (:id notification)))
       (let [tx-data (assoc notification 
                       :notification/created-at (java.util.Date.)
                       :notification/updated-at (java.util.Date.))]
         (d/transact conn {:tx-data [tx-data]})
         (log/info (str "Notification saved successfully: " (:id notification)))
         (assoc notification :persisted true))
       (catch Exception e
         (log/error (str "Error saving notification: " (.getMessage e)))
         (throw e))))
   
   :find-by-id
   (fn [id]
     (try
       (log/debug (str "Finding notification by id: " id))
       (let [db (d/db conn)
             result (d/q '[:find (pull ?e [*]) 
                          :where [?e :notification/id ?id]]
                        db {:id id})]
         (if-let [notification (ffirst result)]
           (do
             (log/debug (str "Notification found: " id))
             notification)
           (do
             (log/debug (str "Notification not found: " id))
             nil)))
       (catch Exception e
         (log/error (str "Error finding notification: " (.getMessage e)))
         nil)))
   
   :list-all
   (fn [& {:keys [limit offset] :or {limit 10 offset 0}}]
     (try
       (log/debug (str "Listing notifications - limit: " limit ", offset: " offset))
       (let [db (d/db conn)
             result (d/q '[:find (pull ?e [*]) 
                          :where [?e :notification/id]]
                        db)]
         (vec (drop offset (take (+ limit offset) result))))
       (catch Exception e
         (log/error (str "Error listing notifications: " (.getMessage e)))
         [])))})

(defn initialize-schema
  "Initialize Datomic schema for notifications"
  [conn]
  (try
    (log/info "Initializing Datomic schema...")
    (d/transact conn {:tx-data notification-schema})
    (log/info "Datomic schema initialized successfully")
    true
    (catch Exception e
      (log/warn (str "Schema already exists or error: " (.getMessage e)))
      true)))
