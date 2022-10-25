# 🤹 Clerk Demo

This is a bucket of interesting Clerk demos. See https://github.clerk.garden/nextjournal/clerk-demo.

## Usage

To play with this, you need to have [Clojure
installed](https://clojure.org/guides/install_clojure), then run:

``` shell
clj -M:nextjournal/clerk nextjournal.clerk/serve! --watch-paths notebooks --port 7777 --browse
```

This will start the Clerk webserver on port 7777 and watch the
`notebooks/` directory for changes and open Clerk in your
browser. 

Open one of the files there, e.g. `rule_30.clj`, make a
change and save it. You should then see these changes reflected in the
browser.

## From your Editor

For interactive development, it is recommended you let your editor
start the project (`jack-in`), if asked you should select `deps.edn` as
the project type.

Then, evaluate forms in `dev/user.clj`.
