(ns user
  (:require [nextjournal.clerk :as clerk]))

(comment
  ;; start without file watcher, open browser when started
  (clerk/serve! {:browse? true})

  ;; start with file watcher
  (clerk/serve! {:watch-paths ["notebooks" "src"]})

  ;; start with file watcher and show filter function to enable notebook pinning
  (clerk/serve! {:watch-paths ["notebooks" "src"] :show-filter-fn #(clojure.string/starts-with? % "notebooks")})

  ;; open clerk
  (browse/browse-url (str "http://localhost:" port))

  ;; or call `clerk/show!` explicitly
  (clerk/show! "notebooks/introduction.clj") ; combine with "hello" and make a nice intro
  (clerk/show! "notebooks/viewer_api.clj") ; combine these two
  ;; make a notebook for simple "data science" stuff, maybe using kixi?
  ;; * read a CSV
  ;; * take some summary stats
  ;; * graph a few things
  ;; could use setosa, or maybe grab a more interesting data set?
  (clerk/show! "notebooks/viewers/table.clj")

  (clerk/show! "notebooks/rule_30.clj")
  (clerk/show! "notebooks/semantic.clj")
  
  ;; if you want to know more of the details about how it works and
  ;; why it's reasonably fast
  (clerk/show! "notebooks/how_clerk_works.clj")

  ;; TODO move to tests?
  (clerk/show! "notebooks/pagination.clj")

  ;; remove these in favor of the intro itself
  ;; (clerk/show! "notebooks/viewers/html.clj")
  ;; (clerk/show! "notebooks/viewers/markdown.clj")
  ;; (clerk/show! "notebooks/viewers/tex.clj")
  ;; (clerk/show! "notebooks/viewers/plotly.clj")
  ;; (clerk/show! "notebooks/viewers/vega.clj")

  (clerk/show! "src/arslonga/core.clj")
  ;; produce a static app
  (clerk/build-static-app! {:paths (mapv #(str "notebooks/" % ".clj")
                                         '[rule_30 viewer_api how_clerk_works pagination tablecloth
                                           viewers/html viewers/markdown viewers/plotly viewers/tex viewers/vega])})

  )

