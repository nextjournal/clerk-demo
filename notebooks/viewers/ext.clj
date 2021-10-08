;; # Viewer Extensibility
(require '[nextjournal.clerk :as clerk])

;; The viewer system is open and extensible.

;; We use the Small Clojure Interpreter to evaluate Clojure in the browser.

(clerk/set-viewers!
 [{:name ::cell :fn #(v/html
                      [:div.inline-block
                       {:class (if (zero? %)
                                 "bg-white border-solid border-2 border-black"
                                 "bg-black")
                        :style {:width 16 :height 16}}])}
  {:name ::row :fn (fn [x options]
                     (v/html
                      (into [:div.flex.inline-flex]
                            (map (partial v/inspect options))
                            x)))}
  {:name ::board :fn (fn [x options]
                       (v/html
                        (into [:div.flex.flex-col]
                              (map (partial v/inspect options))
                              x)))}])

(v/with-viewer ::cell 0)

(v/with-viewer ::cell 1)

(v/with-viewer ::row [0 1 0])
