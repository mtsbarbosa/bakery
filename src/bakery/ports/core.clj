(ns bakery.ports.core
  (:require [bakery.ports.http.core :as http.c]))

(defn start-ports-dev
  []
  (http.c/start-dev))

(defn start-ports
  []
  (http.c/start))
