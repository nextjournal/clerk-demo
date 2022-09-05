```clojure
(ns index
  {:nextjournal.clerk/visibility {:code :hide}}
  (:require [clojure.string :as str]
            [nextjournal.clerk :as clerk]))
```

# 🎪 Clerk Demo [on GitHub](https://github.com/nextjournal/clerk-demo)

```clojure
(clerk/html
  (into
    [:div.md:grid.md:gap-8.md:grid-cols-2.pb-8]
    (map
      (fn [{:keys [path preview title description]}]
        [:a.rounded-lg.shadow-lg.border.border-gray-300.relative.flex.flex-col.hover:border-indigo-600.group.mb-8.md:mb-0
         {:href (clerk/doc-url path)
          :style {:height 300}}
         [:div.flex-auto.overflow-hidden.rounded-t-md.flex.items-center.px-3.py-4
          [:img {:src preview :width "100%" :style {:object-fit "contain"}}]]
         [:div.sans-serif.border-t.border-gray-300.px-4.py-2.group-hover:border-indigo-600
          [:div.font-bold.block.group-hover:text-indigo-600 title]
          [:div.text-xs.text-gray-500.group-hover:text-indigo-600.leading-normal description]]])
      [{:title "Introduction"
        :preview "https://cdn.nextjournal.com/data/Qmb7qfVDvgcfeEQrfcPwD1DFipw8TuyW8Rno33NAJSYDjr?filename=introduction.png&content-type=image/png"
        :path "notebooks/introduction.clj"
        :description "A first look at Clerk, including many small usage examples."}
       {:title "Data Science"
        :preview "https://cdn.nextjournal.com/data/QmcznoqioDQUKbH777pacrT4LDTwqmP1nmw7bfQ7uXrZQh?filename=CleanShot%202021-11-30%20at%2018.19.07@2x.png&content-type=image/png"
        :path "notebooks/data_science.clj"
        :description "An exploration of open data using Clerk."}
       {:title "Semantic Queries"
        :preview "https://cdn.nextjournal.com/data/QmYH2gBYSifEgNJDjGUjVAAaHWt5c4WD6Ko7Be7Be5f4hy?filename=CleanShot%202021-11-30%20at%2018.26.23@2x.png&content-type=image/png"
        :path "notebooks/semantic.clj"
        :description "A starter kit for interactively exploring WikiData's vast semantic database."}
       {:title "Images"
        :preview "https://upload.wikimedia.org/wikipedia/commons/thumb/3/31/The_Sower.jpg/1510px-The_Sower.jpg"
        :path "notebooks/images.clj"
        :description "Some examples about dealing with images."}
       {:title "The double pendulum"
        :preview "https://cdn.nextjournal.com/data/Qmdhk9WEogAvH9cgehnpq3REkATa91JiWgYNUtoSW74Q9A?filename=CleanShot%202021-11-30%20at%2018.45.46@2x.png&content-type=image/png"
        :path "notebooks/sicmutils.clj"
        :description "Simulate and visualize physical systems from the REPL."}
       {:title "Rule 30"
        :preview "https://cdn.nextjournal.com/data/QmQCrqkdYtKfNm9CGbXhzY4cy6qG8xhpWaCRPF5m6biLgV?filename=CleanShot%202021-11-30%20at%2018.46.55@2x.png&content-type=image/png"
        :path "notebooks/rule_30.clj"
        :description "Cellular automata meet moldable viewers. 👾"}])))
```
