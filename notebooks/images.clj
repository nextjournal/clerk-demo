;; # 🏞 Automatic Image Support
^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns images
  (:require [nextjournal.clerk :as clerk]
            [clojure.java.io :as io]
            [babashka.http-client :as http])
  (:import [java.net URL]
           [java.nio.file Paths Files]
           [java.awt.image BufferedImage]
           [javax.imageio ImageIO]))

;; Clerk now has built-in support for the
;; `java.awt.image.BufferedImage` class, which is the native image
;; format of the JVM.

;; When combined with `javax.imageio.ImageIO/read`, one can easily load
;; images in a variety of formats from a `java.io.File`, an
;; `java.io.InputStream`, or any resource that a `java.net.URL` can
;; address.

;; For example, we can fetch a photo of _De zaaier_, Vincent van Gogh's famous
;; painting of a farmer sowing a field from Wiki Commons like this. Since Wiki
;; Commons requires a User-Agent header to be set when requesting the image, we
;; use babashka.http-client.
(ImageIO/read
 (-> (http/get "https://upload.wikimedia.org/wikipedia/commons/thumb/3/31/The_Sower.jpg/1510px-The_Sower.jpg"
               {:as :stream})
     :body))

;; We've put some effort into making the default image rendering
;; pleasing. The viewer uses the dimensions and aspect ratio of each
;; image to guess the best way to display it in classic
;; [DWIM](https://en.wikipedia.org/wiki/DWIM) fashion. For example, an
;; image larger than 900px wide with an aspect ratio larger then two
;; will be displayed full width:
(ImageIO/read (URL. "https://images.unsplash.com/photo-1532879311112-62b7188d28ce?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8"))

;; On the other hand, smaller images are centered and shown using
;; their intrinsic dimensions:
(ImageIO/read (URL. "https://nextjournal.com/data/QmeyvaR3Q5XSwe14ZS6D5WBQGg1zaBaeG3SeyyuUURE2pq?filename=thermos.gif&content-type=image/gif"))

;; If you find yourself using a library that returns images as a
;; `ByteArray`, you can read the image into a `BufferedImage` by
;; wrapping it in a `java.io.ByteArrayInputStream` before passing it
;; to `java.imageio.ImageIO/read`.

;; In this example, we'll load computer art pioneer Vera Molnár's 1974
;; work _(Des)Ordres_ into a byte array, then convert it to a
;; `BufferedImage` for display. 😍
(def raw-image
  (Files/readAllBytes (Paths/get "" (into-array ["datasets/images/vera-molnar.jpg"]))))

(with-open [in (io/input-stream raw-image)]
  (ImageIO/read in))

;; In addition to being able to load and use images from many sources,
;; one can also generate images from scratch with code.  Here is an
;; example mathematical butterfly: 🦋
(let [width 800
      height 800
      scale 70
      img (BufferedImage. width height BufferedImage/TYPE_BYTE_BINARY)]
  (doseq [t (range 30000)]
    (let [n (- (Math/pow Math/E (Math/cos t))
               (* 2 (Math/cos (* 4 t)))
               (Math/pow (Math/sin (/ t 12)) 5))
          x (* scale (Math/sin t) n)
          y (* scale (Math/cos t) n)]
      (.setRGB img
               (+ (* 0.5 width) x)
               (+ (* 0.43 height) y)
               (.getRGB java.awt.Color/WHITE))))
  img)

;; ... which should finally let us implement this legendary emacs
;; function:

(ImageIO/read (URL. "https://imgs.xkcd.com/comics/real_programmers.png"))

;; Thanks for reading, and — as always — let us know what you make with Clerk!
