```clojure
^{:nextjournal.clerk/visibility #{:hide :hide-ns}}
(ns index
  (:require [nextjournal.clerk :as clerk]))
```

# 🎪 Clerk Demo [on GitHub](https://github.com/nextjournal/clerk-demo)

```clojure
(clerk/html
  (into
    [:div.md:grid.md:gap-8.md:grid-cols-2.pb-8]
    (map
      (fn [{:keys [path preview title]}]
        [:a.rounded-lg.shadow-lg.border.border-gray-300.relative.flex.flex-col.hover:border-indigo-600.group
         {:href (str "#/" path)
          :style {:height 300}}
         [:div.flex-auto.overflow-hidden.rounded-t-md.flex.items-center.justify-center.p-3
          [:img {:src preview :width "100%" :style {:object-fit "contain"}}]]
         [:div.font-bold.sans-serif.border-t.border-gray-300.px-4.py-2.group-hover:border-indigo-600.group-hover:text-indigo-600
          title]])
      [{:title "Data Science"
        :preview "https://cdn.nextjournal.com/data/QmcznoqioDQUKbH777pacrT4LDTwqmP1nmw7bfQ7uXrZQh?filename=CleanShot%202021-11-30%20at%2018.19.07@2x.png&content-type=image/png"
        :path "notebooks/data_science.clj"}
       {:title "Semantic Queries"
        :preview "https://cdn.nextjournal.com/data/QmYH2gBYSifEgNJDjGUjVAAaHWt5c4WD6Ko7Be7Be5f4hy?filename=CleanShot%202021-11-30%20at%2018.26.23@2x.png&content-type=image/png"
        :path "notebooks/semantic.clj"}
       {:title "The double pendulum"
        :preview "https://cdn.nextjournal.com/data/Qmdhk9WEogAvH9cgehnpq3REkATa91JiWgYNUtoSW74Q9A?filename=CleanShot%202021-11-30%20at%2018.45.46@2x.png&content-type=image/png"
        :path "notebooks/sicmutils.clj"}
       {:title "Rule 30"
        :preview "https://cdn.nextjournal.com/data/QmQCrqkdYtKfNm9CGbXhzY4cy6qG8xhpWaCRPF5m6biLgV?filename=CleanShot%202021-11-30%20at%2018.46.55@2x.png&content-type=image/png"
        :path "notebooks/rule_30.clj"}])))
```
