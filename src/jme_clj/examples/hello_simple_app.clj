(ns jme-clj.examples.hello-simple-app
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_simpleapplication.html"
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.math ColorRGBA)))


(defn init []
  (let [box           (box 1 1 1)
        geom          (geo "Box" box)
        mat           (material "Common/MatDefs/Misc/Unshaded.j3md")]
    (set* mat :color "Color" ColorRGBA/Blue)
    (set* geom :material mat)
    (add-to-root geom)))


(defsimpleapp app :init init)


(comment
 (start app)
 (stop app)

 (re-init app init)

 (unbind-app #'app))
