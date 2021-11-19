(def jme-version "3.3.2-stable")

(defproject jme-clj "0.1.13"

  :codox {:metadata    {:doc        "**TODO**: write docs"
                        :doc/format :markdown}
          :output-path "docs"
          ;;:source-uri  "https://github.com/simon-brooke/jme-clj/blob/main/{filepath}#L{line}"
          :source-uri   "file:///home/simon/workspace/jme-clj/{filepath}#L{line}"
          }
  
  :description "A Clojure 3D Game Engine Wrapper, Powered By jMonkeyEngine"

  :url "https://github.com/ertugrulcetin/jme-clj"

  :author "Ertuğrul Çetin"

  :license {:name "MIT License"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.jmonkeyengine/jme3-core ~jme-version]
                 [org.jmonkeyengine/jme3-plugins ~jme-version]
                 [org.jmonkeyengine/jme3-terrain ~jme-version]
                 [org.jmonkeyengine/jme3-jogg ~jme-version]
                 [org.jmonkeyengine/jme3-jogl ~jme-version]
                 [org.jmonkeyengine/jme3-effects ~jme-version]
                 [org.jmonkeyengine/jme3-desktop ~jme-version]
                 [org.jmonkeyengine/jme3-lwjgl ~jme-version]
                 [org.jmonkeyengine/jme3-niftygui ~jme-version]
                 [org.jmonkeyengine/jme3-networking ~jme-version]
                 [org.jmonkeyengine/jme3-blender ~jme-version]
                 [camel-snake-kebab "0.4.2"]
                 [com.github.stephengold/Minie "3.1.0"]
                 [kezban "0.1.92"]
                 [potemkin "0.4.5"]]

  :plugins [[ertu/lein-bikeshed "0.1.13"]
            [ertu/lein-carve "0.1.0"]
            [jonase/eastwood "0.3.11"]
            [lein-ancient "0.6.15"]
            [lein-codox "0.10.8"]
            [lein-nsort "0.1.14"]
            [pisano/lein-kibit "0.1.2"]]

  :min-lein-version "2.0.0"

  :repositories [["jcenter" "https://jcenter.bintray.com/"]]

  :resource-paths ["resources"]

  :java-source-paths ["java"]

  :aot [jme-clj.core]

  :eastwood {:source-paths       ["src"]
             :exclude-namespaces [:test-paths]
             :add-linters        [:unused-private-vars]
             :exclude-linters    [:deprecations
                                  :implicit-dependencies
                                  :unused-ret-vals
                                  :unused-meta-on-macro
                                  :local-shadows-var
                                  :constant-test]}

  :nsort {:require      :alias-bottom-asc
          :source-paths ["src" "test"]}

  :bikeshed {:max-line-length 120
             :source-paths    ["src" "test"]}

  :carve {:paths   ["src" "test"]
          :dry-run true
          :report  {:format :text}}

  :aliases {"lint"      ["do"
                         ["nsort"]
                         ["bikeshed"]
                         ["carve"]
                         ["clj-kondo" "--lint" "src"]
                         ["kibit"]
                         ["eastwood"]]
            "clj-kondo" ["with-profile" "+dev" "run" "-m" "clj-kondo.main"]}

  :jvm-opts ^:replace ["-XX:+UseZGC"
                       "-XX:-OmitStackTraceInFastThrow"
                       "-XX:+ScavengeBeforeFullGC"
                       "-XX:+IgnoreUnrecognizedVMOptions"
                       "-Djava.net.preferIPv4Stack=true"
                       "-Dfile.encoding=UTF-8"]

  :profiles {:dev  {:dependencies   [[clj-kondo "2021.01.20"]
                                     [org.clojure/tools.logging "1.1.0"]]
                    :repl-options   {:init-ns jme-clj.core}
                    :resource-paths ["test/resources"]}
             :test {:dependencies   [[clj-kondo "2021.01.20"]
                                     [org.clojure/tools.logging "1.1.0"]]
                    :resource-paths ["test/resources"]}})
