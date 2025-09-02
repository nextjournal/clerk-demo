;; # Semantic Queries

;; Clerk can be very helpful when exploring any kind of data,
;; including the sorts of things for which we might turn to the
;; Semantic Web. To give a sense of what that's like, this notebook
;; gives some examples of querying WikiData for facts about the world.

;; First, we bring in Clerk, the Clerk viewer helpers, Mundaneum (a
;; WikiData wrapper that uses a Datomic-like syntax), and Arrowic (to
;; draw graphviz-style box-and-arrow graphs).

(ns semantic
  (:require [clojure.string :as str]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as v]
            [applied-science.mundaneum.properties :refer [wdt]]
            [applied-science.mundaneum.query :refer [describe entity label query]]
            [arrowic.core :as arr]))

;; Now we can ask questions, like "what is James Clerk Maxwell famous
;; for having invented or discovered?"

(query `{:select [?what]
         :where  [[?what ~(wdt :discoverer-or-inventor) ~(entity "James Clerk Maxwell")]]})

;; The WikiData internal ID `:wd/Q1080745` doesn't immediately mean much
;; to a human, so we'll try again by appending `Label` to the end of
;; the `?what` logic variable so we can see a human readable label
;; that item:

(query `{:select [?whatLabel]
         :where  [[?what ~(wdt :discoverer-or-inventor) ~(entity "James Clerk Maxwell")]]})

;; Ah, better. 😊 This ceremony is required because WikiData uses a
;; language-neutral data representation internally, leaving us with an
;; extra step to get readable results. This can be a little annoying,
;; but it does have benefits. For example, we can ask for an entity's
;; label in every language for which it has been specified in
;; WikiData:

(query `{:select [?what ?label]
         :where  [[?what ~(wdt :discoverer-or-inventor) ~(entity "James Clerk Maxwell")]
                  [?what :rdfs/label ?label]]})

;; One of the nice things about data encoded as a knowledge graph is
;; that we can ask questions that are difficult to pose any other way,
;; then receive answers as structured data for further processing.

;; Here, for instance, is a query asking for things discovered or
;; invented by anyone who has as one of their occupations "physicist":

(def inventions-and-discoveries
  (->> (query `{:select [?whatLabel ?whomLabel]
                :where  [[?what ~(wdt :discoverer-or-inventor) ?whom]
                         [?whom ~(wdt :occupation) ~(entity "physicist")]]
                :limit 500})))

;; ## Tabular data

;; It's great that we can retrieve this information as a sequence of
;; maps that we can explore interactively in Clerk, but sometimes it's
;; more pleasant to display data organized in a table view:

(clerk/table inventions-and-discoveries)

;; Once we see how a given table looks, we might decide that it would
;; be better if, for example, these inventions were grouped by
;; inventor. This is just the sort of thing that Clojure sequence
;; functions can help us do:

(clerk/table (->> inventions-and-discoveries
                  (group-by :whomLabel)
                  (mapv (fn [[whom whats]] [whom (apply str (interpose " ; " (map :whatLabel whats)))]))))

;; ## Geospatial data

;; Some data are more naturally viewed in other ways, of course. In
;; this example we find every instance of any subclass of "human
;; settlement" (village, town, city, and so on) in Germany that has a
;; German language placename ending in _-ow_ or _-itz_, both of which
;; indicate that it was originally named by speakers of a Slavic
;; language.

(def slavic-place-names
  (->> `{:select *
         :where [{?ort {(cat ~(wdt :instance-of) (* ~(wdt :subclass-of))) #{~(entity "human settlement")}
                        ~(wdt :country) #{~(entity "Germany")}
                        :rdfs/label #{?name}
                        ~(wdt :coordinate-location) #{?lonlat}}}
                 [:filter (= (lang ?name) "de")]
                 [:filter (regex ?name "(ow|itz)$")]]
         :limit 1000}
       query
       ;; cleanup lon-lat formatting for map plot!
       (mapv #(let [[lon lat] (-> %
                                  :lonlat
                                  :value
                                  (str/replace #"[^0-9 \.]" "")
                                  (str/split #" "))]
                {:name (:name %) :latitude lat :longitude lon}))))

;; The `:coordinate-location` in this query is the longitude/latitude
;; position of each of these places in a somewhat unfortunate string
;; fomat. The `mapv` at the end converts these `lonlat` strings into
;; key/value pairs so Vega can plot the points on a map. This gives us
;; a very clear picture of which parts of Germany were Slavic prior to
;; the Germanic migrations:

(v/vl {:width 650 :height 650
       :config {:projection {:type "mercator" :center [10.4515 51.1657]}}
       :layer [{:data {:url "https://raw.githubusercontent.com/AliceWi/TopoJSON-Germany/master/germany.json"
                       :format {:type "topojson" :feature "states"}}
                :mark {:type "geoshape" :fill "lightgray" :stroke "white"}}
               {:encoding {:longitude {:field "longitude" :type "quantitative"}
                           :latitude {:field "latitude" :type "quantitative"}}
                :mark "circle"
                :data {:values slavic-place-names}}]})

;; Sometimes the data needs a more customized view. Happily, we can
;; write arbitrary hiccup to be rendered in Clerk. We'll use this
;; query to fetch a list of different species of _Apodiformes_ (swifts
;; and hummingbirds), returning the name in English and Japanese, an
;; image of the bird itself, and map of that bird's home range for
;; each one.

(->> (query `{:select-distinct [?englishName ?japaneseName ?pic ?range]
              :where [[?item (* ~(wdt :parent-taxon)) ~(entity "Apodiformes")]
                      [?item ~(wdt :taxon-rank) ~(entity "species")]
                      [?item :rdfs/label ?englishName]
                      [?item :rdfs/label ?japaneseName]
                      [?item ~(wdt :image) ?pic]
                      [?item ~(wdt :taxon-range-map-image) ?range]
                      [:filter (= (lang ?englishName) "en")]
                      [:filter (= (lang ?japaneseName) "ja")]]
              :limit 9})
     (mapv  #(vector :tr
                    [:td.w-32 (:englishName %)]
                    [:td.w-32 (:japaneseName %)]
                    [:td [:img.w-80 {:src (:pic %)}]]
                    [:td [:img.w-80 {:src (:range %)}]]))
     (into [:table
            [:tr
             [:th "English"]
             [:th "Japanese"]
             [:th "Photo"]
             [:th "Range"]]])
     clerk/html)

;; ## Network diagrams

;; Another useful technique when dealing with semantic or graph-shaped
;; data is to visualize the results as a tree. Here we gather all the
;; languages influenced by Lisp or by languages influenced by Lisp (a
;; transitive query across the graph), and visualize them in a big
;; network diagram.

;; Because Clerk's `html` viewer also understands SVGs, we can just
;; plug in an existing graph visualization library and send the output
;; to Clerk.

;; The graph is really huge, so you'll need to scroll around a bit to
;; see all the languages.

(-> (clerk/html
     (let [lisp (entity "Common Lisp")
           data (query `{:select [?itemLabel ?influencedByLabel]
                         :where [[?item (* ~(wdt :influenced-by)) ~lisp]
                                 [?item ~(wdt :influenced-by) ?influencedBy]
                                 [?influencedBy (* ~(wdt :influenced-by)) ~lisp]]})]
       (arr/as-svg
        (arr/with-graph (arr/create-graph)
          (let [vertex (->> (mapcat (juxt :itemLabel :influencedByLabel) data)
                            distinct
                            (reduce #(assoc %1 %2 (arr/insert-vertex! %2)) {}))]
            (doseq [edge data]
              (when (:influencedByLabel edge)
                (arr/insert-edge! (vertex (:influencedByLabel edge))
                                  (vertex (:itemLabel edge))))))))))
    (assoc :nextjournal/width :full))

;; I hope this gives you some ideas about things you might want to try!
