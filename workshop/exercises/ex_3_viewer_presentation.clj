;; # Viewer Presentation

(ns exercises.ex-3-viewer-presentation
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as viewer]))

;; ## Pagination
;; Modify the following expressions so all elements are shown initially
(clerk/with-viewer (dissoc viewer/sequential-viewer :page-size)
  (range 100))

(def without-pagination
  {:page-size #(dissoc % :page-size)})

(def viewers-without-lazy-loading
  (viewer/update-viewers viewer/default-viewers without-pagination))

(clerk/with-viewers viewers-without-lazy-loading
  [{:hello (range 100)}])

;; ## Presentation
(def edn-value-viewer
  {:render-fn '(fn [x]
                 [:pre (pr-str x)])})

;; Note now Clerk transforms this value. Call `viewer/present` at the REPL to explore how presentation works. Try plugging in different values so you see the effect of elisions as well.

(viewer/present [1 2 3])

{:nextjournal/value [1 2 3]}

(viewer/present {:hello :world})

(clerk/with-viewer edn-value-viewer
  {:hello :world})

;; Fill in a different value for `:transform-fn` so the map and it's keys are preserved.
(clerk/with-viewer (assoc edn-value-viewer :transform-fn clerk/mark-preserve-keys)
  {:hello :world})

;; Fill in a different value for `:transform-fn` so the whole value is preserved.
(clerk/with-viewer (assoc edn-value-viewer :transform-fn clerk/mark-presented)
  {:hello :world})
