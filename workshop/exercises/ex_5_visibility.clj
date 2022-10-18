(ns exercises.ex-5-visibility
  (:require [clojure.string :as str]
            [meta-csv.core :as csv]
            [nextjournal.clerk :as clerk]))

(clerk/md "# Visibility
This notebook has a bit of setup code. Use Clerk's visibility settings to fully hide it.
**Hint**: you only need to add two forms to archieve this.")


(def cia-factbook
  (csv/read-csv "./datasets/cia-factbook.tsv"))

(def life-expectancy
  (->> cia-factbook
       (remove #(some nil? (map (partial get %) ["Country" "GDP/cap" "Life expectancy"])))
       (map #(sorted-map :country (str/trim (get % "Country"))
                         :gdp (read-string (get % "GDP/cap"))
                         :life-expectancy (read-string (get % "Life expectancy"))))))

#_ "TODO: hide the setup code above"

;; ## Life expectancy

;; Luckily, Clerk's built in table viewer is able to infer how to
;; handle all of the most common configurations of rows and columns
;; automatically.

(clerk/table life-expectancy)

;; We can also graph the data to see if there are any visible
;; correlation between our two variables of interest, GDP per capita
;; and life expectancy.
(clerk/vl
 {:data {:values life-expectancy}
  :width 700
  :height 500
  :mark {:type "point"
         :tooltip {:field "Country"}}
  :encoding {:x {:field :gdp
                 :type :quantitative}
             :y {:field :life-expectancy
                 :type :quantitative}}})
