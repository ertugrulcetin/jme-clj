;; Please start your REPL with `+test` profile
(ns examples.fps.client
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.texture Texture$WrapMode)
   (com.jme3.terrain.heightmap HillHeightMap)))


(defn init []
  (set* (fly-cam) :move-speed 100)
  (let [bas           (attach (bullet-app-state))
        grass         (set* (load-texture "Textures/Terrain/splat/grass.jpg") :wrap Texture$WrapMode/Repeat)
        dirt          (set* (load-texture "Textures/Terrain/splat/dirt.jpg") :wrap Texture$WrapMode/Repeat)
        rock          (set* (load-texture "Textures/Terrain/splat/road.jpg") :wrap Texture$WrapMode/Repeat)
        mat           (material "Common/MatDefs/Terrain/Terrain.j3md")
        _             (set! (HillHeightMap/NORMALIZE_RANGE) 100)
        height-map    (hill-hm 513 1000 50 100 (byte 3))
        _             (call* height-map :load)
        patch-size    65
        terrain       (terrain-quad "my terrain" patch-size 513 (get-hm height-map))
        terrain-shape (create-mesh-shape terrain)
        landscape     (rigid-body-control terrain-shape 0)]
    (-> mat
        (set* :texture "Alpha" (load-texture "Textures/Terrain/splat/alphamap.png"))
        (set* :texture "Tex1" grass)
        (set* :float "Tex1Scale" (float 64))
        (set* :texture "Tex2" dirt)
        (set* :float "Tex2Scale" (float 32))
        (set* :texture "Tex3" rock)
        (set* :float "Tex3Scale" (float 128)))
    (-> terrain
        (setc :material mat
              :local-translation [0 -100 0]
              :local-scale [2 1 2])
        (add-control (terrain-lod-control terrain (cam)))
        (add-control landscape)
        (add-to-root))
    (add-to-root (create-sky "Textures/Sky/Bright/BrightSky.dds" :cube))
    (-> bas
        (get* :physics-space)
        (call* :add landscape))))


(defsimpleapp app
              :opts {:show-settings?       false
                     :pause-on-lost-focus? false
                     :settings             {:title          "My JME Game"
                                            :load-defaults? true
                                            :frame-rate     60
                                            :width          1200
                                            :height         800}}
              :init init)


(comment
 (start app)

 ;;after calling unbind-app, we need to re-define the app with defsimpleapp
 (unbind-app #'app)

 (run app
      (re-iwnit init))
 )
