(ns user
  (:require [nextjournal.clerk :as clerk]))

(comment
  ;; start without file watcher, open browser when started
  (clerk/serve! {:browse? true :port 6677})

  ;; start with file watcher for these sub-directory paths
  (clerk/serve! {:watch-paths ["notebooks" "src"]})

  ;; start with file watcher and a `show-filter-fn` function to watch
  ;; a subset of notebooks
  (clerk/serve! {:watch-paths ["notebooks" "src"] :show-filter-fn #(clojure.string/starts-with? % "notebooks")})

  (clerk/clear-cache!)

  ;; or call `clerk/show!` explicitly
  (clerk/show! "notebooks/introduction.clj")
  (clerk/show! "notebooks/controls.clj")
  (clerk/show! "notebooks/data_science.clj")
  (clerk/show! "notebooks/sicmutils.clj")
  (clerk/show! "notebooks/dictionary.clj")
  (clerk/show! "notebooks/elements.clj")
  (clerk/show! "notebooks/git.clj")
  (clerk/show! "notebooks/logo.clj")
  (clerk/show! "notebooks/rule_30.clj")
  (clerk/show! "notebooks/semantic.clj")
  (clerk/show! "notebooks/images.clj")
  (clerk/show! "notebooks/zipper_with_scars.clj")
  (clerk/show! "notebooks/numpy_plot.clj")
  (clerk/show! "notebooks/python.clj")

  (clerk/show! "index.md")

  ;; TODO If you would like more details about how Clerk works, here's a
  ;; notebook with some implementation details.
  (clerk/show! "notebooks/how_clerk_works.clj")

  ;; produce a static app
  (clerk/build-static-app! {:paths (mapv #(str "notebooks/" % ".clj")
                                         '[introduction data_science rule_30 semantic])})

  )
