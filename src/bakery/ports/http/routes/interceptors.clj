(ns bakery.ports.http.routes.interceptors
  (:require
   [clojure.core.async :as async]
   [clojure.data.json :as cjson]))

(defn convert-to-json
  [m]
  (cond (map? m) (cjson/write-str m)
        :else m))

(defn push-async-channel
  [context channel-id channel]
  (assoc-in context [:request :async-channels channel-id] channel))

(defn push-async-data
  ([context data]
    (assoc-in context [:request :async-data] (conj (get-in context [:request :async-data] [])
                                                    data)))
  ([context id data]
   (assoc-in context [:request :async-data id] data)))

;thread pool clojure.core.async.pool-size
;e. g. :jvm-opts ["-Dclojure.core.async.pool-size=32"] in project.clj

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

(def async-interceptor-3
  {:name  ::async-3
   :enter (fn [context]
            (let [chan (async/chan)]
              (async/go
                (do
                  (Thread/sleep 2000)
                  (println "channel 3 being put!")
                  (async/>! chan (get-in (push-async-data context :async-3 42) [:request :async-data]))))
              (println "exit 3")
              (push-async-channel context :async-3 chan)))})

(def async-interceptor-4
  {:name  ::async-4
   :enter (fn [context]
            (let [chan (async/chan)]
              (async/go
                (do
                  (Thread/sleep 4000)
                  (println "channel 4 being put!")
                  (async/>! chan (get-in (push-async-data context :async-4 1917) [:request :async-data]))))
              (println "exit 4")
              (push-async-channel context :async-4 chan)))})

(def full-async-interceptor
  {:name  ::full-async
   :enter (fn [context]
            (async/go
              (do
                (Thread/sleep 10000)
                (println "full-async ending!")))
            context)})

(def async-blocker-interceptor
  {:name  ::async-blocker
   :enter (fn [context]
            (println "async-blocker-interceptor")
            (let [channels (-> context :request :async-channels vals)
                  merged-channels (async/map merge channels)
                  async-data (async/<!! merged-channels)
                  context (assoc-in context [:request :async-data] async-data)]
              (async/close! merged-channels)
              context))})

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

(def error-interceptor
  {:name ::error-i
   :error (fn [context err]
            (println err)
            (assoc context :response {:status 400 :body "Another bad one"}))})

(def name-only-interceptor
  {:name ::name-i})
