(ns jme-clj.examples.hello-terrain
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_terrain.html"
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.texture Texture$WrapMode)))


(defn init []
  (set* (fly-cam) :move-speed 50)
  (let [asset-manager  (get-manager :asset)
        grass          (set* (load-texture "Textures/Terrain/splat/grass.jpg") :wrap Texture$WrapMode/Repeat)
        dirt           (set* (load-texture "Textures/Terrain/splat/dirt.jpg") :wrap Texture$WrapMode/Repeat)
        rock           (set* (load-texture "Textures/Terrain/splat/road.jpg") :wrap Texture$WrapMode/Repeat)
        mat            (material asset-manager "Common/MatDefs/Terrain/Terrain.j3md")
        height-map-tex (load-texture "Textures/Terrain/splat/mountains512.png")
        height-map     (->> height-map-tex image image-based-hm load-hm)
        patch-size     65
        terrain        (terrain-quad "my terrain" patch-size 513 (get-hm height-map))]
    (-> mat
        (set* :texture "Alpha" (load-texture "Textures/Terrain/splat/alphamap.png"))
        (set* :texture "Tex1" grass)
        (set* :float "Tex1Scale" (float 64))
        (set* :texture "Tex2" dirt)
        (set* :float "Tex2Scale" (float 32))
        (set* :texture "Tex3" rock)
        (set* :float "Tex3Scale" (float 128)))
    (-> terrain
        (set* :material mat)
        (set* :local-translation 0 -100 0)
        (set* :local-scale 2 1 2)
        (add-to-root)
        (add-control (terrain-lod-control terrain (cam))))))


(defsimpleapp app :init init)


(comment
 (start app)
 (stop app)

 (re-init app init)

 (unbind-app #'app))
