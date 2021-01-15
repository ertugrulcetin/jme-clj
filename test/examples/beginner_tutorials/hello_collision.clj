;; Please start your REPL with `+test` profile
(ns examples.beginner-tutorials.hello-collision
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_collision.html"
  (:require [jme-clj.core :refer :all])
  (:import
   (com.jme3.asset.plugins ZipLocator)
   (com.jme3.input KeyInput)
   (com.jme3.math ColorRGBA)))


;; for keeping internal *bindings* work, also the app. We need to define
;; listeners with `defn`. `def` should NOT be used!
(defn on-action-listener []
  (action-listener
   (fn [name* pressed? tpf]
     (let [{:keys [player]} (get-state)]
       (if (= ::jump name*)
         (when pressed?
           (call* player :jump))
         (set-state (-> name* name keyword) pressed?))))))


(defn- set-up-keys []
  (apply-input-mapping
   ;; Using qualified keywords for inputs is highly recommended!
   {:triggers  {::left  (key-trigger KeyInput/KEY_A)
                ::right (key-trigger KeyInput/KEY_D)
                ::up    (key-trigger KeyInput/KEY_W)
                ::down  (key-trigger KeyInput/KEY_S)
                ::jump  (key-trigger KeyInput/KEY_SPACE)}
    :listeners {(on-action-listener) [::left ::right ::up ::down ::jump]}}))


(defn- set-up-light []
  (-> (light :ambient)
      (set* :color (.mult (ColorRGBA/White) 1.3))
      (add-light-to-root))
  (-> (light :directional)
      (set* :color ColorRGBA/White)
      (set* :direction (vec3 2.8 -2.8 -28 :normalize))
      (add-light-to-root)))


(defn init []
  (let [bullet-as     (bullet-app-state)
        ;bullet-as     (set* bullet-as :debug-enabled true)
        view-port     (set* (view-port) :background-color (color-rgba 0.7 0.8 1 1))
        fly-cam       (set* (fly-cam) :move-speed 100)
        _             (set-up-keys)
        _             (set-up-light)
        _             (register-locator "town.zip" ZipLocator)
        scene-model   (set* (load-model "main.scene") :local-scale (float 2))
        scene-shape   (create-mesh-shape scene-model)
        landscape     (rigid-body-control scene-shape 0)
        scene-model   (add-control scene-model landscape)
        capsule-shape (capsule-collision-shape 1.5 6 1)
        player        (setc (character-control capsule-shape 0.05)
                            :jump-speed 20
                            :fall-speed 80
                            :gravity 30
                            :physics-location (vec3 0 10 0))]
    (attach bullet-as)
    (add-to-root scene-model)
    (-> bullet-as
        (get* :physics-space)
        (call* :add landscape))
    (-> bullet-as
        (get* :physics-space)
        (call* :add player))
    {:player         player
     :walk-direction (vec3)
     :left           false
     :right          false
     :up             false
     :down           false
     :cam-dir        (vec3)
     :cam-left       (vec3)}))


(defn simple-update [tpf]
  (let [{:keys [cam-dir
                cam-left
                walk-direction
                player
                left
                right
                up
                down]} (get-state)
        cam-dir        (-> cam-dir (setv (get* (cam) :direction)) (mult-loc 0.6))
        cam-left       (-> cam-left (setv (get* (cam) :left)) (mult-loc 0.4))
        walk-direction (setv walk-direction 0 0 0)
        direction      (cond
                         left cam-left
                         right (negate cam-left)
                         up cam-dir
                         down (negate cam-dir))
        walk-direction (or (some->> direction (add-loc walk-direction))
                           walk-direction)]
    ;;since we mutate objects internally, we don't need to return hash-map in here
    (set* player :walk-direction walk-direction)
    (set* (cam) :location (get* player :physics-location))))


(defsimpleapp app
              :init init
              :update simple-update)


(comment
 (start app)

 ;;after calling unbind-app, we need to re-define the app with defsimpleapp
 (unbind-app #'app)

 (run app
      (re-init init))
 )
