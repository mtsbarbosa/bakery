(ns bakery.ports.http.routes.core
  (:require
   [clojure.core.async :as async]
   [io.pedestal.http :as http]
   [io.pedestal.http.body-params :as body-params]
   [bakery.ports.http.routes.interceptors :as i]
   [bakery.ports.http.routes.foo :as r.foo]))

(def common-interceptors [(body-params/body-params)
                          http/html-body])

(def json-interceptors [(body-params/body-params)
                        (i/json-out)
                        http/html-body])

(def json-take-interceptors [(body-params/body-params)
                             i/async-interceptor
                             i/async-interceptor-2
                             (i/json-out)
                             http/html-body])

(def json-take-interceptors-2 [(body-params/body-params)
                               i/async-interceptor-3
                               i/async-interceptor-4
                               i/full-async-interceptor
                               i/pos-interceptor
                               (i/json-out)
                               http/html-body])

(def json-take-interceptors-3 [(body-params/body-params)
                               i/async-interceptor-3
                               i/async-interceptor-4
                               i/full-async-interceptor
                               i/pos-interceptor
                               i/async-blocker-interceptor
                               (i/json-out)
                               http/html-body])

(defn take-foo
  [request]
  (println "Handler")
  {:status 200 :headers {"Content-Type" "application/json"} :body (:async-data request)})

(defn take-foo-2
  [request]
  (println "Handler 2")
  (let [channels (-> request :async-channels vals)
        merged-channels (async/map merge channels)
        async-data (async/<!! merged-channels)]
    (async/close! merged-channels)
    {:status 200 :headers {"Content-Type" "application/json"} :body async-data}))

(def specs #{["/foo" :get (conj json-interceptors `r.foo/get-foo) :route-name :get-foo]
             ["/take-foo" :get (conj json-take-interceptors `take-foo) :route-name :take-foo]
             ["/take-foo-2" :get (conj json-take-interceptors-2 `take-foo-2) :route-name :take-foo-2] ;handler awaits approach
             ["/take-foo-3" :get (conj json-take-interceptors-3 `take-foo) :route-name :take-foo-3] ;interceptor awaits approach
             ["/foo" :post (conj json-interceptors `r.foo/post-foo) :route-name :post-foo]})
