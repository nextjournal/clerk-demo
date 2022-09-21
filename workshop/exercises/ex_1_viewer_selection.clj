;; # Viewer Selection

(ns exercises.ex-1-viewer-selection
  (:require [exercises.ex-5-visibility :as vis-ex]
            [nextjournal.clerk :as clerk]))

;; To select a viewer, you can use functions or metadata. In what
;; situations would you reach for which one? Explore the differences
;; at the REPL.

;; ## Explicit Function

(def table-explicit-function
  (clerk/table vis-ex/life-expectancy))

;; ## Metadata

^{::clerk/viewer clerk/table}
(def table-metadata
  vis-ex/life-expectancy)
