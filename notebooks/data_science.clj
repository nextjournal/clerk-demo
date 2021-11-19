;; # A small data science example 🔢
(ns data-science
  (:require [meta-csv.core :as csv]
            [next.jdbc.sql :as sql]
            [next.jdbc :as jdbc]
            [dk.ative.docjure.spreadsheet :as ss]
            [nextjournal.clerk :as clerk]))

;; cia-factbook.tsv
;; earnings-by-school.tsv
;; wall-street-commute-times.tsv
;; worker-ceo-ratio.tsv

#_(def query-results
  (let [_run-at #inst "2021-05-20T08:28:29.445-00:00" ; bump this to re-run the query!
        ds (jdbc/get-datasource {:dbtype "sqlite" :dbname "./datasets/chinook.db"})]
    (with-open [conn (jdbc/get-connection ds)]
      (clerk/table (jdbc/execute! conn ["SELECT Name, TrackID, AlbumId, UnitPrice FROM tracks"])))))

#_(clerk/table
 (csv/read-csv (slurp "https://gist.githubusercontent.com/netj/8836201/raw/6f9306ad21398ea43cba4f7d537619d0e07d5ae3/iris.csv")))

#_(clerk/clear-cache!)

(def worker-ceo-ratio
  (csv/read-csv "./datasets/worker-ceo-ratio.tsv"))

#_(clerk/vl
 {:data {:values worker-ceo-ratio}
  :width 700
  :height 500
  :mark "bar"
  :encoding {:x {:bin {:maxbins 20}
                 :axis {:labelAngle -45}
                 :field :Compensation}
             :y {:aggregate "count"}}})

#_(clerk/vl
 {:data {:values worker-ceo-ratio}
  :width 700
  :height 500
  :mark "bar"
  :encoding {:x {:bin {:maxbins 20}
                 :axis {:labelAngle -45}
                 :field "Median Worker Pay"}
             :y {:aggregate "count"}}})

(clerk/vl
 {:data {:values worker-ceo-ratio}
  :mark "rect"
  :width 700
  :height 500
  :encoding {:x {:bin {:maxbins 40}
                 :field :Compensation
                 :axis {:labelAngle -45}
                 :type "quantitative"}
             :y {:bin {:maxbins 40}
                 :field :Median-Worker-Pay
                 :type "quantitative"}
             :color {:aggregate "count" :type "quantitative"}}
  :config {:view {:stroke "transparent"}}})



;; load some data using
;; https://github.com/mjul/docjure

#_(->> (ss/load-workbook "spreadsheet.xlsx")
       (ss/select-sheet "Price List")
       (ss/select-columns {:A :name, :B :price}))

#_(let [wb (create-workbook "Price List"
                          [["Name" "Price"]
                           ["Foo Widget" 100]
                           ["Bar Widget" 200]])
      sheet (select-sheet "Price List" wb)
      header-row (first (row-seq sheet))]
  (set-row-style! header-row (create-cell-style! wb {:background :yellow,
                                                     :font {:bold true}}))
  (save-workbook! "spreadsheet.xlsx" wb))
