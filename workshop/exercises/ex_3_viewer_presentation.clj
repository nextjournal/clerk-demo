;; # Viewer Presentation

(ns exercises.ex-3-viewer-presentation
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as viewer]))

;; ## Pagination
;; Modify the following expressions so all elements are shown initially
(clerk/with-viewer viewer/sequential-viewer
  (range 100))

(clerk/with-viewers (mapv (fn [v] v) (viewer/get-default-viewers))
  [{:hello (range 100)}])

;; ## Presentation
(def edn-value-viewer
  {:render-fn '(fn [x]
                 (v/html [:pre (pr-str x)]))})

;; Note now Clerk transforms this value. Call `viewer/present` at the REPL to explore how presentation works. Try plugging in different values so you see the effect of elisions as well.

(clerk/with-viewer edn-value-viewer
  {:hello :world})

;; Fill in a different value for `:transform-fn` so the map and it's keys are preserved.
(clerk/with-viewer (assoc edn-value-viewer :transform-fn ,,,identity)
  {:hello :world})

;; Fill in a different value for `:transform-fn` so the whole value is preserved.
(clerk/with-viewer (assoc edn-value-viewer :transform-fn ,,,identity)
  {:hello :world})
