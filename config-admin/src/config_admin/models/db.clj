(ns config-admin.models.db
  (:require [clojure.java.jdbc :as sql])
  (:import java.sql.DriverManager))

(def db {:classname "org.sqlite.JDBC",
         :subprotocol "sqlite",
         :subname "db.sq3"})

(defn create-configadmin-table []
  (sql/with-connection
    db
    (sql/create-table
      :configadmin
      [:id "INTEGER PRIMARY KEY AUTOINCREMENT"]
      [:timestamp "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"]
      [:name "TEXT"]
      [:pass "TEXT"]
      [:role "TEXT"])
    (sql/do-commands "CREATE INDEX timestamp_index ON configadmin (timestamp)")))

(defn read-users []
  (sql/with-connection
    db
    (sql/with-query-results res
      ["SELECT * FROM configadmin ORDER BY timestamp DESC"]
      (doall res))))

(defn get-user [name]
  (sql/with-connection
    db
    (sql/with-query-results res
      ["SELECT * FROM configadmin WHERE name = ?" name]
      (first res))))

(defn save-user [name pass role]
  (sql/with-connection
    db
    (sql/insert-values
      :configadmin
      [:name :pass :role :timestamp]
      [name pass role (new java.util.Date)])))
