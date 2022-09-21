;; # Table Viewer

(ns exercises.ex-2-table-viewer
  {::clerk/visibility {:code :fold}}
  (:require [nextjournal.clerk :as clerk]))

;; ### Supported Formats
;; What are the different formats that the Clerk table viewer supports? Write a small example using each of them. Also use `clerk/use-headers`.

;; ### Column Ordering
;; Which of the formats above supports explicit column ordering? Write a small example for each one.

;; ### Composing Viewers

;; The `sparkline` function takes a sequence of numbers and draws a
;; sparkline. Use it to make the table below show a sparkline in its
;; first column.

(defn sparkline [values]
  (clerk/vl {:data {:values (map-indexed (fn [i v] {:date i :price v}) values)}
             :mark {:type "line" :strokeWidth 1.2}
             :width 140
             :height 20
             :config {:background nil :border nil :view {:stroke "transparent"}}
             :encoding {:x {:field "date" :type "temporal" :axis nil :background nil}
                        :y {:field "price" :type "quantitative" :axis nil :background nil}}}))

(sparkline (shuffle (range 50)))

(defn format-million [value]
  (clerk/html
   [:div.text-right.tabular-nums
    [:span.text-slate-400 "$M "] [:span (format "%,12d" value)]]))

(defn format-percent [value]
  (clerk/html
   [:div.text-right.tabular-nums
    (if (neg? value)
      [:span.text-red-500 "–"]
      [:span.text-green-500 "+"])
    [:span (format "%.2f" (abs value))]
    [:span.text-slate-400 "%"]]))


^{::clerk/viewer clerk/table ::clerk/width :full}
(def prices-table
  {"" (repeatedly 5 #(shuffle (range 50)))
   "Assets" (map format-million [64368 62510 50329 47355 40500])
   "Fund" ["Vanguard 500" "Fidelity Magellan" "Amer A Invest" "Amer A WA" "Pimco"]
   "4 wks" (map format-percent [-2.0 -2.1 -1.2 -1.5 -2.3])
   "2003" (map format-percent [12.2 11.3 9.4 9.4 2.4])
   "3 years" (map format-percent [-11.7 -12.9 -3.9 0.8 9.4])
   "5 years" (map format-percent [-0.8 -0.2 4.0 3.0 7.6])})

