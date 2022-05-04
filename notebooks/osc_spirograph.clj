;; # ꩜ An OSC _fourieristic_ Spirograph
;; _This short text shows how to use an [OSC](https://en.wikipedia.org/wiki/Open_Sound_Control)
;; driven controller running on your phone to interact with vector graphic animations in a Clerk notebook. OSC is generally employed in live multimedia
;; devices and sound synthesizers, but as [remarked a while ago by Joe Armstrong](https://joearms.github.io/published/2016-01-28-A-Badass-Way-To-Connect-Programs-Together.html)
;; its properties make it an interesting choice for exchanging data across machines in a broader range of applications._
^{:nextjournal.clerk/visibility :hide-ns}
(ns osc-spirograph
  (:require [nextjournal.clerk :as clerk]
            [clojure.java.io :as io])
  (:import (com.illposed.osc ConsoleEchoServer OSCMessageListener OSCMessageEvent OSCMessage)
           (com.illposed.osc.messageselector JavaRegexAddressMessageSelector)
           (org.slf4j LoggerFactory)
           (java.net InetSocketAddress)
           (javax.imageio ImageIO)))

^{::clerk/visibility :fold :nextjournal.clerk/viewer :hide-result}
(def client-model-sync
  ;; This viewer is used to sync models between clojure values and those on the client side
  {:fetch-fn (fn [_ x] x)
   :transform-fn (fn [{::clerk/keys [var-from-def]}] {:value @@var-from-def})
   :render-fn '(fn [{:keys [value]}]
                 (defonce model (atom nil))
                 (-> (swap! model (partial merge-with (fn [old new] (if (vector? old) (mapv merge old new) new))) value)
                     (update :phasors (partial mapv #(dissoc % :group)))
                     (dissoc :drawing :curve)))})

;; This is the model representing the constituents of our spirograph.
;; Three [phasors](https://en.wikipedia.org/wiki/Phasor), each one carrying an amplitude and an angular frequency.
^{::clerk/viewer client-model-sync}
(defonce model
  (atom {:phasors [{:amplitude 0.41 :frequency 0.46}
                   {:amplitude 0.46 :frequency -0.44}
                   {:amplitude 1.00 :frequency -0.45}]}))

;; Our drawing is a function of time with values in the complex plane.
;;
;; $$\zeta(t) = \sum_{k=1}^3 \mathsf{amplitude}_k\,\large{e}^{2\pi\,\mathsf{frequency}_k \,i\, t}$$
;;

^{::clerk/visibility :fold ::clerk/viewer :hide-result}
(def spirograph-viewer
  {:render-fn '(fn [_]
                 (v/html
                   [v/with-d3-require {:package "two.js@0.7.13"}
                    (fn [Two]
                      (reagent/with-let
                        [Vector (.-Vector Two) Line (.-Line Two) Group (.-Group Two)
                         world-matrix (.. Two -Utils -getComputedMatrix)
                         R 200 MAXV 1000 time-scale 0.09 frequency-factor (* 2 js/Math.PI 0.025)
                         arm-color ["#f43f5e" "#65a30d" "#4338ca"] ;; [ r , g , b ]
                         phasor-group (fn [drawing parent {:keys [amplitude color]}]
                                        (let [G (doto (Group.)
                                                  (j/assoc! :position
                                                            (j/get-in parent [:children 0 :vertices 1]
                                                                      (Vector. (/ (.-width drawing) 2)
                                                                               (/ (.-height drawing) 2)))))]
                                          (.add parent G)
                                          (.add G (doto (Line. 0.0 0.0 (* amplitude R) 0.0)
                                                    (j/assoc! :linewidth 7)
                                                    (j/assoc! :stroke color)
                                                    (j/assoc! :cap "round")))
                                          G))
                         build-phasors (fn [{:as m :keys [drawing]}]
                                         (update m :phasors (fn [phasors]
                                                              (->> phasors
                                                                   (transduce (map-indexed (fn [i ph] (assoc ph :color (arm-color i))))
                                                                              (fn
                                                                                ([] {:phasors [] :parent-group (.-scene drawing)})
                                                                                ([ret] (:phasors ret))
                                                                                ([{:as acc :keys [parent-group]} params]
                                                                                 (let [g (phasor-group drawing parent-group params)]
                                                                                   (-> acc
                                                                                       (update :phasors conj (-> params (assoc :group g) (dissoc :color)))
                                                                                       (assoc :parent-group g))))))))))
                         update-phasor! (fn [{:keys [amplitude frequency group]} dt]
                                          (when group
                                            (j/assoc-in! group [:children 0 :vertices 1 :x] (* amplitude R))
                                            (j/update! group :rotation + (* frequency-factor frequency dt))))
                         build-curve (fn [{:as m :keys [drawing]}]
                                       (assoc m :curve
                                                (doto (.makeCurve drawing)
                                                  (j/assoc! :closed false)
                                                  (j/assoc! :stroke "#5b21b6")
                                                  (j/assoc! :linewidth 5)
                                                  (j/assoc! :opacity 0.8)
                                                  .noFill)))
                         pen-position (fn [{:keys [phasors]}]
                                        (let [{:keys [amplitude group]} (last phasors)]
                                          (.copy (Vector.)
                                                 (-> group world-matrix (.multiply (* amplitude R) 0.0 1.0)))))
                         ->color (fn [{:keys [phasors]}]
                                   (let [[r g b] (map (comp js/Math.floor (partial * 200) :amplitude) phasors)]
                                     (str "rgb(" r "," g "," b ")")))
                         update-curve! (fn [{:as model :keys [drawing mode curve]} dt]
                                         (when curve
                                           (let [vxs (.-vertices curve) size (.-length vxs)]
                                             (case (or mode 0)
                                               0            ;; spirograph
                                               (.push vxs (pen-position model))
                                               1            ;; fourier
                                               (doto vxs
                                                 (.push (j/assoc! (pen-position model) :x (/ (.-width drawing) 2)))
                                                 (.forEach (fn [p] (j/update! p :x - dt))))
                                               nil)
                                             (when (< MAXV size) (.splice vxs 0 (- size MAXV)))
                                             (j/assoc! curve :stroke (->color model)))))
                         apply-model (fn [{:as model :keys [clean? curve drawing phasors]} dt]
                                       (doseq [rot phasors] (update-phasor! rot dt))
                                       (js/requestAnimationFrame #(update-curve! model dt)) ;; draw curve at next tick
                                       (when clean? (.remove drawing curve))
                                       (cond-> model clean? build-curve))
                         update! (fn [_frames dt] (swap! model apply-model (* time-scale dt)))
                         refn (fn [el]
                                (when (and el (not (:drawing @model)))
                                  (let [drawing (Two. (j/obj :type (.. Two -Types -svg) :autostart true :fitted true))]
                                    (.appendTo drawing el)
                                    (.bind drawing "update" update!)
                                    (swap! model #(-> % (assoc :drawing drawing) build-phasors build-curve)))))]
                        [:div {:ref refn :style {:width "100%" :height "800px"}}]))]))})

^{::clerk/width :full ::clerk/visibility :hide ::clerk/viewer spirograph-viewer}
(Object.)

;; We'll be interacting with the spirograph by means of [TouchOSC](https://hexler.net/touchosc) an application for building OSC (or MIDI) driven interfaces runnable on smartphones and the like.
;; Our controller is looking like this:
^{::clerk/visibility :hide}
(ImageIO/read (io/resource "spirograph.png"))
;; the linear faders on the left will control the phasors amplitudes while the radial ones change their frequencies. This
;; specific layout is saved in [this file](https://github.com/zampino/osc-spirograph/blob/main/spirograph.tosc).
;;
;; OSC binary messages are composed of an _address_ and sequential _arguments_. We configured our interface to emit message
;; arguments of the form `[value & path]` where the first entry is an integer in the range `0` to `100` while the tail is a valid path in
;; the model. We're actually ignoring the message address.
;;
;; In order to receive OSC messages, we instantiate an OSC Server. We're overlaying an extra broadcast layer on top of the simple echo server
;; provided by the [JavaOSC library](https://github.com/hoijui/JavaOSC). This will, in addition, allow to debug incoming messages in the terminal.
;;
;; Received events are tapped into the JVM for them to be handled with clojure functions, this piece shows Java interop at its best!
(when-not (System/getenv "NOSC")
  (defonce osc
    (doto (proxy [ConsoleEchoServer]
                 [(InetSocketAddress. "0.0.0.0" 6669)
                  (LoggerFactory/getLogger ConsoleEchoServer)]
            (start []
              (proxy-super start)
              (.. this
                  getDispatcher
                  (addListener (JavaRegexAddressMessageSelector. ".*")
                               (reify
                                 OSCMessageListener
                                 (^void acceptMessage [_this ^OSCMessageEvent event]
                                   (tap> (.getMessage event))))))))
      .start)))

;; Next, we need a function to convert OSC messages into normalized clojure data
(defn osc->map [^OSCMessage m]
  (let [[v & path] (map #(cond-> % (string? %) keyword) (.getArguments m))]
    {:value (if (= :phasors (first path)) (float (/ v 100)) v)
     :path path}))

;; and a helper for updating our model and recomputing the notebook
(defn update-model! [f]
  (swap! model f)
  (binding [*ns* (find-ns 'osc-spirograph)]
    (clerk/recompute!)))

;; finally, a message handler to be added to tap callbacks
(defn osc-message-handler [osc-message]
  (let [{:keys [path value]} (osc->map osc-message)]
    (update-model! #(assoc-in % path value))))

;; Clerk won't cache forms returning nil values, hence the do here to ensure we register our handler just once when the notebook is evaluated
(do
  (add-tap osc-message-handler)
  true)

;; And that's it I guess. Now, if you're looking at a static version of this notebook, you might want to clone [this repo](https://github.com/zampino/osc-spirograph), launch
;; Clerk with `(nextjournal.clerk/serve! {})` and see it in action with `(nextjournal.clerk/show! "notebooks/osc_spirograph.clj")`.
;;
;; This project has been partly inspired by Jack Schaedler's interactive article ["SEEING CIRCLES, SINES, AND SIGNALS"](https://jackschaedler.github.io/circles-sines-signals/index.html)
;; to which I refer the reader to explore the implications of Fourier analysis with digital signal processing.

^{::clerk/visibility :hide ::clerk/viewer :hide-result}
(comment
  (clerk/serve! {:port 7779})
  (clerk/clear-cache!)

  (remove-tap osc-message-handler)

  (.start osc)
  (.isListening osc)
  (.stopListening osc)

  (update-model (fn [m] (assoc-in m [:phasors 0 :amplitude] 2.0)))
  (update-model (fn [m] (assoc-in m [:phasors 1 :frequency] 0.9)))

  ;; save nice models
  @model
  (do
    (reset! model
            #_
            {:mode 0,
                :phasors [{:amplitude 0.4, :frequency 0.2}
                         {:amplitude 1.0, :frequency -0.2}
                         {:amplitude 0.4, :frequency 0.6}]}
            #_ {:mode 0
             :phasors [{:amplitude 0.41, :frequency 0.46}
                       {:amplitude 0.71, :frequency -0.44}
                       {:amplitude 0.6, :frequency -0.45}]}

            #_ {:mode 0,
             :phasors [{:amplitude 0.41, :frequency 0.46}
                       {:amplitude 0.46, :frequency -0.44}
                       {:amplitude 1.0, :frequency -0.45}]}
            #_
            {:mode 0
             :phasors [{:amplitude 0.57, :frequency 0.39}
                        {:amplitude 0.5, :frequency -0.27}
                        {:amplitude 0.125, :frequency 0.27}]}

            #_ {:mode 0,
                :phasors [{:amplitude 0.72, :frequency -0.25}
                         {:amplitude 0.59, :frequency 0.45}
                         {:amplitude 0.52, :frequency 0.3}]}

            {:mode 0,
             :phasors [{:amplitude 0.80, :frequency 0.55}
                       {:amplitude 0.5, :frequency -0.27}
                       {:amplitude 0.75, :frequency 0.27}]})
    (clerk/recompute!))

  ;; clean
  (do (swap! model assoc :clean? true)
      (clerk/recompute!)
      (swap! model assoc :clean? false)
      (clerk/recompute!)))
