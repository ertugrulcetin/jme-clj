;; Please start your REPL with `+test` profile
(ns examples.beginner-tutorials.hello-animation
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_animation.html"
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.animation AnimControl LoopMode)
   (com.jme3.input KeyInput)
   (com.jme3.math ColorRGBA)))


;; for keeping internal *bindings* work, also the app. We need to define
;; listeners with `defn`. `def` should NOT be used!
(defn on-action-listener []
  (action-listener
   (fn [name pressed? tpf]
     (when (and (= name ::walk) (not pressed?))
       (let [{:keys [channel]} (get-state)]
         (when (-> channel (get* :animation-name) (not= "Walk"))
           (-> channel
               (set* :anim "Walk" 0.5)
               (set* :loop-mode LoopMode/Loop))))))))


(defn on-anim-listener []
  (anim-listener
   (fn [control channel name]
     (when (= "Walk" name)
       (-> channel
           (set* :anim "stand" 0.5)
           (set* :loop-mode LoopMode/DontLoop)
           (set* :speed 1.0))))
   (fn [control channel name])))


(defn- init-keys []
  (apply-input-mapping
   ;; Using qualified keywords for inputs is highly recommended!
   {:triggers  {::walk (key-trigger KeyInput/KEY_SPACE)}
    :listeners {(on-action-listener) ::walk}}))


(defn init []
  (let [player  (load-model "Models/Oto/OtoOldAnim.j3o")
        control (get* player :control AnimControl)
        channel (create-channel control)]
    (set* (view-port) :background-color ColorRGBA/LightGray)
    (-> (light :directional)
        (set* :direction (vec3 -0.1 -1 -1 :normalize))
        (add-light-to-root))
    (-> player
        ;; interop needs float casting
        (set* :local-scale (float 0.5))
        (add-to-root))
    (add-anim-listener control (on-anim-listener))
    (set* channel :anim "stand")
    (init-keys)
    {:channel channel}))


(defsimpleapp app :init init)


(comment
 (start app)

 ;;after calling unbind-app, we need to re-define the app with defsimpleapp
 (unbind-app #'app)

 (run app
      (re-init init))
 )
