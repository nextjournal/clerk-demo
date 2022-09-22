# Hello there 👋

`clerk-slideshow` enables you to create beautiful interactive slide decks
using Clerk notebooks.

---

## How does it work?

Simply require `clerk-slideshow`…

```clojure
(ns simple-slideshow
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk-slideshow :as slideshow]))
```

…and add it to Clerk’s existing viewers:

```clojure
^{::clerk/visibility {:result :hide}}
(clerk/add-viewers! [slideshow/viewer])
```

---

## What now?

With that in place, you can use Markdown comments to write your slides’ content.
Use Markdown rulers (`---`) to separate your slides. You can use everything that
you’ll normally use in your Clerk notebooks:
Markdown, plots, code blocks, you name it.

Press `←` and `→` to navigate between slides or `Escape` to get an overview.

Now some demos 👉

---

## 📊 A Plotly graph

```clojure
^{::clerk/visibility {:code :hide}}
(clerk/plotly {:data [{:z [[1 2 3] [3 2 1]] :type "surface"}]})
```

---

## 📈 A Vega Lite graph

```clojure
^{::clerk/visibility {:code :hide}}
(clerk/vl {:width 650 :height 400 :data {:url "https://vega.github.io/vega-datasets/data/us-10m.json"
                                         :format {:type "topojson" :feature "counties"}}
           :transform [{:lookup "id" :from {:data {:url "https://vega.github.io/vega-datasets/data/unemployment.tsv"}
                                            :key "id" :fields ["rate"]}}]
           :projection {:type "albersUsa"} :mark "geoshape" :encoding {:color {:field "rate" :type "quantitative"}}})
```

---

## And that’s it for now! 👋

More demos will follow soon!
