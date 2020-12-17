(ns jme-clj.examples.hello-node
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_node.html"
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.math ColorRGBA)))


(defn init []
  (let [asset-manager (get-manager :asset)
        box1          (box 1 1 1)
        blue          (geo "Box" box1)
        mat1          (material asset-manager "Common/MatDefs/Misc/Unshaded.j3md")
        mat1          (set* mat1 :color "Color" ColorRGBA/Blue)
        box2          (box 1 1 1)
        red           (geo "Box" box2)
        mat2          (material asset-manager "Common/MatDefs/Misc/Unshaded.j3md")
        mat2          (set* mat2 :color "Color" ColorRGBA/Red)
        pivot         (node "pivot")]
    (-> blue
        (set* :local-translation (vec3 1 -1 1))
        (set* :material mat1))
    (-> red
        (set* :local-translation (vec3 1 3 1))
        (set* :material mat2))
    (-> pivot
        (add-to-root)
        (attach-child blue)
        (attach-child red)
        (rotate 0.4 0.4 0))))


(defsimpleapp app :init init)


(comment
 (start-app app)
 (stop-app app)

 (re-init app init)

 (unbind-app #'app))
