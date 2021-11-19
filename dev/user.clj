(ns user
  (:require [nextjournal.clerk :as clerk]))

(comment
  ;; start without file watcher, open browser when started
  (clerk/serve! {:browse? true})

  ;; start with file watcher for these sub-directory paths
  (clerk/serve! {:watch-paths ["notebooks" "src"]})

  ;; start with file watcher and a `show-filter-fn` function to watch
  ;; a subset of notebooks
  (clerk/serve! {:watch-paths ["notebooks" "src"] :show-filter-fn #(clojure.string/starts-with? % "notebooks")})

  ;; or call `clerk/show!` explicitly
  (clerk/show! "notebooks/introduction.clj") ; combine with "hello" and "pagination" to make a nice intro
  (clerk/show! "notebooks/pagination.clj")
  (clerk/show! "notebooks/viewer_api.clj") ; expand or combine with intro?

  ;; make a notebook for simple "data science" stuff, maybe using kixi?
  ;; * read a CSV
  ;; * take some summary stats
  ;; * graph a few things
  ;; could use setosa, or maybe grab a more interesting data set?
  (clerk/show! "notebooks/data_science.clj")

  (clerk/show! "notebooks/sicmutils.clj")

  ;; done
  (clerk/show! "notebooks/rule_30.clj")
  (clerk/show! "notebooks/semantic.clj")
  
  ;; TODO If you would like more details about how Clerk works, here's a
  ;; notebook with some implementation details.
  (clerk/show! "notebooks/how_clerk_works.clj")

  ;; produce a static app
  (clerk/build-static-app! {:paths (mapv #(str "notebooks/" % ".clj")
                                         '[introduction data_science rule_30 semantic])})

  )

