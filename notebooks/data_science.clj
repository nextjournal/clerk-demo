;; # Tables 🔢
(ns viewers.table
  (:require [clojure.data.csv :as csv]
            [next.jdbc.sql :as sql]
            [next.jdbc :as jdbc]
            [nextjournal.clerk :as clerk]))

(def query-results
  (let [_run-at #inst "2021-05-20T08:28:29.445-00:00" ; bump this to re-run the query!
        ds (jdbc/get-datasource {:dbtype "sqlite" :dbname "./datasets/chinook.db"})]
    (with-open [conn (jdbc/get-connection ds)]
      (clerk/table (jdbc/execute! conn ["SELECT Name, TrackID, AlbumId, UnitPrice FROM tracks"])))))

(clerk/table
 (csv/read-csv (slurp "https://gist.githubusercontent.com/netj/8836201/raw/6f9306ad21398ea43cba4f7d537619d0e07d5ae3/iris.csv")))

