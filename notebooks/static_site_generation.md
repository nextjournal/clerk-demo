# Publishing to Github Pages

Clerks function `nextjournal.clerk/build-static-app!` takes a map with `:paths` and builds a static html page of your notebooks at such paths.  

To publish a collection of clerk notebooks to github pages (like this guide) we suggest adding a `:nextjournal/clerk` alias to your repo's `deps.edn` with e.g. the following properties:

    {:extra-deps {io.github.nextjournal/clerk {:git/sha "13ba371a894aa8696697d0a47cf715296c35e4b5"}}
     :extra-paths ["datasets"]
     :exec-fn nextjournal.clerk/build-static-app!
     :exec-args {:paths [.. paths to my notebooks ... ]}}

and a e.g. `.github/workflows/main.yml` to your repo with the following steps:


        name: Deploy to GitHub Pages
        
        on:
          push:
            #branches: [master] #uncomment this to only respond to a push to master
        
          # Allows you to run this workflow manually from the Actions tab
          workflow_dispatch:
        
        # Sets permissions of the GITHUB_TOKEN to allow deployment to GitHub Pages
        permissions:
          contents: read
          pages: write
          id-token: write
        
        # Allow one concurrent deployment
        concurrency:
          group: "pages"
          cancel-in-progress: true
        
        jobs:
          # Build job
          build:
            runs-on: ubuntu-latest
            steps:
              - name: Checkout
                uses: actions/checkout@v3
              - name: Setup Pages
                uses: actions/configure-pages@v1
        
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
                run: clojure -X:nextjournal/clerk :path-prefix '"jointprob-clerk/"' :git/sha '"${{ github.sha }}"'
        
              - name: Upload artifact
                uses: actions/upload-pages-artifact@v1
                with:
                  # Upload entire repository
                  path: 'public/build'
        
          # Deployment job
          deploy:
            environment:
              name: github-pages
              url: ${{ steps.deployment.outputs.page_url }}
            runs-on: ubuntu-latest
            needs: build
            steps:
              - name: Deploy to GitHub Pages
                id: deployment
                uses: actions/deploy-pages@v1

    
You might want to add this badge in your repo's readme :-)

    ![badge][https://img.shields.io/static/v1?label=Run%20with&message=Clerk&color=rgb(50,175,209)&logo=plex&logoColor=rgb(50,175,209)]

![badge][badgeURL]

[badgeURL]: https://img.shields.io/static/v1?label=Run%20with&message=Clerk&color=rgb(50,175,209)&logo=plex&logoColor=rgb(50,175,209)
