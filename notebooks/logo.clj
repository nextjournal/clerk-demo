;; # 🎨 Making a Clerk Logo
^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns logo
  (:require [nextjournal.clerk :as clerk]
            [clojure2d.core :as c2d]
            [fastmath.complex :as complex]
            [fastmath.vector :as v]))

^{:nextjournal.clerk/visibility #{:hide}}
(clerk/hide-result ; we need this because we don't want to open any windows
 (System/setProperty "java.awt.headless" "true"))

;; The new Clerk header image is made from a fifth order [Hilbert
;; Curve](https://en.wikipedia.org/wiki/Hilbert_curve), so we will
;; first create a recursive function to generate Hilbert curves of
;; arbitrary order.
(defn hilbert-curve
  ([x-size y-size order]
   (hilbert-curve 0 0 x-size 0 0 y-size order))
  ([x y xi xj yi yj n]
   (if (<= n 0)
     [(v/vec2 (+ x (/ (+ xi yi) 2.0))
              (+ y (/ (+ xj yj) 2.0)))]
     (mapcat (partial apply hilbert-curve)
             [[x y (/ yi 2.0) (/ yj 2.0) (/ xi 2.0) (/ xj 2.0) (dec n)]
              [(+ x (/ xi 2.0)) (+ y (/ xj 2.0)) (/ xi 2.0) (/ xj 2.0) (/ yi 2.0) (/ yj 2.0) (dec n)]
              [(+ x (/ xi 2.0) (/ yi 2.0)) (+ y (/ xj 2.0) (/ yj 2.0)) (/ xi 2.0) (/ xj 2.0) (/ yi 2.0) (/ yj 2.0) (dec n)]
              [(+ x (/ xi 2.0) yi) (+ y (/ xj 2.0) yj) (/ (- yi) 2.0) (/ (- yj) 2.0) (- (/ xi 2.0)) (- (/ xj 2.0)) (dec n)]]))))

;; These are the points for this curve:
(def hilbert-points
  (hilbert-curve 800 800 5))

;; But they're much more interesting if we use
;; [Clojure2D](https://github.com/Clojure2D/clojure2d) canvas to draw
;; a path made from points to show the complete curve:
(c2d/with-canvas [canvas (c2d/canvas 800 800 :highest)]
  (-> canvas
      (c2d/set-background 255 255 255)
      (c2d/set-color 66 66 66)
      (c2d/set-stroke 4)
      (c2d/path hilbert-points)
      c2d/to-image)) 

;; The trick to getting the effect we want is to apply a conformal
;; mapping to the original Hilbert Curve to convert it into an 👁 shape
;; in celebration of Clerk's viewers. We can do this by treating the
;; original point coordinates as complex numbers, squaring them, then
;; taking the real and imaginary portions of each of those complex
;; numbers as the _x_ and _y_ coordinates of a new set of points. This
;; is made especially easy because Clojure2D happens to include the
;; author's [Fastmath](https://github.com/generateme/fastmath)
;; library. 🎉

(c2d/with-canvas [canvas (c2d/canvas 1000 600 :highest)]
  (-> canvas
      (c2d/set-background 33.0 5.0 24.0) ; RGB deep purple
      (c2d/translate 500 300)            ; origin to center
      (c2d/rotate (/ Math/PI 2))         ; rotate the canvas, ⬯ → ⬭
      ;; colour and stroke width
      (c2d/set-color 147.0 189.0 154.0)
      (c2d/set-stroke 4)
      ;; ellipses to fill in the center of the "eye"
      (c2d/ellipse 0 0 22 22)
      (c2d/ellipse 0 -10 20 20)
      (c2d/ellipse 0 10 20 20)
      ;; draw a path using the complex square of our hilbert curve points
      (c2d/path (map #(-> (v/sub % (v/vec2 400 400)) ; -[½w ½h] from vectors to center the curve
                          complex/sq                 ; square each vector as a complex number
                          (v/mult 0.0015))           ; scale those squared vectors down          
                     hilbert-points))
      c2d/to-image))

;; What I find so special and enchanting about the $$w = z^{2}$$
;; mapping that we're using here is that it maintains the angle of
;; intersection everywhere but $$z = 0$$ (the origin). 📐

;; This is called a _conformal map_ by mathematicians. 😍

#_(clerk/show! "notebooks/logo.clj")
