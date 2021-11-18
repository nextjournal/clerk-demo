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
            [mundaneum.query :refer [describe entity label property query]]
            [arrowic.core :as arr]))

;; Now we can ask questions, like "what is James Clerk Maxwell famous
;; for having invented or discovered?"

(query '[:select ?what
         :where [[?what (wdt :discoverer-or-inventor) (entity "James Clerk Maxwell")]]])

;; The WikiData internal ID `Q1080745` doesn't immediately mean much
;; to a human, so we'll try again by appending `Label` to the end of
;; the `?what` logic variable so we can see a human readable label
;; that item:

(query '[:select ?whatLabel
         :where [[?what (wdt :discoverer-or-inventor) (entity "James Clerk Maxwell")]]])

;; Ah, better. 😊 This ceremony is required because WikiData uses a
;; language-neutral data representation internally, leaving us with an
;; extra step to get readable results. This can be a little annoying,
;; but it does have benefits. For example, we can ask for an entity's
;; label in every language for which it has been specified in
;; WikiData:

(query '[:select ?what ?label
         :where [[?what (wdt :discoverer-or-inventor) (entity "James Clerk Maxwell")]
                 [?what rdfs:label ?label]]])

;; One of the nice things about data encoded as a knowledge graph is
;; that we can ask questions that are difficult to pose any other way,
;; then receive answers as structured data for further processing.

;; Here, for instance, is a query asking for things discovered or
;; invented by anyone who has as one of their occupations "physicist":

(def inventions-and-discoveries
  (->> (query '[:select ?whatLabel ?whomLabel
                :where [[?what (wdt :discoverer-or-inventor) ?whom]
                        [?whom (wdt :occupation) (entity "physicist")]]
                :limit 500])))

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
  (->> (query
        '[:select *
          :where [[?ort (wdt :instance-of) / (wdt :subclass-of) * (entity "human settlement")
                   _ (wdt :country) (entity "Germany")
                   _ rdfs:label ?name
                   _ (wdt :coordinate-location) ?lonlat]
                  :filter ((lang ?name) = "de")
                  :filter ((regex ?name "(ow|itz)$"))]
          :limit 1000])
       ;; cleanup lon-lat formatting for map plot!
       (mapv #(let [[lon lat] (-> (:lonlat %)
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
       :layer [{:data {:url "https://raw.githubusercontent.com/deldersveld/topojson/master/countries/germany/germany-regions.json"
                       :format {:type "topojson" :feature "DEU_adm2"}}
                :mark {:type "geoshape" :fill "lightgray" :stroke "white"}}
               {:encoding {:longitude {:field "longitude" :type "quantitative"}
                           :latitude {:field "latitude" :type "quantitative"}}
                :mark "circle"
                :data {:values slavic-place-names}}]})

;; Sometimes the data needs a more customized view. Happily, we can
;; write arbitrary hiccup to be rendered in Clerk. First, we'll make a
;; helper function to convert the style of image url we receive from
;; WikiData into proper Wiki Commons URLs with a fixed width:

(defn wiki-image
  "Helper that takes an image path `url` and creates a full wikimedia commons URL from it, optionally specifying a particular `width`."
  ([url width] (str "https://commons.wikimedia.org/w/index.php?title=Special:Redirect/file/"
                    url
                    "&width="
                    width))
  ([url] (wiki-image url 300)))

;; And now we can use this query to fetch a list of different species
;; of _Apodiformes_ (swifts and hummingbirds), returning a name,
;; image, and map of home range for each one.

(->> (query '[:select :distinct ?item ?itemLabel ?pic ?range
              :where [[?item (wdt :parent-taxon) * (entity "Apodiformes")
                       _ (wdt :taxon-rank) (entity "species")
                       _ rdfs:label ?englishName
                       _ (wdt :image) ?pic
                       _ (wdt :taxon-range-map-image) ?range]
                      :filter ((lang ?englishName) = "en")]
              :limit 10])
     (mapv #(vector :tr
                    [:td (:itemLabel %)]
                    [:td [:img {:src (wiki-image (:pic %))}]]
                    [:td [:img {:src (wiki-image (:range %))}]]))
     (into [:table])
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
     (let [data (query '[:select ?itemLabel ?influencedByLabel
                         :where [[?item (wdt :influenced-by) * (entity "Lisp")
                                  _ (wdt :influenced-by) ?influencedBy]
                                 [?influencedBy (wdt :influenced-by) * (entity "Lisp")]]])]
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
