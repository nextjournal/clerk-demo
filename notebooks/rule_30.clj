;; # Rule 30 🕹
;; Let's explore cellular automata in a Clerk Notebook.
(ns rule-30
  (:require [nextjournal.clerk :as clerk]))

;; We start by creating custom viewers for numbers, lists, and
;; vectors.

;; These viewers are maps that contain a `:pred` (predicate) function
;; that Clerk will use to decide which items should be viewed with the
;; `:render-fn` that follows. Clerk always uses the first viewer whose
;; predicate matches, so it's possible to override the built-in
;; viewers with whatever we want.

;; In this case, we want `1`s and `0`s to show up as filled and empty
;; boxes, lists to stack their contents vertically, and vectors to
;; line up their contents horizontally. We achieve this with the
;; `html` viewer, which allows us to emit arbitrary hiccup to
;; represent a value.

(clerk/add-viewers!
 [{:pred number?
   :render-fn '(fn [n] [:div.inline-block {:style {:width 16 :height 16}
                                           :class (if (pos? n) "bg-black" "bg-white border-solid border-2 border-black")}])}
  {:pred (every-pred list? (partial every? (some-fn number? vector?)))
   :render-fn '(fn [rows opts] (into [:div.flex.flex-col] (nextjournal.clerk.render/inspect-children opts) rows))}
  {:pred (every-pred vector? (complement map-entry?) (partial every? number?))
   :render-fn '(fn [row opts] (into [:div.flex.inline-flex] (nextjournal.clerk.render/inspect-children opts) row))}])

;; Now let's test each one to make sure they look the way we want:

0

1

[0 1 0]

(list 0 1 0)

;; Looks good! 😊

;; _Rule 30_ is implemented as a set of rules for translating one
;; state to another, which can be represented as a map if transitions
;; in Clojure. This definition maps any vector of three cells to a new
;; value for the middle cell. Later, we'll scan over our state space,
;; applying these rules to every position on the board. (Notice how
;; the built-in map viewer works unchanged with our newly defined
;; number and vector viewers.)
(def rule-30
  {[1 1 1] 0
   [1 1 0] 0
   [1 0 1] 0
   [1 0 0] 1
   [0 1 1] 1
   [0 1 0] 1
   [0 0 1] 1
   [0 0 0] 0})

;; Our first generation is a row with 33 elements. The element at the center is a black square, all other squares are white.
(def first-generation
  (let [n 33]
    (assoc (vec (repeat n 0)) (/ (dec n) 2) 1)))

;; Finally, we can `iterate` over `first-generation`'s state to evolve
;; the state of the whole board over time. Try changing the value
;; passed to `take` to render more states! Add a `drop` after the
;; `take` to sample other points in time! Most of all, have fun.
(let [evolve #(mapv rule-30 (partition 3 1 (repeat 0) (cons 0 %)))]
  (->> first-generation (iterate evolve) (take 17) (apply list)))

#_(clerk/show! "notebooks/rule_30.clj")
