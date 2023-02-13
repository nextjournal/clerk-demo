(ns gigasquid.plot
  (:require
            ; [libpython-clj.python :as py :refer [py. py.. py.-]]
   [libpython-clj2.python
    :refer [as-python as-jvm
            ->python ->jvm
            get-attr call-attr call-attr-kw
            get-item initialize!
            run-simple-string
            add-module module-dict
            import-module
            python-type
            py. py.. py.-]
    :as py]
   [libpython-clj2.require :refer [require-python]]
   [gigasquid.utils :refer [display-image create-tmp-file]]
   [clojure.java.shell :as sh]))

;;; This uses the headless version of matplotlib to generate a graph then copy it to the JVM
;;; where we can then print it

;;; have to set the headless mode before requiring pyplot
(def mplt (py/import-module "matplotlib"))
(py. mplt "use" "Agg")

(require-python '[matplotlib.pyplot :as pyplot])
(require-python 'matplotlib.backends.backend_agg)
(require-python 'numpy)

(defmacro with-show
  "Takes forms with mathplotlib.pyplot to then show locally"
  [& body]
  `(let [_# (pyplot/clf)
         fig# (pyplot/figure)
         agg-canvas# (matplotlib.backends.backend_agg/FigureCanvasAgg fig#)
         temp-file# (create-tmp-file "tmp-image" ".png")
         temp-image# (.getAbsolutePath temp-file#)]
     ~(cons 'do body)
     (py. agg-canvas# "draw")
     (pyplot/savefig temp-image#)
     (display-image temp-image#)
     (.deleteOnExit temp-file#)))

;;;; If you run into mem problems with temporary files try this one

(defmacro with-show-one
  "Takes forms with mathplotlib.pyplot to then show locally"
  [& body]
  `(let [_# (pyplot/clf)
         fig# (pyplot/figure)
         agg-canvas# (matplotlib.backends.backend_agg/FigureCanvasAgg fig#)]
     ~(cons 'do body)
     (py. agg-canvas# "draw")
     (pyplot/savefig "temp.png")
     (sh/sh "open" "temp.png")))

(defmacro with-save
  "Takes forms with mathplotlib.pyplot to then show locally"
  [fname & body]
  `(let [_# (pyplot/clf)
         fig# (pyplot/figure)
         agg-canvas# (matplotlib.backends.backend_agg/FigureCanvasAgg fig#)]
     ~(cons 'do body)
     (py. agg-canvas# "draw")
     (pyplot/savefig  ~fname)))

(comment

  (def x (numpy/linspace 0 2 100))

  (with-show
    (pyplot/plot [x x] :label "linear")
    (pyplot/plot [x (py. x "__pow__" 2)] :label "quadratic")
    (pyplot/plot [x (py. x "__pow__" 3)] :label "cubic")
    (pyplot/xlabel "x label")
    (pyplot/ylabel "y label")
    (pyplot/title "Simple Plot"))

  (with-show (pyplot/plot [[1 2 3 4 5] [1 2 3 4 10]] :label "linear")))
