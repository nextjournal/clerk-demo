;; # Tables 🔢
(ns viewers.table
  (:require [clojure.data.csv :as csv]
            [nextjournal.clerk :as clerk]))


(clerk/table (csv/read-csv (slurp "https://gist.githubusercontent.com/netj/8836201/raw/6f9306ad21398ea43cba4f7d537619d0e07d5ae3/iris.csv")))
