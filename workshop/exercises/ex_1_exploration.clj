;; # Exercise 1: Exploring Data with Clerk

(ns explore
  {:nextjournal.clerk/auto-expand-results? true
   :nextjournal.clerk/budget nil}
  (:require [applied-science.edn-datasets :as data]
            [nextjournal.clerk :as clerk]))

;; The `edn-datasets` library is contains a number of small datasets to explore.
(def dataset-vars
  (sort-by str (vals (ns-publics (find-ns 'applied-science.edn-datasets)))))

;; Explore the metadata on those vars to get a description of what's inside them.

;; ## Table
;; Pick one dataset above that interests you and use `clerk/table` to show the dataset as a table.

;; ## Plotting with Vega or Plotly

;; Use either `clerk/vl` or `clerk/plotly` to visualize the
;; dataset. Work from the basic templates below and adjust them to
;; your needs. You will probably want to use the docs for [Vega
;; Lite](https://vega.github.io/vega-lite/docs/) and
;; [Plotly](https://plotly.com/javascript/reference/index/).

^{::clerk/visibility {:code :hide :result :hide}}
(comment
  (def sample-data
    (take 10 (repeatedly #(hash-map :x (rand-int 1000) :y (rand-int 1000)))))

  (clerk/vl {:data {:values sample-data}
             :mark {:type "point"}
             :encoding {:x {:field :x}
                        :y {:field :y}}})

  (clerk/plotly [{:mode "markers"
                  :x (mapv :x sample-data)
                  :y (mapv :y sample-data)}]))

;; If you have time left, feel free to explore more. Here's some ideas:
;;
;; * Improve the plot to better fit your problem at hand
;; * Use `kixi.stats` to calculate statistical properties like the `mean` or `standard-deviation` of a given dataset.
;; * Play with a second dataset
