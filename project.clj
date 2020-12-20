(def jme-version "3.2.2-stable")

(defproject jme-clj "0.1.0-SNAPSHOT"

  :description "A Clojure 3D Game Engine Wrapper, Powered By jMonkeyEngine"

  :url "https://github.com/ertugrulcetin/jme-clj"

  :author "Ertuğrul Çetin"

  :license {:name "MIT License"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.jmonkeyengine/jme3-core ~jme-version]
                 [org.jmonkeyengine/jme3-plugins ~jme-version]
                 [org.jmonkeyengine/jme3-terrain ~jme-version]
                 [org.jmonkeyengine/jme3-bullet ~jme-version]
                 [org.jmonkeyengine/jme3-bullet-native ~jme-version]
                 [org.jmonkeyengine/jme3-jogg ~jme-version]
                 [org.jmonkeyengine/jme3-jogl ~jme-version]
                 [org.jmonkeyengine/jme3-effects ~jme-version]
                 [org.jmonkeyengine/jme3-desktop ~jme-version]
                 [org.jmonkeyengine/jme3-lwjgl ~jme-version]
                 [org.jmonkeyengine/jme3-niftygui ~jme-version]
                 [org.jmonkeyengine/jme3-networking ~jme-version]
                 [org.jmonkeyengine/jme3-blender ~jme-version]
                 [camel-snake-kebab "0.4.2"]
                 [kezban "0.1.92"]
                 [potemkin "0.4.5"]]

  :plugins [[ertu/lein-bikeshed "0.1.11"]
            [ertu/lein-carve "0.1.0"]
            [jonase/eastwood "0.3.11"]
            [lein-ancient "0.6.15"]
            [lein-nsort "0.1.14"]
            [pisano/lein-kibit "0.1.2"]]

  :min-lein-version "2.0.0"

  :repositories [["jcenter" "https://jcenter.bintray.com/"]]

  :resource-paths ["resources"]

  :aot [jme-clj.core]

  :eastwood {:source-paths       ["src"]
             :exclude-namespaces [:test-paths]
             :add-linters        [:unused-private-vars]
             :exclude-linters    [:deprecations
                                  :implicit-dependencies
                                  :unused-ret-vals
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
                         ["clj-kondo" "--lint" "src" "test"]
                         ["kibit"]
                         ["eastwood"]]
            "clj-kondo" ["with-profile" "+dev" "run" "-m" "clj-kondo.main"]}

  :jvm-opts ["-XX:-OmitStackTraceInFastThrow"
             "-XX:+ScavengeBeforeFullGC"
             "-XX:+IgnoreUnrecognizedVMOptions"
             "-Djava.net.preferIPv4Stack=true"
             "-Dfile.encoding=UTF-8"]

  :profiles {:dev  {:dependencies [[clj-kondo "2020.10.10"]]
                    :repl-options {:init-ns jme-clj.core}}
             :test {:dependencies   [[clj-kondo "2020.10.10"]]
                    :resource-paths ["test/resources"]}})
