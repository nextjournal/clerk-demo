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
  (clerk/show! "notebooks/rule_30.clj")
  (clerk/show! "notebooks/viewer_api.clj")
  (clerk/show! "notebooks/how_clerk_works.clj")
  (clerk/show! "notebooks/pagination.clj")
  (clerk/show! "notebooks/tablecloth.clj")

  (clerk/show! "notebooks/viewers/html.clj")
  (clerk/show! "notebooks/viewers/markdown.clj")
  (clerk/show! "notebooks/viewers/plotly.clj")
  (clerk/show! "notebooks/viewers/table.clj")
  (clerk/show! "notebooks/viewers/tex.clj")
  (clerk/show! "notebooks/viewers/vega.clj")

  ;; produce a static app
  (clerk/build-static-app! {:paths (mapv #(str "notebooks/" % ".clj")
                                         '[rule_30 viewer_api how_clerk_works pagination tablecloth
                                           viewers/html viewers/markdown viewers/plotly viewers/tex viewers/vega])})

  )
