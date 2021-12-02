# Publishing to Github Pages

Clerks function `nextjournal.clerk/build-static-app!` takes a map with `:paths` and builds a static html page of your notebooks at such paths.  

To publish a collection of clerk notebooks to github pages (like this guide) we suggest adding a `:nextjournal/clerk` alias to your repo's `deps.edn` with e.g. the following properties:

    {:extra-deps {io.github.nextjournal/clerk {:git/sha "13ba371a894aa8696697d0a47cf715296c35e4b5"}}
     :extra-paths ["datasets"]
     :exec-fn nextjournal.clerk/build-static-app!
     :exec-args {:paths [.. paths to my notebooks ... ]}}

and a e.g. `.github/workflows/main.yml` to your repo with the following steps:


    name Publish to Pages
    on: push
    jobs:
      static-build:
      runs-on: ubuntu-latest
      steps:
        - name: 🛎️ Checkout
          uses: actions/checkout@v2

        - name: 🔧 Install java
          uses: actions/setup-java@v1
          with:
            java-version: '11.0.7'
    
        - name: 🔧 Install clojure
          uses: DeLaGuardo/setup-clojure@master
          with:
            cli: '1.10.3.943'
    
        - name: 🗝 maven cache
          uses: actions/cache@v2
          with:
            path: |
              ~/.m2
              ~/.gitlibs
            key: ${{ runner.os }}-maven-${{ github.sha }}
            restore-keys: |
              ${{ runner.os }}-maven-
    
        - name: 🗝 Clerk Cache
            uses: actions/cache@v2
            with:
              path: .cache
              key: ${{ runner.os }}-clerk
    
        - name: 🏗 Clerk Build
          run: clojure -X:nextjournal/clerk
    
        - name: 🚀 Deploy
          uses: JamesIves/github-pages-deploy-action@4.1.6
          with:
            branch: gh-pages
            folder: public/build
    
    
You might want to add this badge in your repo's readme :-)

    ![badge][https://img.shields.io/static/v1?label=Run%20with&message=Clerk&color=rgb(50,175,209)&logo=plex&logoColor=rgb(50,175,209)]

![badge][badgeURL]

[badgeURL]: https://img.shields.io/static/v1?label=Run%20with&message=Clerk&color=rgb(50,175,209)&logo=plex&logoColor=rgb(50,175,209)
