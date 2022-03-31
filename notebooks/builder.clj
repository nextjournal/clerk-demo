;; # 👷🏼 Garden Builder
(ns builder
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.hashing :as h]))

;; ## Initial state
;; We start with an initial fileset from `deps.edn`.
(def paths
  (-> "deps.edn" slurp h/read-string (get-in [:aliases :nextjournal/clerk :exec-args :paths])))

;; Our initial state is a seq of maps with only a `:file` key. This is the first state we want to visualize. Successive states should add to this map.
(def initial-state
  (mapv #(hash-map :file %) paths))

;; ## Parsing
;; We parse & hash all files at once to fail early.
(def parsed
  (mapv (comp (partial h/parse-file {:doc? true}) :file) initial-state))

(def parsed-state
  (mapv (fn [{:as f :keys [blocks]}]
          (-> f
              (select-keys [:file])
              (assoc :block-counts (frequencies (map :type blocks)))))
        parsed))


;; ## Execution Progress
;; 🚧
