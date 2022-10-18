;; # Viewer Require

(ns exercises.ex-4-viewer-require
  (:require [nextjournal.clerk :as clerk]))

;; Complete the following viewer to dynamically require the `emoji-js`
;; package so you see an emoji at the end.

;; Hint: once you loaded the package using `v/with-d3-require`, you
;; will need to instantiate the emoji converter and use it's
;; `.replace_colons` function. There's some code to help you with this
;; at the bottom of this doc if you're stuck.

(def emoji-viewer
  {:transform-fn clerk/mark-presented
   :render-fn '(fn [value]
                 (v/html value))})

(clerk/with-viewer emoji-viewer
  "Hallo :smile:")
































^{::clerk/visibility {:code :hide :result :hide}}
(comment
  (fn [EmojiConverter]
    (let [emoji (EmojiConverter.)]
      (.replace_colons emoji value))))
