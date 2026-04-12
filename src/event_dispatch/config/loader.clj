(ns event-dispatch.config.loader
  (:require [aero.core :as aero]
            [clojure.java.io :as io]))

(defn load-config
  "Load configuration from config.edn file"
  []
  (aero/read-config (io/resource "config.edn")))

(defn get-config
  "Get configuration value by path"
  [config path]
  (get-in config path))
