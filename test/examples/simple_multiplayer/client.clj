;; Please start your REPL with `+test` profile
(ns examples.simple-multiplayer.client
  (:require
   [jme-clj.core :refer :all]
   [jme-clj.network :refer :all])
  (:import
   (com.jme3.input KeyInput)
   (com.jme3.math Vector3f ColorRGBA)
   (com.jme3.texture Texture$WrapMode)))


(defn- init-as [bullet-as]
  (let [player         (load-model "Models/Oto/OtoOldAnim.j3o")
        player-control (character-control (capsule-collision-shape 3.0 4.0) 0.01)]
    (setc player-control
          :gravity (vec3 0 -30 0)
          :physics-location (vec3 0 100 0))
    (add-control player player-control)
    (-> bullet-as
        (get* :physics-space)
        (call* :add player-control))
    (add-to-root player)
    {:player         player
     :player-control player-control
     :walk-direction (vec3)
     :left           false
     :right          false
     :up             false
     :down           false}))


(defn update-as [tpf]
  (let [cam-dir        (get* (cam) :direction)
        cam-left       (get* (cam) :left)
        states         (-> :app-state get-state ::player)
        walk-direction (setv (:walk-direction states) 0 0 0)
        direction      (cond
                         (:up states) cam-dir
                         (:down states) (negate cam-dir)
                         (:left states) cam-left
                         (:right states) (negate cam-left))
        walk-direction (or (some->> direction (add-loc walk-direction))
                           walk-direction)
        player-control (:player-control states)]
    (.setY walk-direction 0)
    (set* player-control :walk-direction (mult-loc walk-direction 1))
    (set-state [:player-data :location] (get* player-control :physics-location))
    (set-state [:player-data :rotation] (get* (:player states) :local-rotation))
    (set-state [:player-data :walk-direction] walk-direction)
    nil))


(defn- create-player-as [bullet-as]
  (app-state ::player
             :init #(init-as bullet-as)
             :update update-as))


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


;; for keeping internal *bindings* work, also the app. We need to define
;; listeners with `defn`. `def` should NOT be used!
(defn on-action-listener []
  (action-listener
   (fn [name* pressed? tpf]
     (set-state :app-state [::player (-> name* name keyword)] pressed?))))


(defn- init-keys []
  (apply-input-mapping
   ;; Using qualified keywords for inputs is highly recommended!
   {:triggers  {::up    (key-trigger KeyInput/KEY_UP)
                ::down  (key-trigger KeyInput/KEY_DOWN)
                ::left  (key-trigger KeyInput/KEY_LEFT)
                ::right (key-trigger KeyInput/KEY_RIGHT)}
    :listeners {(on-action-listener) [::left ::right ::up ::down]}}))


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
    (attach (create-player-as bullet-as))
    (init-keys)
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
