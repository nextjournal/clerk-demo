;; # 👷🏼 Garden Builder
(ns builder
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.hashing :as h]
            [clojure.string :as str]))

;; ## Initial state
;; We start with an initial fileset from `deps.edn`.
(def paths
  (-> "deps.edn" slurp h/read-string :aliases :nextjournal/clerk :exec-args :paths))

;; Our initial state is a seq of maps with only a `:file` key. This is the first state we want to visualize. Successive states should add to this map.
(def initial-state
  (mapv #(hash-map :file %) paths))

(defn badge [{:keys [block-counts file progress state]}]
  (let [done? (= state :done)]
    [:div.p-1
     [:div.rounded-md.border.border-slate-300.px-4.py-3.font-sans.shadow
      {:class (if done? "bg-green-100" "bg-slate-100")}
      [:div.flex.justify-between.items-center
       [:div.flex.items-center
        (if done?
          [:svg.w-4.h-4.text-green-600
           {:class "mr-[7px] -ml-[2px]" :xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 20 20" :fill "currentColor"}
           [:path {:fill-rule "evenodd" :d "M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" :clip-rule "evenodd"}]]
          [:div.rounded-full.h-3.w-3.mr-2.border.border-slate-400.shadow-inner
           {:class (case state
                     :parsed "bg-orange-300"
                     :executing "bg-green-300"
                     "bg-slate-300")}])
        [:div
         [:div.text-sm.font-medium.leading-none
          file
          (when (seq block-counts)
            [:span.text-slate-600.ml-2.font-normal
             {:class "text-[11px]"}
             (str "(" (str/join ", " (map (fn [[type count]] (str count " " (name type) " blocks")) block-counts)) ")")])]]]
       (when progress
         [:div.rounded-full.h-2.bg-white.border.border-slate-400.overflow-hidden.shadow-inner
          {:class "w-[100px]"}
          [:div.h-full.bg-green-300
           {:style {:width (str (* 100 progress) "%")}}]])]]]))

^{::clerk/viewer {:transform-fn (comp clerk/html badge)}}
(first initial-state)

;; ## Parsing
;; We parse & hash all files at once to fail early.
(def parsed
  (mapv (comp (partial h/parse-file {:doc? true}) :file) initial-state))

(def parsed-state
  (mapv (fn [{:as f :keys [blocks]}]
          (-> f
              (select-keys [:file])
              (assoc :block-counts (frequencies (map :type blocks)))))
        parsed))

^{::clerk/viewer {:transform-fn (comp clerk/html badge)}}
(assoc (first parsed-state) :state :parsed)

;; ## Execution Progress
;; 🚧

^{::clerk/viewer {:transform-fn (comp clerk/html badge)}}
(assoc (first parsed-state) :state :executing :progress 0.7)

;; ## Done
;; 🚧

^{::clerk/viewer {:transform-fn (comp clerk/html badge)}}
(assoc (first parsed-state) :state :done)
