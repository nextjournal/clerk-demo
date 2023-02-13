(ns gigasquid.utils
  (:require
   [clojure.string :as string]
   [clojure.java.shell :as sh]
   [clojure.pprint :refer [pprint]])
  (:import [java.io File]))

(def is-linux?
  (= "linux"
     (-> "os.name"
         System/getProperty
         string/lower-case)))

(def is-mac?
  (-> "os.name"
      System/getProperty
      string/lower-case
      (string/starts-with? "mac")))

(defn display-image
  "Display image on OSX or on Linux based system"
  [image-file]
  (cond
    is-mac?
    (sh/sh "open" image-file)

    is-linux?
    (sh/sh "display" image-file)))

(defn create-tmp-file
  "Return full path of temporary file.

  Example:
  (create-tmp-file \"tmp-image\" \".png\") "
  [prefix ext]
  (File/createTempFile prefix ext))
