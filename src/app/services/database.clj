(ns app.services.database
  (:require [hikari-cp.core :as cp]
            [clojure.java.jdbc :as jdbc]
            [migratus.core :as migratus]
            [integrant.core :as ig]))

(defmethod ig/init-key ::connection
  [_ datasource-options]
  {:datasource (cp/make-datasource datasource-options)})

(defmethod ig/halt-key! ::connection
  [_ datasource]
  (cp/close-datasource (:datasource datasource)))

(defmethod ig/init-key ::querier
  [_ {:keys [db-connection]}]
  (fn [sql-params]
    (jdbc/query db-connection sql-params)))

(defmethod ig/init-key ::transactor
  [_ {:keys [db-connection]}]
  (fn [sql-params]
    (jdbc/execute! db-connection sql-params)))

(defmethod ig/init-key ::migrations
  [_ config]
  (migratus/migrate config))
