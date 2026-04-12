(ns event-dispatch.boundary.wire.schemas.notification-schema
  (:require [schema.core :as s]))

(def notification-create-schema
  {(s/required-key :recipient) s/Str
   (s/required-key :message) s/Str
   (s/required-key :type) s/Str})

(def notification-response-schema
  {(s/required-key :id) s/Str
   (s/required-key :recipient) s/Str
   (s/required-key :message) s/Str
   (s/required-key :type) s/Str
   (s/required-key :status) s/Str
   (s/required-key :created-at) s/Inst})

(def notification-list-response-schema
  {(s/required-key :total) s/Int
   (s/required-key :limit) s/Int
   (s/required-key :offset) s/Int
   (s/required-key :results) [notification-response-schema]})
