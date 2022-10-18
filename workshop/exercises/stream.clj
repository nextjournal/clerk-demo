;; # Introduction: Exploring Data with Clerk

(ns explore
  (:require [applied-science.edn-datasets :as data]
            [nextjournal.clerk :as clerk]))

;; Let's start with this dataset.

data/air-passengers

;; ## Table
;; Use `clerk/table` to show the dataset as a table.
(clerk/table data/air-passengers)

;; ## Metadata
;; Explore the metadata on the dataset's `var`. (Hint: `clerk/md` might come in handy).
(clerk/md (:doc (meta (var data/air-passengers))))

;; ## Plotting with Vega or Plotly

(clerk/vl {:data {:values data/iris}
           :width 500
           :height 500
           :title "sepal-length vs. sepal-width"
           :mark {:type "point"
                  :tooltip {:field :species}}
           :encoding {:color {:field :species}
                      :x {:field :sepal-length
                          :type :quantitative
                          :scale {:zero false}}
                      :y {:field :sepal-width
                          :type :quantitative
                          :scale {:zero false}}}
           :embed/opts {:actions false}})

(ns-publics (find-ns 'applied-science.edn-datasets))

;; Start with the rationale
;; Intro talk
