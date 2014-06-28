(ns config-admin.routes.home
  (:require [compojure.core :refer :all]
            [config-admin.views.layout :as layout]
            [hiccup.form :refer :all]
            [hiccup.element :refer [link-to]]
            [noir.response :as resp]
            [config-admin.models.zk :as zk]
            [config-admin.models.db :as db]
            [noir.session :as session]
            [noir.response :as resp]))

(defn show-dirs [path]
  [:h3 
   [:ol.dirs (str "**************** " path " ****************")
    (for [elem (zk/collectDirs path)]
      [:li [:a {:name elem} [:p elem]]
       (form-to [:post "/update"] 
                (hidden-field :path elem)
                (text-area {:rows 2 :cols 80} "value" (zk/getData elem))
                (submit-button "update"))])]])

(defn show-createPathValue []
  [:h3 "Create a path:"
   (form-to [:post "/create"]
            [:p "Path:"]
            (text-area {:rows 2 :cols 80} "path")
            [:p "Value:"]
            (text-area {:rows 2 :cols 80} "value")
            [:br]
            (submit-button "create"))
   [:hr]])

(defn show-deletePathValue []
  [:h3 "Delete a path:"
   (form-to [:post "/delete"]
            [:p "Path:"]
            (text-area {:rows 2 :cols 80} "path")
            [:br]
            (submit-button "delete"))
   [:hr]])

(defn get-roles [name]
  (let [user (db/get-user name)]
    (:role user)))

(def config-root-path "/trinity/config")

(defn get-app-role-root-paths [app-paths, roles]
  (for [i app-paths j (.split roles ",")]
    (if (.endsWith i "/")
      (str config-root-path "/" i j)
      (str config-root-path "/" i "/" j))))

(defn show-app-root-paths [path-list]
  [:h3 
   [:ul.paths 
    (for [e path-list]
      [:li [:p (link-to (str "#" e) e)]]
      )]])

(defn home []
  (layout/common 
    [:h1 "Zookeeper Config Admin"]
    (if-let [user (session/get :user)]
      [:div [:h2  (link-to "/logout" (str "logout " user))]
       [:h2 "Welcome to zk config server (" zk/connection-string "), your role is: " (get-roles user) ". Your allowed paths are: "]
       [:br]
       (let [app-root-paths (get-app-role-root-paths (zk/readDir config-root-path) (get-roles user))]
         (list
           (show-app-root-paths app-root-paths)
           [:hr]
           (show-createPathValue)
           (show-deletePathValue)
           (for [i app-root-paths]
             (show-dirs i))))]
      [:div "Please Login :)"
       (form-to [:post "/login"]
                (text-field {:placeholder "screen name"} "name")
                (password-field {:placeholder "password"} "pass")
                (submit-button "login"))])))

(defn error [message]
  (layout/common [:h1 "Zookeeper Config Admin"]
                 [:h2 "There is some error:" ]
                 [:h2 message]))

(defn path-has-auth-base? [path, roles]
  (first (if-let [roles (vec (.split roles, ","))]
           (filter #(.matches path (str config-root-path "/.+/" % ".*")) roles ))))

(defn path-has-auth? [path]
  (path-has-auth-base? path (get-roles (session/get :user))))

(defn updateZk [path value]
  (cond
    (empty? path)
    (error "No path to update!")
    :else
    (do
      (zk/setData path value)
      (resp/redirect "/"))))

(defn createZk [path value]
  (cond
    (empty? path)
    (error "No path to create!")
    (not (path-has-auth? path))
    (error "You don't have permission to create!")
    :else
    (do
      (zk/create path value)
      (resp/redirect "/"))))

(defn deleteZk [path]
  (cond
    (empty? path)
    (error "No path to delete!")
    (not (path-has-auth? path))
    (error "You don't have permission to delete!")
    :else
    (do
      (zk/delete path)
      (resp/redirect "/"))))

(defn handle-login [name pass]
  (let [user (db/get-user name)]
    (if (and user (.equals pass (:pass user)))
      (session/put! :user name)))
  (resp/redirect "/"))

(defn handle-logout []
  (session/clear!)
  (resp/redirect "/"))

(defroutes home-routes
  (GET "/" [] (home))
  (POST "/update" [path value] (updateZk path value))
  (POST "/create" [path value] (createZk path value))
  (POST "/delete" [path value] (deleteZk path))
  (POST "/login" [name pass] (handle-login name pass))
  (GET "/logout" [] (handle-logout)))


