(ns bakery.controllers.foo
  (:require [bakery.logic.foo :as l.foo]))

(defn generate-foo-data
  []
  (l.foo/return-a-foo-map))
