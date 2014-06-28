(ns config-admin.models.zk
  (:import [org.apache.curator RetryPolicy])
  (:import [org.apache.curator.framework CuratorFramework CuratorFrameworkFactory])
  (:import [org.apache.curator.retry ExponentialBackoffRetry])
  (:import [org.apache.zookeeper CreateMode])
  (:import [java.io File]))

(defn createClient [connectionString]
  ;(println (instance? RetryPolicy (ExponentialBackoffRetry. 1000 3)))
  (let [client (CuratorFrameworkFactory/newClient connectionString (ExponentialBackoffRetry. 1000 3))]
    (doto client (.start))))

(def connection-string "10.1.11.214:2181")

(def client (createClient connection-string))

(defn checkExists [path] 
  (.forPath (.checkExists client) path))

(defn readDir [path]
  ;(println path client)
  (if (checkExists path)
    (.forPath (.watched (.getChildren client)) path)))

(def pathsList (java.util.ArrayList.))

(defn listDir [path]
  
  ;(println "------1st-------" path)
  (let [currentDirs (readDir path)]
    ;(println "------2nd-------" currentDirs)
    ;(dorun (map listDir currentDirs))
    
    (do   ;if (empty? currentDirs)
      ;(println "------3rd-------" pathsList (nil? pathsList) (bean pathsList))
      (.add pathsList path)
      (doseq [x currentDirs]
        (if (.endsWith path "/")
          (listDir (str path x))
          (listDir (str path "/" x)))))))

(defn collectDirs [path]
  (.clear pathsList)
  (listDir path)
  pathsList)

(defn getData [path]
  ;(println path)
  (if (checkExists path)
    (let [bPath (.forPath (.getData client) path)]
      (if(nil? bPath)
        nil
        (String. bPath)))))

(defn setData [path data]
  (if (nil? data)
    nil
    (.forPath (.setData client) path (.getBytes data))))

(defn create [path data]
  (if (nil? data)
    nil
    (.forPath (.create client) path (.getBytes data))))

(defn delete [path]
  (.forPath (.guaranteed (.delete client)) path))

