(ns emmy-repro
  (:require [emmy.env :as e]
            [emmy.mechanics.lagrange]))

;; ## BUG 1:

;; This notebook takes close to 2 seconds to evaluate:

;; Clerk evaluated '/Users/sritchie/code/clj/clerk-demo/notebooks/emmy_repro.clj' in 1853.674042ms.

(defn angles->rect [theta1]
  (e/sin theta1))


;; Form the final Langrangian in generalized coordinates (the angles of each
;; segment) by composing `L-rect` with a properly transformed `angles->rect`
;; coordinate transform!
