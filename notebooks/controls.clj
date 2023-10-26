;; # Controls! 🎛
^{:nextjournal.clerk/visibility {:code :hide}}
(ns controls
  {:nextjournal.clerk/no-cache true}
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.experimental :as cx]))

;; As we've seen in other notebooks, anyone using Clerk can extend the
;; viewers however they like without changing the source code of Clerk
;; itself. This includes being able to make interactive controls that
;; call back into Clerk's runtime to provide two-way binding using
;; `::clerk/sync` and some (experimental!) viewers from
;; `nextjournal.clerk.experimental`. For example, we can create a
;; viewer that presents an atom-wrapped number as a slider like this:

;; And then assign it to a particular atom like this:
^{::clerk/sync true ::clerk/viewer cx/slider}
(defonce bar (atom 0))

;; Clerk will always check to see if the value of an atom reference
;; has been updated, so we see realtime updates as we drag the slider.
@bar

;; There's also a viewer to create a two-way bound text-input.

^{::clerk/sync true ::clerk/viewer cx/text-input}
(defonce text-state (atom "Hey 👋"))

@text-state

;; Given the flexibility of this mechanism, we hope to see all sorts
;; of interesting things that we never imagined developed using
;; Clerk's interactive viewers. 😀

;; In the meantime, to see an example of a small application built
;; using these viewers, check out the Dictionary notebook.


#_(clerk/clear-cache!)
