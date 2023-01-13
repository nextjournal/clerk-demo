;; # 🎨 Making a Clerk Logo
^{:nextjournal.clerk/visibility {:code :fold}}
(ns logo
  "A notebook generating the Clerk's logo."
  (:require [nextjournal.clerk :as clerk]))

;; The new Clerk header image is made from a fifth order [Hilbert
;; Curve](https://en.wikipedia.org/wiki/Hilbert_curve), so we will
;; first create a recursive function to generate Hilbert curves of
;; arbitrary order.
(defn hilbert-curve
  ([x-size y-size order]
   (hilbert-curve 0 0 x-size 0 0 y-size order))
  ([x y xi xj yi yj n]
   (if (<= n 0)
     [[(+ x (/ (+ xi yi) 2.0))
       (+ y (/ (+ xj yj) 2.0))]]
     (mapcat (partial apply hilbert-curve)
             [[x y (/ yi 2.0) (/ yj 2.0) (/ xi 2.0) (/ xj 2.0) (dec n)]
              [(+ x (/ xi 2.0)) (+ y (/ xj 2.0)) (/ xi 2.0) (/ xj 2.0) (/ yi 2.0) (/ yj 2.0) (dec n)]
              [(+ x (/ xi 2.0) (/ yi 2.0)) (+ y (/ xj 2.0) (/ yj 2.0)) (/ xi 2.0) (/ xj 2.0) (/ yi 2.0) (/ yj 2.0) (dec n)]
              [(+ x (/ xi 2.0) yi) (+ y (/ xj 2.0) yj) (/ (- yi) 2.0) (/ (- yj) 2.0) (- (/ xi 2.0)) (- (/ xj 2.0)) (dec n)]]))))

;; These are the points for this curve:
(def hilbert-points
  (hilbert-curve 800 800 5))

;; But they're much more interesting if we use the browser's built-in
;; support for SVG to draw a path made from points to show the
;; complete curve. First, we'll make a little helper function to
;; convert a sequence of points into an SVG path:

(defn points->path
  "Turn a sequence of points into an SVG path string."
  [[[start-x start-y] & pts]]
  (reduce str
          (str "M " start-x "," start-y)
          (map (fn [[x y]] (str " L" x "," y)) pts)))

;; And then we'll use that to visualize the curve:

(clerk/html
 [:svg {:stroke "#666666"
        :stroke-width 4
        :fill "none"
        :viewBox "0 0 800 800"}
  [:path {:d (points->path hilbert-points)}]])

;; The trick to getting the effect we want is to apply a conformal
;; mapping to the original Hilbert Curve to convert it into an 👁 shape
;; in celebration of Clerk's viewers. We can do this by treating the
;; original point coordinates as complex numbers, squaring them, then
;; taking the real and imaginary portions of each of those complex
;; numbers as the _x_ and _y_ coordinates of a new set of points. To
;; make this work, we'll use a couple of helper functions to perform
;; those calculations:

(defn complex-multiply
  "Multiply two complex numbers."
  [z1 z2]
  [(- (* (first z1) (first z2))
      (* (second z1) (second z2)))
   (+ (* (first z1) (second z2))
      (* (second z1) (first z2)))])

(defn complex-square
  "Square a complex number."
  [z]
  (complex-multiply z z))

;; And a helpers for multiplying a vector by a scalar:

(defn v*
  "Multiply vector `v` by scalar `s`."
  [v s]
  [(* (first v) s) (* (second v) s)])

;; After which we can generate the complete logo:

(clerk/html
 [:svg {:stroke "rgb(147.0 189.0 154.0)"
        :stroke-width 4
        :fill "none"
        :viewBox "0 0 1000 600"}
  [:rect {:width 1000 :height 600 :stroke "none" :fill "rgb(33.0 5.0 24.0)"}] ; deep purple bg
  [:ellipse {:cx 500 :cy 300 :rx 11 :ry 11 :stroke "none" :fill "rgb(147.0 189.0 154.0)"}] ; the pupil
  [:ellipse {:cx 490 :cy 300 :rx 11 :ry 11 :stroke "none" :fill "rgb(147.0 189.0 154.0)"}]
  [:ellipse {:cx 510 :cy 300 :rx 11 :ry 11 :stroke "none" :fill "rgb(147.0 189.0 154.0)"}]
  (let [points (map #(-> [(- (first %) 400) (- (second %) 400)] ; -[½w ½h] to center the curve
                         complex-square   ; square each vector as a complex number
                         (v* 0.0015))     ; scale those squared vectors down
                    hilbert-points)]
    [:path {:transform "rotate(90) translate(300,-500)" ; rotate and center "eye"
            :stroke-linejoin "round"
            :stroke-linecap "round"
            :d (points->path points)}])])

;; What I find so special and enchanting about the $$w = z^{2}$$
;; mapping that we're using here is that it maintains the angle of
;; intersection everywhere but $$z = 0$$ (the origin). 📐

;; This is called a _conformal map_ by mathematicians. 😍

#_(clerk/show! "notebooks/logo.clj")
