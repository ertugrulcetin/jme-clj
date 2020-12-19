(ns jme-clj.examples.hello-physics
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_physics.html"
  (:require [jme-clj.core :refer :all])
  (:import
   (com.jme3.input MouseInput)
   (com.jme3.math Vector3f)
   (com.jme3.scene.shape Sphere$TextureMode)
   (com.jme3.texture Texture$WrapMode)))


(def brick-length 0.48)
(def brick-width 0.24)
(def brick-height 0.12)


;; for keeping internal *bindings* work, also the app. We need to define
;; listeners with `defn`. `def` should NOT be used!
(defn- on-action-listener []
  (action-listener
   (fn [name pressed? tpf]
     (when (and (= name :shoot) (not pressed?))
       ;;todo make balls
       ))))


(defn- set-up-keys []
  (apply-input-mapping
   {:triggers  {:shoot (mouse-trigger MouseInput/BUTTON_LEFT)}
    :listeners {(on-action-listener) :shoot}}))


(defn- init-cross-hairs []
  (let [gui-font (load-font "Interface/Fonts/Default.fnt")
        settings (get* (context) :settings)
        ch       (bitmap-text gui-font false)]
    (-> ch
        (set* :size (-> gui-font
                        (get* :char-set)
                        (get* :rendered-size)
                        (* 2)))
        (set* :text "+")
        (set* :local-translation
              (- (/ (get* settings :width) 2)
                 (/ (get* ch :line-width) 2))
              (+ (/ (get* settings :height) 2)
                 (/ (get* ch :line-height) 2))
              0)
        (#(attach-child (gui-node) %)))))


(defn- init-materials []
  (letj [wall-mat (material "Common/MatDefs/Misc/Unshaded.j3md")
         stone-mat (material "Common/MatDefs/Misc/Unshaded.j3md")
         floor-mat (material "Common/MatDefs/Misc/Unshaded.j3md")]
        (set* wall-mat :texture "ColorMap" (load-texture "Textures/Terrain/BrickWall/BrickWall.jpg"))
        (set* stone-mat :texture "ColorMap" (load-texture "Textures/Terrain/Rock/Rock.PNG"))
        (set* floor-mat :texture "ColorMap" (-> (load-texture "Textures/Terrain/Pond/Pond.jpg")
                                                (set* :wrap Texture$WrapMode/Repeat)))))


(defn- make-brick [loc box* wall-mat bullet-as]
  (let [brick-geo (-> (geo "brick" box*)
                      (set* :material wall-mat)
                      (add-to-root))
        brick-geo (set* brick-geo :local-translation loc)
        brick-phy (rigid-body-control 0)]
    (add-control brick-geo brick-phy)
    (-> bullet-as
        (get* :physics-space)
        (call* :add brick-phy))))


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


(defn- init-wall [box* wall-mat bullet-as]
  (let [startpt (atom (float (/ brick-length 4)))
        height  (atom 0)]
    (doseq [_ (range 15)]
      (doseq [i (range 6)]
        (make-brick (vec3 (+ @startpt (* i brick-length 2))
                          (+ brick-height @height)
                          0)
                    box*
                    wall-mat
                    bullet-as))
      (swap! startpt -)
      (swap! height + (* 2 brick-height)))))


(defn init []
  (let [bullet-as (bullet-app-state)
        ;bullet-as     (set* bullet-as :debug-enabled true)
        sphere*   (sphere 32 32 0.4 true false)
        box*      (box brick-length brick-height brick-width)
        floor     (box 10 0.1 5)]
    (set* sphere* :texture-mode Sphere$TextureMode/Projected)
    (scale-texture-coords box* (vec2 1 0.5))
    (scale-texture-coords floor (vec2 3 6))
    (attach bullet-as)
    (setc (cam) :location (vec3 0 4 6))
    (look-at (vec3 2 2 0) Vector3f/UNIT_Y)
    (let [{:keys [wall-mat stone-mat floor-mat]} (init-materials)
          _ (init-wall box* wall-mat bullet-as)
          _ (init-floor bullet-as floor floor-mat)]
      (init-cross-hairs)
      (merge {:sphere sphere*
              :box    box*
              :floor  floor
              }))))


(defsimpleapp app :init init)


(comment
 (start app)
 (stop app)

 (re-init app init)

 (run app
      #_(let [{:keys [bullet-as floor-phy]} (get-state)
              floor-phy (set* floor-phy :mass 2.0)]
          (-> bullet-as
              (get* :physics-space)
              (call* :add brick-phy)))
      (doseq [b (:bricks @temp)]
        (set* b :mass 2.0))
      )

 (unbind-app #'app))
