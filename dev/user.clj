(require
 '[clojure.java.browse :as browse]
 '[nextjournal.clerk.webserver :as webserver]
 '[nextjournal.clerk :as clerk]
 '[nextjournal.beholder :as beholder])

(def port 7777)

(webserver/start! {:port port})

(comment
  ;; Optionally start a file-watcher to automatically refresh notebooks when saved
  (def filewatcher
    (beholder/watch #(clerk/file-event %) "notebooks" "src"))

  ;; and stop it again
  (beholder/stop filewatcher)

  ;; open clerk
  (browse/browse-url (str "http://localhost:" port))

  ;; or call `clerk/show!` explicitly
  (clerk/show! "notebooks/introduction.clj")
  (clerk/show! "notebooks/viewer_api.clj")
  (clerk/show! "notebooks/rule_30.clj")
  (clerk/show! "notebooks/elements.clj")
  (clerk/show! "notebooks/pagination.clj")
  (clerk/show! "notebooks/how_clerk_works.clj")
  (clerk/show! "notebooks/tablecloth.clj")

  (clerk/show! "notebooks/viewers/html.clj")
  (clerk/show! "notebooks/viewers/markdown.clj")
  (clerk/show! "notebooks/viewers/plotly.clj")
  (clerk/show! "notebooks/viewers/table.clj")
  (clerk/show! "notebooks/viewers/tex.clj")
  (clerk/show! "notebooks/viewers/vega.clj")

  ;; produce a static build of some notebooks
  ;; ⚠️ warning: this inlines the results.
  ;; This means it's currently only for small results.
  (clerk/build-static-app! {:paths (concat
                                    ["notebooks/rule_30.clj"]
                                    (map #(str "notebooks/viewers/" % ".clj") '[html markdown plotly tex vega]))})
  )
