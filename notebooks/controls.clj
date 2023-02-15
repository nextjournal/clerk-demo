;; # Controls! 🎛
^{:nextjournal.clerk/visibility {:code :hide}}
(ns ^:nextjournal.clerk/no-cache controls
  (:require [nextjournal.clerk :as clerk]))

;; As we've seen in other notebooks, anyone using Clerk can extend the
;; viewers however they like without changing the source code of Clerk
;; itself. This includes being able to make interactive controls that
;; call back into Clerk's runtime to provide two-way binding. For
;; example, we can create a viewer that presents an atom-wrapped
;; number as a slider like this:

(def slider
  {:var-from-def? true
   :transform-fn (comp clerk/mark-presented
                       (clerk/update-val (fn [{::clerk/keys [var-from-def]}]
                                           {:var-name (symbol var-from-def) :value @@var-from-def})))
   :render-fn '(fn [{:keys [var-name value]}]
                 [:input {:type :range
                          :initial-value value
                          :on-change #(nextjournal.clerk.render/clerk-eval `(reset! ~var-name (Integer/parseInt ~(.. % -target -value))))}])})

;; And then assign is to a particular atom like this:
^{::clerk/viewer slider}
(defonce bar (atom 0))

;; Clerk will always check to see if the value of an atom reference
;; has been updated, so we see realtime updates as we drag the slider.
@bar

;; As we can see above, a viewer is just a map that supplies a set of
;; functions to Clerk. This is really convenient because it means we
;; can define a set of viewers or controls and re-use them wherever we
;; like!

;; Now, let's build an example two-way binding for a text field, but
;; with comments inline to help explain how everything works:

(def text-input
  ;; We're telling Clerk to opt-out of the default behaviour (acting on the value held by
  ;; the var) and return a map containing the actual var object instead.
  {:var-from-def? true

   ;; When we specify a `:transform-fn`, it gets run on the JVM side
   ;; to pre-process our value before sending it to the front-end. In
   ;; this case we want to send the symbol for the var along with the
   ;; unwrapped value because our custom renderer will need to know
   ;; both of those things (see below).
   ;;
   ;; Normally, Clerk's front-end is very careful to fetch data from
   ;; the JVM is bite-sized chunks to avoid killing the browser. But
   ;; sometimes we need to override that mechanism, which is done by
   ;; calling `clerk/mark-presented` to ask Clerk to send the whole
   ;; value as-is to the browser.
   :transform-fn (comp clerk/mark-presented
                       (clerk/update-val (fn [{::clerk/keys [var-from-def]}]
                                           {:var-name (symbol var-from-def) :value @@var-from-def})))

   ;; The `:render-fn` is the heart of any viewer. It will be executed
   ;; by a ClojureScript runtime in the browser, so — unlike these
   ;; other functions — we must quote it for transmission over the
   ;; wire.
   ;;
   ;; The interactive two-way binding magic comes from using Clerk's
   ;; support for Hiccup to produce an input tag whose `:on-input`
   ;; handler calls `clerk-eval` to send a quoted form back to the
   ;; JVM. This ability to easily transmit arbitrary code between the
   ;; front- and back- ends of the system is extremely
   ;; powerful — thanks, Lisp!
   :render-fn '(fn [{:keys [var-name value]}]
                 [:input {:type :text
                          :placeholder "⌨️"
                          :initial-value value
                          :class "px-3 py-3 placeholder-blueGray-300 text-blueGray-600 relative bg-white bg-white rounded text-sm border border-blueGray-300 outline-none focus:outline-none focus:ring w-full"
                          :on-input #(nextjournal.clerk.render/clerk-eval `(reset! ~var-name ~(.. % -target -value)))}])})

^{::clerk/viewer text-input}
(defonce text-state (atom ""))

@text-state

;; Given the flexibility of this mechanism, we hope to see all sorts
;; of interesting things that we never imagined developed using
;; Clerk's interactive viewers. 😀

;; In the meantime, to see an example of a small application built
;; using these viewers, check out the Dictionary notebook.


#_(clerk/clear-cache!)
