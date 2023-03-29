(ns bakery.ports.http.routes.core
  (:require [io.pedestal.http :as http]
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

(defn take-foo-2
  [request]
  (println "Handler")
  {:status 200 :headers {"Content-Type" "application/json"} :body (:async-data request)})

(def specs #{["/foo" :get (conj json-interceptors `r.foo/get-foo) :route-name :get-foo]
             ["/take-foo" :get (conj json-take-interceptors `take-foo-2) :route-name :take-foo]
             ["/foo" :post (conj json-interceptors `r.foo/post-foo) :route-name :post-foo]})
