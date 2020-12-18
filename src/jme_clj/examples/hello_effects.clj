(ns jme-clj.examples.hello-effects
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_effects.html"
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.effect ParticleMesh$Type)
   (com.jme3.math ColorRGBA)))


(defn init []
  (let [fire       (particle-emitter "Emitter" ParticleMesh$Type/Triangle 30)
        mat-fire   (material "Common/MatDefs/Misc/Particle.j3md")
        debris     (particle-emitter "Debris" ParticleMesh$Type/Triangle 10)
        mat-debris (material "Common/MatDefs/Misc/Particle.j3md")]
    (set* mat-fire :texture "Texture" (load-texture "Effects/Explosion/flame.png"))
    (set* mat-debris :texture "Texture" (load-texture "Effects/Explosion/Debris.png"))
    (setc fire
          :material mat-fire
          :images-x 2
          :images-y 2
          :end-color (color-rgba 1 0 0 1)
          :start-color (color-rgba 1 1 0 0.5)
          :start-size 1.5
          :end-size 0.1
          :gravity [0 0 0]
          :low-life 1
          :high-life 3)
    (-> fire
        (get* :particle-influencer)
        (setc :velocity-variation 0.3
              :initial-velocity (vec3 0 2 0)))
    (add-to-root fire)
    (setc debris
          :material mat-debris
          :images-x 3
          :images-y 3
          :rotate-speed 4
          :select-random-image true
          :start-color ColorRGBA/White
          :gravity [0 6 0]
          :low-life 1
          :high-life 3)
    (-> debris
        (get* :particle-influencer)
        (setc :velocity-variation 0.6
              :initial-velocity (vec3 0 4 0)))
    (add-to-root debris)
    (emit-all-particles debris)))


(defsimpleapp app :init init)


(comment
 (start app)
 (stop app)

 (re-init app init)

 (unbind-app #'app))
