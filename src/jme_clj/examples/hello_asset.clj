(ns jme-clj.examples.hello-asset
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_asset.html"
  (:require [jme-clj.core :refer :all]))


(defn init []
  (let [root-node   (root-node)
        mat-default (material "Common/MatDefs/Misc/ShowNormals.j3md")
        teapot      (load-model "Models/Teapot/Teapot.obj")
        teapot      (set* teapot :material mat-default)
        root-node   (attach-child root-node teapot)
        ;; Create a wall with a simple texture from test_data
        box         (box 2.5 2.5 1.0)
        mat-brick   (material "Common/MatDefs/Misc/Unshaded.j3md")
        texture     (load-texture "Textures/Terrain/BrickWall/BrickWall.jpg")
        mat-brick   (set* mat-brick :texture "ColorMap" texture)
        wall        (geo "Box" box)
        wall        (-> wall (set* :material mat-brick) (set* :local-translation 2.0 -2.5 0.0))
        root-node   (attach-child root-node wall)
        ;; Display a line of text with a default font
        gui-node    (detach-all-child (gui-node))
        gui-font    (load-font "Interface/Fonts/Default.fnt")
        size        (-> gui-font (get* :char-set) (get* :rendered-size))
        hello-text  (bitmap-text gui-font false)
        hello-text  (-> hello-text
                        (set* :size size)
                        (set* :text "Hello World")
                        (set* :local-translation 300 (get* hello-text :line-height) 0))]
    (attach-child gui-node hello-text)
    ; Load a model from test_data (OgreXML + material + texture)
    (-> (load-model "Models/Ninja/Ninja.mesh.xml")
        (scale 0.05 0.05 0.05)
        (rotate 0.0 -3.0 0.0)
        (set* :local-translation 0.0 -5.0 -2.0)
        (add-to-root))
    ;; You must add a light to make the model visible
    (-> (light :directional)
        (set* :direction (vec3 -0.1 -0.7 -1.0))
        (add-light-to-root))))


(defsimpleapp app :init init)


(comment
 (start app)
 (stop app)

 (re-init app init)

 (unbind-app #'app))
