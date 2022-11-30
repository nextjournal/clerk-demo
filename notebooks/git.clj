;; # 🕰️ GIT timeline for this project
(ns timeline
  {:nextjournal.clerk/visibility {:code :hide :result :hide}}
  (:require [nextjournal.clerk :as clerk]
            [clj-jgit.porcelain :as cg]
            [clj-jgit.querying :as cgq]
            [clojure.set :as set]
            [nextjournal.clerk.viewer :as v]))

(def clerk-repo
  (cg/load-repo ".git"))

(def commits
  (->> (cg/git-log clerk-repo)
       (map (comp (partial cgq/commit-info clerk-repo) :id))
       reverse
       (into [])))

{:nextjournal.clerk/visibility {:result :show}}

(clerk/with-viewer
  {:transform-fn (comp v/mark-presented
                       (v/update-val (partial map
                                              (fn [c]
                                                (update (select-keys c [:id :changed_files :time])
                                                        :time #(subs (str %) 4 10))))))
   :render-fn '(fn [commits]
                 [:div.min-h-screen.py-6.flex.flex-col.justify-center.sm:py-12.font-sans
                  [:div.py-3.sm:max-w-xl.sm:mx-auto.w-full.px-2.sm:px-0
                   (into
                    [:div.relative.text-gray-700.antialiased.text-xs
                     [:div {:class "hidden sm:block w-1 bg-gray-300 absolute h-full left-1/2 transform -translate-x-1/2"}]]
                    (mapv (fn [i c]
                            [:div.mt-6.sm:mt-0.sm:mb-12 {:id (:id c)}
                             [:div.flex.flex-col.sm:flex-row.items-center
                              [:div {:class "bg-white w-12 h-12 absolute left-1/2 -translate-y-4 sm:translate-y-0 transform -translate-x-1/2 flex items-center justify-center"}
                               [:svg.h-10.w-12.pl-2.text-gray-500 {:xmlns "http://www.w3.org/2000/svg"
                                                                   :fill "none"
                                                                   :viewbox "0 0 32 32"
                                                                   :stroke "currentColor"}
                                [:path {:transform "translate(5,0)"
                                        :stroke-width "2"
                                        :d "M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"}]
                                [:text.font-sans {:x 0 :y 36 :fill "currentColor"} (:time c)]]]
                              [:div {:class (str "flex " (if (even? i) "justify-start" "justify-end")
                                                 " w-full mx-auto items-center")}
                               [:div {:class (str "w-full sm:w-1/2 " (if (even? i) "sm:pr-8" "sm:pl-8"))}
                                (into
                                 [:div {:class "p-4 bg-white rounded shadow"}]
                                 (mapv (fn [[filename status]]
                                         [:span {:class (cond (= status :add) "text-lime-500"
                                                              (= status :delete) "text-rose-600"
                                                              :else "black")} filename [:br]])
                                       (:changed_files c)))]]]])
                          (range)
                          commits))]])}
 commits)
