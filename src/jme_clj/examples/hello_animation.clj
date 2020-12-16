(ns jme-clj.examples.hello-animation
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_animation.html"
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.app SimpleApplication)
   (com.jme3.math ColorRGBA)
   (com.jme3.animation AnimControl LoopMode)
   (com.jme3.input KeyInput)))


(def action-listener
  (create-action-listener
   (fn [name pressed? tpf]
     (when (and (= name "Walk") (not pressed?))
       (let [{:keys [channel]} (get-state)]
         (when (-> channel (get* :animation-name) (not= "Walk"))
           (-> channel
               (set* :anim "Walk" 0.5)
               (set* :loop-mode LoopMode/Loop))))))))


(def anim-listener
  (create-anim-listener
   (fn [control channel name]
     (when (= "Walk" name)
       (-> channel
           (set* :anim "stand" 0.5)
           (set* :loop-mode LoopMode/DontLoop)
           (set* :speed 1.0))))
   (fn [control channel name])))


(defn- init-keys []
  (apply-input-mapping
   {:triggers  {"Walk" (key-trigger KeyInput/KEY_SPACE)}
    :listeners {action-listener "Walk"}}))


(defn init [^SimpleApplication app]
  (let [player  (load-model "Models/Oto/OtoOldAnim.j3o")
        control (get* player :control AnimControl)
        channel (create-channel control)]
    (set* (.getViewPort app) :background-color ColorRGBA/LightGray)
    (-> (light :directional)
        (set* :direction (vec3 -0.1 -1 -1 :normalize))
        (add-light-to-root))
    (-> player
        ;; interop needs float casting
        (set* :local-scale (float 0.5))
        (add-to-root))
    (add-anim-listener control anim-listener)
    (set* channel :anim "stand")
    (init-keys)
    {:channel channel}))


(defsimpleapp app :init init)


(comment
 (start-app app)
 (stop-app app)

 (re-init app init)

 (unbind-app #'app)
 )
