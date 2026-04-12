(ns event-dispatch.boundary.wire.schemas.event-schema
  (:require [schema.core :as s]))

(def event-schema
  {(s/required-key :id) s/Str
   (s/required-key :notification-id) s/Str
   (s/required-key :event-type) s/Str
   (s/required-key :timestamp) s/Inst
   (s/required-key :details) s/Any})

(def event-publish-schema
  {(s/required-key :id) s/Str
   (s/required-key :notification-id) s/Str
   (s/required-key :event-type) s/Str
   (s/required-key :timestamp) s/Inst
   (s/required-key :details) s/Any})
