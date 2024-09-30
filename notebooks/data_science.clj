;; # A small data science example 🔢
^{:nextjournal.clerk/visibility {:code :hide}}
(ns data-science
  (:require [clojure.set :refer [join rename-keys project]]
            [clojure.string :as str]
            [dk.ative.docjure.spreadsheet :as ss]
            [kixi.stats.core :as kixi-stats]
            [kixi.stats.protocols :as kixi-p]
            [meta-csv.core :as csv]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [nextjournal.clerk :as clerk]))

;; # Exploring the world in data

;; One of the challenges in real data science is getting data from
;; different sources in many different formats. In this notebook, we
;; will explore some facts about the world using data taken from a TSV
;; file, an Excel spreadsheet, and a database query.

;; ## Life expectancy

;; First, we'll read in a TSV file containing the most recent CIA
;; World Factbook data using the
;; [meta-csv](https://github.com/ngrunwald/meta-csv) library.
(def cia-factbook
  (csv/read-csv "./datasets/cia-factbook.tsv"))

;; Expanding the results in the data viewer tells us that there are
;; some `nil` values in columns of interest, and that our TSV importer
;; was thrown off by this fact and so didn't convert the numerical
;; columns to number types.

;; We're going to post process this table a bit with ordinary Clojure
;; sequence functions to filter out rows that have `nil`s for our
;; columns of interest, select those rows, convert strings to numbers,
;; and — because we're Clojurists — convert keys to keywords.

(def life-expectancy
  (->> cia-factbook
       (remove #(some nil? (map (partial get %) ["Country" "GDP/cap" "Life expectancy"])))
       (map #(sorted-map :country (str/trim (get % "Country"))
                         :gdp (read-string (get % "GDP/cap"))
                         :life-expectancy (read-string (get % "Life expectancy"))))))

;; Things look pretty good in the data structure browser, but it would
;; be easier to get an overview in tabular form. Luckily, Clerk's
;; built in table viewer is able to infer how to handle all of the
;; most common configurations of rows and columns automatically.

(clerk/table life-expectancy)

;; We can also graph the data to see if there are any visible
;; correlation between our two variables of interest, GDP per capita
;; and life expectancy.
(clerk/vl
 {:data {:values life-expectancy}
  :width 700
  :height 500
  :mark {:type :point}
  :encoding {:x {:field :gdp
                 :type :quantitative}
             :y {:field :life-expectancy
                 :type :quantitative}
             :tooltip {:field :country}}})

;; Unsurprisingly, it seems that living in an extremely poor country
;; has negative consequences for life expectancy. On the other hand,
;; it looks like things start to flatten out once GDP/capita goes
;; above $10-15k/year. Some other interesting patterns also emerge:
;; Singapore and Japan have similar life expectancies, despite the
;; former's GDP being twice the latter's, and Qatar — the richest
;; nation in the dataset by GDP/capita — has similar average life
;; expectancy as the Dominican Republic.

;; ## Inequality

;; Now, let's try the same experiment using information from a
;; spreadsheet containing the GINI coefficient — a widely used measure
;; of income inequality — for each country. We're going to use a
;; library called [Docjure](https://github.com/mjul/docjure) that
;; provides access to Microsoft Office file formats.

;; Docjure's API is a bit low-level and doesn't make the obvious tasks
;; easy, so we're going to use this helper function to make the code
;; below clearer. Check out the line-by-line comments to see how this
;; function works.
(defn load-first-sheet
  "Return the first sheet of an Excel spreadsheet as a seq of maps."
  [filename]
  (let [rows (->> (ss/load-workbook filename) ; load the file
                  (ss/sheet-seq)              ; seq of sheets in the file
                  first                       ; take the first (only)
                  ss/row-seq                  ; get the rows from it
                  (mapv ss/cell-seq))         ; each row -> seq of cells
        ;; break off the headers to produce a seq of maps
        headers   (mapv (comp keyword ss/read-cell) (first rows))]
    ;; map over the rows creating new maps with the headers as keys
    (mapv #(zipmap headers (map ss/read-cell %)) (rest rows))))

;; Now we're going to use a few lines of code to:
;; 1. Load the spreadsheet data.
;; 2. Use `clojure.set`'s `join` function to combine our freshly
;; loaded GINI spreadsheet with our previously prepared life
;; expectancy data, which works because they are both sequences of
;; maps that have a `:country` key.
;; 3. Assoc a `:gini` key in each map to the World Bank's number, but
;; falling back to the CIA's estimate. (These kinds of small
;; programmatic tasks are a constant feature of data wrangling.)
(def expectancy-and-gini
  (->> (load-first-sheet "datasets/countries-gini.xlsx")
       (join life-expectancy)
       (keep #(if-let [gini (or (:giniWB %) (:giniCIA %))]
                (assoc % :gini gini)
                nil))))

;; Expanding the Clojure data structures makes it look like this will
;; work for our comparisons. Let's plot the data to see a list of
;; countries from most to least equal:
(clerk/vl
 {:data {:values expectancy-and-gini}
  :width 600
  :height 1600
  :mark {:type :point}
  :encoding {:x {:field :gini
                 :type :quantitative}
             :y {:field :country
                 :type :nominal
                 :sort :x}
             :tooltip {:field :country}}})

;; And now to have a look at whether inequality and life expectancy
;; are correlated:
(clerk/vl
 {:data {:values expectancy-and-gini}
  :mark :rect
  :width 700
  :height 500
  :encoding {:x {:bin {:maxbins 25}
                 :field :life-expectancy
                 :type :quantitative}
             :y {:bin {:maxbins 25}
                 :field :gini
                 :type :quantitative}
             :color {:aggregate :count :type :quantitative}}
  :config {:view {:stroke :transparent}}})

;; It seems like the mass of long lived countries are also in the
;; lower two thirds of the inequality distribution. A little filtering
;; shows is that the only really long-lived countries above a GINI
;; coefficient of ~50 is Hong Kong.

(clerk/table
 (->> (filter #(< 50 (:gini %)) expectancy-and-gini)
      (sort-by :life-expectancy)))

;; ## Happiness

;; Let's look at happiness! This time, we'll use
;; [jdbc.next](https://github.com/seancorfield/next-jdbc) to perform a
;; SQL query on a Sqlite data containing a table of countries and
;; their relative happiness ratings. Note that we're changing the
;; column name `:country_or_region` to `:country` using
;; `clojure.set`'s `rename-keys` function so that this table will be
;; easy to join with our others.
(def world-happiness
  (let [_run-at #inst "2021-11-26T08:28:29.445-00:00" ; bump this to re-run the query!
        ds (jdbc/get-datasource {:dbtype "sqlite" :dbname "./datasets/happiness.db"})]
    (->> (with-open [conn (jdbc/get-connection ds)]
           (jdbc/execute! conn ["SELECT * FROM happiness"]
                          {:return-keys true :builder-fn rs/as-unqualified-lower-maps}))
         (map #(rename-keys % {:country_or_region :country})))))

;; Looking at the happiness data, it appears that all the usual
;; suspects — Nordics, Western Europeans, Canadians, and Kiwis — are
;; living pretty good lives by their own estimation. Looking closer,
;; we see that although the top twenty countries all relatively
;; prosperous, it's clear that GDP is not strongly correlated with
;; happiness _within that cohort_.

(clerk/table world-happiness)

;; Next, we're computing a linear regression for this dataset using [kixi.stats](https://github.com/MastodonC/kixi.stats).
^{::clerk/viewer {:transform-fn (clerk/update-val kixi-p/parameters)}}
(def linear-regression
  (transduce identity (kixi-stats/simple-linear-regression :score :gdp) world-happiness))

;; We'll use this linear regression to augment out dataset so each datapoint also gets a `:regression` value.
(def world-happiness+regression
  (mapv (fn [{:as datapoint :keys [score]}]
          (assoc datapoint :regression (kixi-p/measure linear-regression score)))
        world-happiness))

;; Let's graph the relationship between happiness and GDP to get a
;; bird's eye view on the situation over our entire dataset. You can
;; mouse over individual data points to get more info:

^{:nextjournal.clerk/visibility {:code :hide :result :show}}
(clerk/vl
 {:data {:values world-happiness+regression}
  :width 700
  :height 500
  :layer [{:mark {:type :point}
           :encoding {:x {:field :score
                          :type :quantitative
                          :scale {:zero false}}
                      :y {:field :gdp
                          :type :quantitative}
                      :tooltip {:field :country}}}
          {:mark {:type :line :color "#ccc"}
           :encoding {:x {:field :score
                          :type :quantitative
                          :scale {:zero false}}
                      :y {:field :regression
                          :type :quantitative}}}]})


;; It looks, as we might have expected, like richer countries are
;; happier than poor ones in general, though with variations and
;; outliers. For example, Finland is in first place but has a similar
;; GDP/capita as number 58, Japan. Perhaps even more striking, Qatar
;; has the highest GDP/capita in the dataset, but Qataris are on
;; average about as happy as people in El Salvador. Likewise, Botswana
;; has five times the GDP/capita of Malawi, but its people are no
;; happier for it. If I were forced to guess why, I might theorize
;; that a properous country with all of its wealth concentrated in
;; very few hands can still be a fairly wretched place to live for the
;; average person to live.

;; One way to investigate this possibility is to plot the correlation
;; between equality and happiness in the rich world. We'll use `join`
;; again, but we'll first use `clojure.set`'s `project` (named by
;; analogy to SQL projection) to pluck just the `:country` and
;; `:score` from the happiness dataset, then sort by the GDP and take
;; the top 20 countries.
(clerk/vl
 {:data {:values (->> (project world-happiness [:country :score])
                      (join expectancy-and-gini)
                      (sort-by :gdp >)
                      (take 20))}
  :width 700
  :height 500
  :mark {:type :point}
  :encoding {:x {:field :score
                 :type :quantitative
                 :scale {:zero false}}
             :y {:field :gini
                 :type :quantitative
                 :scale {:zero false}}
             :tooltip {:field :country}}})

;; This does, at least at first glance, support the notion that the
;; happiest people — just like the longest lived ones — tend to
;; inhabit countries in the more equal part of the GINI distribution.

;; I hope this example gives you some ideas about things you'd like to
;; investigate.
