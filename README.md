# Clerk Examples and Demos

This is a preview of Clerk before its open source release. See the
[Clerk README](https://nextjournal.com/mk/clerk-preview) for more information
and for starting your own project.

## Running these demo notebooks
To appreciate the full power of Clerk we *strongly* recommend you spend the time to [configure your editor](https://clojure.org/guides/editors) to evaluate forms inline, but Clerk does not require it. Instructions for each below.

### Using a standalone Clojure REPL
After cloning this repo & setting this directory as the cwd:

```
clj
```

This will open a REPL that will look something like:
```
Clojure 1.11.1
user=>
```

Open [dev/user.clj](dev/user.clj) and copy/paste one of the `(clerk/serve!)` forms to start the server, then one of the `(clerk/show!)` forms to choose which notebook to display.

Go to http://localhost:7777/ ::tada::


### Using an Editor-connected nREPL
After cloning this repo & setting this directory as the cwd:

```
clj -M:nREPL -m nrepl.cmdline
```

Then simply connect your editor to the host & port indicated after the "nREPL server started on port" log trace.

Open [dev/user.clj](dev/user.clj) and evaluate the commented forms depending on which paths you would like to serve and which demo notebooks you would like to see.

Go to http://localhost:7777/ ::tada::
