;; # Clerk PX/23 👋

;; Clerk is programmer's assistant for Clojure.
;;
;; Clerk's audience is Clojure developers familiar with REPL-driven
;; development. The main idea is to developers use their familiar
;; toolkit:

;; * whatever your favorite editor is
;; * no custom format, regular Clojure namespaces with markdown line comments
;; * plays nice with version control


;; Let me give you a quick tour of Clerk.


(ns demo
  (:require [nextjournal.clerk :as clerk]))

;; Using Emacs on the left here, Clerk's view of this document on the
;; right. I have a hotkey in my editor which makes Clerk display the
;; currently open document.

(map inc (range 30))

;; Note how Clerk paginates the data and allows me to load more on
;; demand.

#_{:hello (map inc (range 30))}

;; Clerk comes with a rich, built-in set of viewers (code, math, vega,
;; plotly, tables, markdown, grid layout, images).

;; Extensible viewer api to mold Clerk to your problem at hand. Let's
;; look an an example of this.

(def rule-30
  {[1 1 1] 0
   [1 1 0] 0
   [1 0 1] 0
   [1 0 0] 1
   [0 1 1] 1
   [0 1 0] 1
   [0 0 1] 1
   [0 0 0] 0})

(def first-generation
  (let [n 33]
    (assoc (vec (repeat n 0)) (/ (dec n) 2) 1)))

(let [evolve #(mapv rule-30 (partition 3 1 (repeat 0) (cons 0 %)))]
  (->> first-generation (iterate evolve) (take 17) (apply list)))

(clerk/add-viewers!
 [#_{:pred #{0 1}
     :render-fn '(fn [n] [:div.inline-block {:style {:width 16 :height 16}
                                            :class (if (pos? n) "bg-black" "bg-white border-solid border-2 border-black")}])}
  #_{:pred (every-pred vector? (complement map-entry?) (partial every? number?))
     :render-fn '(fn [row opts] (into [:div.flex.inline-flex] (nextjournal.clerk.render/inspect-children opts) row))}
  #_{:pred (every-pred list? (partial every? (some-fn number? vector?)))
     :render-fn '(fn [rows opts] (into [:div.flex.flex-col] (nextjournal.clerk.render/inspect-children opts) rows))}])


;; Clerk's evaluation unit is the document, it caches expressions on the level of top-level forms.

;; Uses static analysis to build up a dependency graph of Clojure vars and only evaluate forms it hasn't yet cached.


(def expensive-answer
  (do
    (Thread/sleep 2000)
    42))


(inc expensive-answer)

;; In contrast to regular Clojure, Clerk will warn the programmer when referencing vars that can't be found in the file anymore.

;; That's our quick demo of Clerk. 🙏

