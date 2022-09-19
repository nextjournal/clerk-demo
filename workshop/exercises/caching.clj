;; # Caching
(ns caching
  (:require [nextjournal.clerk :as clerk]))

;; Note how the following notebook will show the same value
;; everytime. Opt out of the caching for the side-effecting expression
;; so it shows an updated order every time.

;; _Hint_: only one expression that needs to annotated.

(defn make-initial-value []
  {:order (shuffle (range 10))})

(defonce !state
  (atom (make-initial-value)))

(reset! !state (make-initial-value))

(mapv inc (:order @!state))
