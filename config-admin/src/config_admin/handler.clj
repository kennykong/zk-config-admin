(ns config-admin.handler
  (:require [compojure.core :refer [defroutes]]
            [compojure.route :as route]
            [config-admin.routes.home :refer [home-routes]]
            [noir.util.middleware :as noir-middleware]))

(defn init []
  (println "config-admin is starting"))

(defn destroy []
  (println "config-admin is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (noir-middleware/app-handler [home-routes app-routes]))


