;; Please start your REPL with `+test` profile
(ns examples.simple-multiplayer.client
  (:require
   [jme-clj.core :refer :all]
   [jme-clj.network :refer :all])
  (:import
   (com.jme3.input MouseInput)
   (com.jme3.math Vector3f ColorRGBA)
   (com.jme3.scene.shape Sphere$TextureMode)
   (com.jme3.texture Texture$WrapMode)))


#_(defn- set-up-keys []
    (apply-input-mapping
     ;; Using qualified keywords for inputs is highly recommended!
     {:triggers  {::shoot (mouse-trigger MouseInput/BUTTON_LEFT)}
      :listeners {(on-action-listener) ::shoot}}))


(defn- init-materials []
  (letj [floor-mat (unshaded-mat)]
        (set* floor-mat :texture "ColorMap" (-> "Textures/Terrain/Pond/Pond.jpg"
                                                (load-texture)
                                                (set* :wrap Texture$WrapMode/Repeat)))))


(defn- init-floor [bullet-as floor floor-mat]
  (let [floor-geo (-> (geo "Floor" floor)
                      (set* :material floor-mat)
                      (set* :local-translation 0 -0.1 0)
                      (add-to-root))
        floor-phy (rigid-body-control 0.0)]
    (add-control floor-geo floor-phy)
    (-> bullet-as
        (get* :physics-space)
        (call* :add floor-phy))))


(defn- create-player-data []
  {:location       (vec3)
   :rotation       (quat)
   :walk-direction (vec3)
   :id             0
   :name           nil})


(defn- add-lights []
  (let [sun     (-> (light :directional)
                    (setc :direction (vec3 -0.5 -0.5 -0.5)
                          :color ColorRGBA/White))
        ambient (-> (light :ambient)
                    (set* :color ColorRGBA/White))]
    (add-light-to-root sun)
    (add-light-to-root ambient)))


(defn init []
  (let [bullet-as (bullet-app-state)
        floor     (box 50 0.1 50)]
    (setc (fly-cam)
          :move-speed 30
          :drag-to-rotate true)
    (set* (cam) :location (vec3 -1.5, 52.5, 97.5))
    (look-at (vec3 0.01 -0.6 -0.79) Vector3f/UNIT_Y)
    (attach bullet-as)
    (scale-texture-coords floor (vec2 3 6))
    (init-floor bullet-as floor (:floor-mat (init-materials)))
    (add-lights)
    {:player-data (create-player-data)
     :bullet-as   bullet-as}))


(defsimpleapp app
              :opts {:show-settings?       false
                     :pause-on-lost-focus? false
                     :settings             {:title          "My JME Game"
                                            :load-defaults? true
                                            :frame-rate     60}}
              :init init)


(comment
 (start app)

 (run app
      (re-init init))

 ;;after calling unbind-app, we need to re-define the app with defsimpleapp
 (unbind-app #'app)
 )
