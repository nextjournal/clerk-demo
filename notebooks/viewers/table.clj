;; # Tables 🔢
(ns viewers.table
  (:require [clojure.data.csv :as csv]
            [nextjournal.clerk :as clerk]))


(clerk/table (csv/read-csv (slurp "datasets/iris.csv")))
