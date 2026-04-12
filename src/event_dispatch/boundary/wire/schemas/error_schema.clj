(ns event-dispatch.boundary.wire.schemas.error-schema
  (:require [schema.core :as s]))

(def error-response-schema
  {(s/required-key :error) s/Str
   (s/required-key :message) s/Str
   (s/required-key :timestamp) s/Inst})

(def validation-error-schema
  {(s/required-key :error) s/Str
   (s/required-key :field) s/Str
   (s/required-key :message) s/Str})
