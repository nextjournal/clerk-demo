;; # 🧠 Perceptron
^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns perceptron
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.experimental :as cx]))

(defn make-perceptron [size]
  ;; TODO discuss adding bias at the end to avoid 0 point
  (into [] (repeatedly size #(- (* 2 (rand)) 1))))

(defn predict [weights input]
  (if (pos? (reduce + (map * weights input))) 1 -1))

(defn learn [target-fn learning-rate weights inputs]
  (mapv (fn [weight input]
          (+ weight (* learning-rate
                       (- (target-fn inputs)
                          (predict weights inputs))
                       input)))
        weights
        inputs))

(defn target-fn [[x y _]]
  (if (< y (* x 2)) 1 -1))

^{::clerk/sync true}
(defonce training-data-size
  (atom 1))

(def trained
  (reduce (partial learn target-fn 0.01)
          (make-perceptron 2)
          (repeatedly @training-data-size #(vector (rand) (rand)))))

^{:nextjournal.clerk/visibility {:code :hide :result :show}}
(clerk/col
 (clerk/html [:strong "Training data size"])
 (cx/slider {:min 0 :max 500 :step 20} `training-data-size))

^{:nextjournal.clerk/visibility {:code :hide :result :show}}
(clerk/html
 (into [:svg {:viewBox "-10 -10 110 110"}
        [:rect {:height 100 :width 100 :fill "#eee"}]
        ;; N.B. line hand-coded to match example training function
        [:line {:stroke "black"
                :stroke-width 0.5
                :x1 0 :y1 0 :x2 50.0 :y2 100.0}]]
       (map (fn [[x y _]]
              (let [target (target-fn [x y])
                    predicted (predict trained [x y])
                    color (if (= target predicted) "black" "red")]
                [:circle {:fill (if (= 1 predicted) color "none")
                          :stroke (if (= -1 predicted) color "none")
                          :stroke-width 0.2
                          :cx (* 100 x)
                          :cy (* 100 y)
                          :r 0.5}]))
            (repeatedly 250 #(vector (rand) (rand) #_1.0)))))

