;; # TeX 🧮
;; ## With KateX and MathJax
(ns tex (:require [nextjournal.clerk :as clerk]))

;; The Einstein-Field Equations are:
(clerk/tex "G_{\\mu\\nu}\\equiv R_{\\mu\\nu} - {\\textstyle 1 \\over 2}R\\,g_{\\mu\\nu} = {8 \\pi G \\over c^4} T_{\\mu\\nu}")

;; Maxwell's equations in differential form:
(clerk/tex "
\\begin{alignedat}{2}
  \\nabla\\cdot\\vec{E} = \\frac{\\rho}{\\varepsilon_0} & \\qquad \\text{Gauss' Law} \\\\
  \\nabla\\cdot\\vec{B} = 0 & \\qquad \\text{Gauss' Law ($\\vec{B}$ Fields)} \\\\
  \\nabla\\times\\vec{E} = -\\frac{\\partial \\vec{B}}{\\partial t} & \\qquad \\text{Faraday's Law} \\\\
  \\nabla\\times\\vec{B} = \\mu_0\\vec{J}+\\mu_0\\varepsilon_0\\frac{\\partial\\vec{E}}{\\partial t} & \\qquad \\text{Ampere's Law}
\\end{alignedat}
")
