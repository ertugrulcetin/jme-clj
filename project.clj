(def jme-version "3.2.2-stable")

(defproject jme-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.jmonkeyengine/jme3-core ~jme-version]
                 [org.jmonkeyengine/jme3-plugins ~jme-version]
                 [org.jmonkeyengine/jme3-terrain ~jme-version]
                 [org.jmonkeyengine/jme3-jbullet ~jme-version]
                 [org.jmonkeyengine/jme3-jogg ~jme-version]
                 [org.jmonkeyengine/jme3-jogl ~jme-version]
                 [org.jmonkeyengine/jme3-effects ~jme-version]
                 [org.jmonkeyengine/jme3-desktop ~jme-version]
                 [org.jmonkeyengine/jme3-lwjgl ~jme-version]
                 [org.jmonkeyengine/jme3-niftygui ~jme-version]
                 [org.jmonkeyengine/jme3-networking ~jme-version]
                 [org.jmonkeyengine/jme3-blender ~jme-version]
                 [camel-snake-kebab "0.4.2"]
                 [kezban "0.1.92"]]
  :repl-options {:init-ns jme-clj.core}
  :repositories [["jcenter" "https://jcenter.bintray.com/"]]
  :resource-paths ["resources"]
  :aot [jme-clj.core])
