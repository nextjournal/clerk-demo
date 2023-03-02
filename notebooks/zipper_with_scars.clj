;; # Clojure Zippers with Scars à la Huet
;; _Building scars into clojure zipper library to go up and back down at the previous location._
(ns zipper-with-scars
  {:nextjournal.clerk/visibility {:code :fold}
   :nextjournal.clerk/no-cache true}
  (:require [clojure.zip :as zip]
            [arrowic.core :as a]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as v]
            [clojure.string :as str])
  (:import (clojure.lang Seqable)))

;; I recently stumbled into a parsing scenario where a stream of tokens is folded onto a zipper.
;; Some of these tokens would just push a new child at the current location while
;; others would also need to vary the shape some ancestor node. Now the issue with `clojure.zip/up` followed
;; by a `clojure.zip/down` is that it goes back to _the first_ (or leftmost) of the child nodes.
;;
;; Can we build a function to actually _go back_ to the previous lower location
;; without say, storing some left offset while going up or using a non-core zipper library?
;;
;; Mr. Gérard Huet to the rescue here! It turns out
;; the [original zipper paper](http://gallium.inria.fr/~huet/PUBLIC/zip.pdf) has a memo-version of the up and down functions. Let's use Clerk viewers to illustrate how they work.
;;
;; Admittedly, the story with scars is just an excuse to test some animated zipper viewers in Clerk. To that end some machinery follows, unfold at your own peril.
^{::clerk/visibility {:result :hide}}
(do
  (defn loc-seq [zloc]
    ;; this sorts nodes so that children seqs are displayed in the correct order by arrowic
    (try
      (doall
       (concat
        (reverse (take-while some? (next (iterate zip/prev zloc))))
        (cons zloc
              (take-while (complement zip/end?) (next (iterate zip/next zloc))))))
      (catch Throwable e
        (throw (ex-info "Cant seq at loc" {:zloc zloc} e)))))
  (def ->name (comp :name zip/node))
  (defn pnode [zloc] (some-> zloc (get 1) :pnodes peek))
  (def empty-graph {:vertices #{} :edges []})
  (defn add-v [g zloc] (update g :vertices conj (->name zloc)))
  (defn add-e [g zloc]
    (let [parent-name (-> zloc pnode :name)]
      (cond-> g parent-name (update :edges conj [parent-name (-> zloc zip/node :name)]))))
  (defn ->graph [zloc]
    (reduce #(-> %1 (add-e %2) (add-v %2))
            (assoc empty-graph :current? #{(->name zloc)})
            (loc-seq zloc)))
  (defn insert-vtx [{:keys [current?]} name]
    (doto (a/insert-vertex! name
                            :font-color "black"
                            :fill-color (if (current? name) "#ec4899" "#a855f7")
                            :perimeter-spacing 1 :spacing-bottom 1 :spacing-left 1
                            :font-size 20 :font-style 1)
      (.. getGeometry (setWidth 40))
      (.. getGeometry (setHeight 40))))
  (defn ->svg [{:as g :keys [vertices edges]}]
    (a/as-svg
     (a/with-graph (a/create-graph)
       (let [vmap (zipmap vertices (map (partial insert-vtx g) vertices))]
         (doseq [[v1 v2] edges]
           (a/insert-edge! (vmap v1) (vmap v2)
                           :end-arrow false :rounded true
                           :stroke-width "3"
                           :stroke-color "#7c3aed"))))))

  (def zipper?
    (every-pred vector? (comp #{2} count) (comp map? first)
                (comp (some-fn nil? :changed? :ppath :pnodes)
                      second)))
  (def zip-location-viewer
    {:transform-fn (comp nextjournal.clerk.viewer/html (v/update-val #(-> % ->graph ->svg)))
     :pred zipper?})

  (def zip-reel-viewer
    {:pred (every-pred zipper? (comp :cut? meta))
     :transform-fn (comp v/mark-presented (v/update-val (comp #(mapv (comp ->svg ->graph) %) :frames meta)))
     :render-fn '(fn [frames]
                   (let [!state (nextjournal.clerk.render.hooks/use-state {:reel? false :idx 0 :tmr nil})
                         frame-count (count frames)]
                     (let [{:keys [reel? idx tmr]} @!state]
                       (cond
                         (and reel? (not tmr))
                         (swap! !state assoc :tmr (js/setInterval (fn [_] (swap! !state update :idx (comp #(mod % frame-count) inc))) 500))
                         (and (not reel?) tmr)
                         (do (js/clearInterval tmr) (swap! !state assoc :tmr nil :idx 0)))
                       [:div.flex.items-left
                        [:div.flex.mr-5 {:style {:font-size "1.5rem"}}
                         [:div.cursor-pointer {:on-click #(swap! !state update :reel? not)} ({true "⏹" false "▶️"} reel?)]]
                        [nextjournal.clerk.viewer/html (frames (if reel? idx (dec frame-count)))]])))})
  (defn reset-reel [zloc] (vary-meta zloc assoc :frames [] :cut? false))
  (defn add-frame [zloc] (vary-meta zloc update :frames (fnil conj []) zloc))
  (defn cut [zloc] (vary-meta zloc assoc :cut? true))
  (defmacro zmov-> [subj & ops]
    (list* '-> subj `reset-reel `add-frame
           (concat (interpose `add-frame ops) [`add-frame `cut])))
  (clerk/add-viewers! [zip-reel-viewer zip-location-viewer]))

{::clerk/visibility {:code :show :result :hide}}

(def ->zip (partial zip/zipper map? :content #(assoc %1 :content %2)))

(defn ->node [name] {:name name})

{::clerk/visibility {:result :show}}
;; In code cells below, you may read `zmov->` as clojure's own threading macro:
;; the resulting values are the same while metadata is being varied to contain intermediate "frames".
(def tree
  (zmov-> (->zip (->node 'a))
    (zip/append-child (->node 'b))
    (zip/append-child (->node 'c))
    zip/down zip/right
    (zip/append-child (->node 'd))
    (zip/append-child (->node 'e))
    (zip/append-child (->node 'f))
    zip/up))

;; So given a location
(def loc
  (zmov-> tree
    zip/down zip/right
    zip/down zip/right
    zip/right))

;; we'd go up, edit and go back down
(zmov-> loc
  zip/up
  (zip/edit assoc :name "☹︎︎")
  zip/down)
;; losing the position we had. This is the scenario pictured by the author:

;; > When an algorithm has frequent operations which necessitate going up in the tree,
;;and down again at the same position, it is a loss of time (and space, and garbage collecting time, etc)
;;to close the sections in the meantime. It may be advantageous
;;to leave “scars” in the structure allowing direct access to the memorized visited
;;positions. Thus we replace the (non-empty) sections by triples memorizing a tree
;;and its siblings:
;;
;; _**The Zipper** J. Functional Programming 1 (1): 1–000, January 1993_
;;
;; Let's now stick to the rules: reuse Clojure's own data structures while adding a "memorized" functionality.
;; A Scar is a seq that remembers who's left and right of a given point
(deftype Scar [left node right]
  Seqable
  (seq [_] (concat left (cons node right))))
;; and we're plugging it in the original up and down function definitions:
^{::clerk/visibility {:code :hide :result :hide}}
(.addMethod ^clojure.lang.MultiFn print-method Scar (get-method print-method clojure.lang.ISeq))

(defn zip-up-memo [[node path :as loc]]
  (let [{:keys [pnodes ppath l r]} path]
    (when pnodes
      (with-meta [(zip/make-node loc (peek pnodes) (->Scar l node r))
                  (when ppath (assoc ppath :changed? true))] (meta loc)))))

(defn zip-down-memo [[node path :as loc]]
  (when (zip/branch? loc)
    (let [children (zip/children loc)]
      (with-meta [(.node children)
                  {:l (.-left children)
                   :ppath path
                   :pnodes (if path (conj (:pnodes path) node))
                   :r (.-right children)}] (meta loc)))))

;; so now we can go up, edit, go back at the previous location
(zmov-> loc
  zip-up-memo
  (zip/edit assoc :name "☻")
  zip-down-memo)

;; same works with repeated applications of memo movements
(zmov-> loc
  zip-up-memo
  zip-up-memo
  (zip/edit assoc :name "☻")
  zip-down-memo
  zip-down-memo)

;; up and down memo is compatible with other zipper operations
(zmov-> loc
  zip-up-memo
  (zip/insert-left (->node '▶))
  (zip/insert-right (->node '◀))
  zip-down-memo)

;; or move away from the remembered position, go back to it, go back memo
(zmov-> loc
  zip-up-memo
  zip/left
  zip/right
  zip-down-memo)

;; let's get a final crazy reel.
(zmov-> (->node 'a)
  ->zip
  (zip/append-child (->node 'b))
  (zip/append-child (->node 'c))
  zip/down
  (zip/edit update :name str/capitalize)
  zip/right
  (zip/append-child (->node 'd))
  (zip/append-child (->node 'e))
  (zip/insert-child (->node 'f))
  zip/down
  (zip/insert-child (->node 'g))
  (zip/insert-child (->node 'h))
  zip/down zip/right zip/remove
  zip/remove
  zip/remove)

^{::clerk/visibility {:result :hide}}
(comment
  (clerk/clear-cache!)
  (macroexpand '(zmov-> tree zip/up zip/down))
  *e)
