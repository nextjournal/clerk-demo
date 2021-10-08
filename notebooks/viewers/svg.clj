;; # SVG 🎨
(require '[nextjournal.clerk.viewer :as v])

(v/svg "<svg viewBox=\"0 0 100 100\">
  <circle r=\"32\" cx=\"35\" cy=\"65\" fill=\"#F00\" opacity=\"0.5\"/>
  <circle r=\"32\" cx=\"65\" cy=\"65\" fill=\"#0F0\" opacity=\"0.5\"/>
  <circle r=\"32\" cx=\"50\" cy=\"35\" fill=\"#00F\" opacity=\"0.5\"/>
</svg>")
