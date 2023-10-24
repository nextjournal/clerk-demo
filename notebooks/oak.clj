;; # 🎨 Drawing a leaf with trigonometry
^{:nextjournal.clerk/visibility {:code :hide}}
(ns oak
  (:require [nextjournal.clerk :as clerk]
            #_[clojure.math :as math :refer [sin cos pow PI]])) ; 😿

^{:nextjournal.clerk/visibility {:code :fold}}
(defn points->svg
  "Turn a sequence of points into an SVG path string."
  [pts]
  (let [xs (map first pts)
        ys (map second pts)
        min-x (- (apply min xs) 0.1)
        min-y (- (apply min ys) 0.1)
        max-x (+ (apply max xs) 0.1)
        max-y (+ (apply max ys) 0.1)
        [[start-x start-y] & pts] pts]
    (clerk/html
     [:svg {:stroke "#666666"
            :stroke-width 0.006
            :fill "none"
            :viewBox (str min-x " " min-y " " (+ max-x (Math/abs min-x)) " " (+ max-y (Math/abs min-y)))}
      [:path {:d (reduce str
                         (str "M " start-x "," start-y)
                         (map (fn [[x y]] (str " L" x "," y)) pts))}]])))

;; Sometimes it's fun to make the outline of some object using a bit
;; of trigonometry. For example, while on a walk with
;; [Martin](https://mas.to/@mkvlr) we encountered a lovely fresh
;; fallen oak leaf. I decided that it might provide an interesting
;; basis for a logo for one of our projects called
;; [Garden](https://application.garden).

;; Here's a function that produces a point set that resembles an oak
;; leaf when plotted:

(def leaf-points
  (mapv (fn [t] [(+ (* 0.01 (Math/pow (Math/cos t) 19))
                   (* 0.25
                      (Math/sin (* 2 t))
                      (- 1 (* 0.45 (Math/pow (Math/sin (* t 10)) 2)))
                      (- 1 (Math/pow (* (Math/cos t) (Math/cos (* t 3))) 10))))
                (- (* (Math/sin t)
                     (- 1 (* (+ 0.45 (Math/pow (Math/sin (* 2 t)) 2))
                             (* 0.2 (Math/pow (Math/sin (* t 10)) 2))))))])
        (range 0 Math/PI 0.01)))

;; And here is the plot:

(points->svg leaf-points)

;; But how does one get from the idea of a leaf to a function of this
;; kind? It starts with a semi-circle:

(points->svg
 (mapv (fn [t] [(Math/cos t)
               (- (Math/sin t))]) ; (- y) so it increases upward
       (range 0 Math/PI 0.01)))

;; Now, if we introduce the ${x}$ half of our leaf point function while
;; continuing to use ${sin}$ for the ${y}$ portion we can see the basic
;; shape of our leaf:

(points->svg
 (mapv (fn [t] [(+ (* 0.01 (Math/pow (Math/cos t) 19))
                  (* 0.25
                     (Math/sin (* 2 t))
                     (- 1 (* 0.45 (Math/pow (Math/sin (* t 10)) 2)))
                     (- 1 (Math/pow (* (Math/cos t) (Math/cos (* t 3))) 10))))
               (- (Math/sin t))])
        (range 0 Math/PI 0.01)))

;; Sculpting with trigonometry almost always relies on these sorts of
;; scaled exponentials. If you tinker with the individual
;; coefficients, you can get a feeling for what kinds of scaling they
;; cause.

;; Notice that if we use the ${y}$ part of our leaf function while
;; using ${cos}$ for the ${x}$ portion we can see the basic contour of
;; our leaf, but arranged along the semi-circle:

(points->svg
 (mapv (fn [t] [(Math/cos t)
               (- (* (Math/sin t)
                     (- 1 (* (+ 0.45 (Math/pow (Math/sin (* 2 t)) 2))
                             (* 0.2 (Math/pow (Math/sin (* t 10)) 2))))))])
       (range 0 Math/PI 0.01)))

;; It's worth noticing the similarities between the function that
;; computes ${x}$ values and the one that computes ${x}$ values. The
;; two shapes are actually quite similar, and they work together to
;; make a very nice shape indeed. 🌳🙂

#_(clerk/show! "notebooks/oak.clj")
