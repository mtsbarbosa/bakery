(ns bakery.ports.http.routes.interceptors
  (:require
   [clojure.core.async :as async]
   [clojure.data.json :as cjson]))

(defn convert-to-json
  [m]
  (cond (map? m) (cjson/write-str m)
        :else m))

(defn push-async-data
  [context data]
  (assoc-in context [:request :async-data] (conj (get-in context [:request :async-data] [])
                                                 data)))

(def async-interceptor
  {:name  ::async
   :enter (fn [context]
            (let [chan (async/go
                         (do
                           (Thread/sleep 2000)
                           (println "channel 1 being put!")
                           (push-async-data context 42)))]
              (println "exit 1")
              chan))})

(def async-interceptor-2
  {:name  ::async-2
   :enter (fn [context]
            (let [chan (async/go
                        (do
                          (Thread/sleep 4000)
                          (println "channel 2 being put!")
                          (push-async-data context 1917)))]
              (println "exit 2")
              chan))})

(def pos-interceptor
  {:name  ::pos
   :enter (fn [context]
            (println "pos-interceptor" (-> context :request :async-data))
            context)})

(defn json-out
  []
  {:name ::json-out
   :leave (fn [context]
            (->> (:response context)
                 :body
                 (convert-to-json)
                 (assoc-in context [:response :body])))})
