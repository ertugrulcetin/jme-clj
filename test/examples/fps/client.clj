;; Please start your REPL with `+test` profile
(ns examples.fps.client
  (:require
   [examples.fps.app-states :as app.states]
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.texture Texture$WrapMode)
   (com.jme3.terrain.heightmap HillHeightMap)
   (com.jme3.input KeyInput)
   (com.jme3.math ColorRGBA)))


(defn on-action-listener []
  (action-listener
   (fn [name* pressed? tpf]
     (let [{:keys [player]} (get-state)]
       (if (= ::jump name*)
         (when pressed?
           (call* player :jump (vec3 0 20 0)))
         (set-state (-> name* name keyword) pressed?))))))


(defn- set-up-keys []
  (apply-input-mapping
   {:triggers  {::left  (key-trigger KeyInput/KEY_A)
                ::right (key-trigger KeyInput/KEY_D)
                ::up    (key-trigger KeyInput/KEY_W)
                ::down  (key-trigger KeyInput/KEY_S)
                ::jump  (key-trigger KeyInput/KEY_SPACE)}
    :listeners {(on-action-listener) [::left ::right ::up ::down ::jump]}}))


(defn- create-player []
  (let [player (character-control (capsule-collision-shape 3 7 1) 0.05)]
    (setc player
          :jump-speed 20
          :fall-speed 30
          :gravity (vec3 0 -30 0)
          :physics-location (vec3 -500 0 0))))


(defn- create-material []
  (let [grass (set* (load-texture "Textures/Terrain/splat/grass.jpg") :wrap Texture$WrapMode/Repeat)
        dirt  (set* (load-texture "Textures/Terrain/splat/dirt.jpg") :wrap Texture$WrapMode/Repeat)
        rock  (set* (load-texture "Textures/Terrain/splat/road.jpg") :wrap Texture$WrapMode/Repeat)]
    (-> (material "Common/MatDefs/Terrain/Terrain.j3md")
        (set* :texture "Alpha" (load-texture "Textures/Terrain/splat/alphamap.png"))
        (set* :texture "Tex1" grass)
        (set* :float "Tex1Scale" (float 64))
        (set* :texture "Tex2" dirt)
        (set* :float "Tex2Scale" (float 32))
        (set* :texture "Tex3" rock)
        (set* :float "Tex3Scale" (float 128)))))


(defn- create-terrain [mat]
  (let [_          (set! (HillHeightMap/NORMALIZE_RANGE) 100)
        height-map (hill-hm 513 1000 50 100 (byte 3))
        _          (call* height-map :load)
        patch-size 65
        terrain    (terrain-quad "my terrain" patch-size 513 (get-hm height-map))]
    (-> terrain
        (setc :material mat
              :local-translation [0 -100 0]
              :local-scale [2 1 2])
        (add-control (terrain-lod-control terrain (cam))))))


(defn- add-lights []
  (let [sun     (-> (light :directional)
                    (setc :direction (vec3 -0.5 -0.5 -0.5)
                          :color ColorRGBA/White))
        ambient (-> (light :ambient)
                    (set* :color ColorRGBA/White))]
    (add-light-to-root sun)
    (add-light-to-root ambient)))


(defn init []
  (set* (fly-cam) :move-speed 100)
  (let [bas           (attach (bullet-app-state))
        mat           (create-material)
        terrain       (create-terrain mat)
        terrain-shape (create-mesh-shape terrain)
        landscape     (rigid-body-control terrain-shape 0)
        player        (create-player)
        spatial       (add-control (load-model "Models/Oto/Oto.mesh.xml") player)]
    (add-to-root (create-sky "Textures/Sky/Bright/BrightSky.dds" :cube))
    (add-lights)
    (attach (app.states/create-cross-hairs))
    (-> terrain
        (add-control landscape)
        (add-to-root))
    (-> bas
        (get* :physics-space)
        (call* :add landscape))
    (-> bas
        (get* :physics-space)
        (call* :add-all spatial))
    (set-up-keys)
    {:bullet-app-state bas
     :player           player
     :spatial          spatial
     :terrain          terrain
     :walk-direction   (vec3)
     :cam-dir          (vec3)
     :cam-left         (vec3)
     :up               false
     :down             false
     :left             false
     :right            false}))


(defn- get-available-loc [player terrain]
  (let [loc         (get* player :physics-location)
        ^Float size (- (get* terrain :terrain-size) 2)
        x           (Math/max ^Float (- size) (Math/min ^Float (.-x loc) size))
        z           (Math/max ^Float (- size) (Math/min ^Float (.-z loc) size))]
    (doto loc
      (.setX x)
      (.setZ z))))


(defn- simple-update [tpf]
  (let [{:keys [cam-dir
                cam-left
                walk-direction
                player
                left
                right
                up
                down] :as m} (get-state)
        cam-dir        (-> cam-dir (setv (get* (cam) :direction)) (mult-loc 0.6))
        cam-left       (-> cam-left (setv (get* (cam) :left)) (mult-loc 0.4))
        walk-direction (setv walk-direction 0 0 0)
        walk-direction (cond-> walk-direction
                               left (add-loc cam-left)
                               right (add-loc (negate cam-left))
                               up (add-loc cam-dir)
                               down (add-loc (negate cam-dir)))
        loc            (get-available-loc (:player m) (:terrain m))]
    (set* player :walk-direction walk-direction)
    (set* player :physics-location loc)
    (set* (cam) :location loc)))


(defsimpleapp app
              :opts {:show-settings?       false
                     :pause-on-lost-focus? false
                     :settings             {:title          "My JME Game"
                                            :load-defaults? true
                                            :frame-rate     60
                                            :width          1200
                                            :height         800}}
              :init init
              :update simple-update)


(comment
 (start app)

 ;;after calling unbind-app, we need to re-define the app with defsimpleapp
 (unbind-app #'app)

 (run app
      (re-init init))

 (run app
      )
 )
