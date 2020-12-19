(ns jme-clj.examples.hello-input-system
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_input_system.html"
  (:require
   [jme-clj.core :refer :all])
  (:import (com.jme3.input KeyInput MouseInput)
           (com.jme3.math ColorRGBA)))


;; for keeping internal *bindings* work, also the app. We need to define
;; listeners with `defn`. `def` should NOT be used!
(defn on-action-listener []
  (action-listener
   (fn [name pressed? tpf]
     (when (and (= name "Pause") (not pressed?))
       ;;TODO fix here
       (update-state update :running? not)))))


(defn on-analog-listener []
  (analog-listener
   (fn [name value tpf]
     (let [speed 1.0
           {:keys [player running?]} (get-state)
           v     (get* player :local-translation)]
       (when running?
         (case name
           :rotate (rotate player 0 (* value speed) 0)
           :right (set* player :local-translation (+ (.-x v) (* value speed)) (.-y v) (.-z v))
           :left (set* player :local-translation (- (.-x v) (* value speed)) (.-y v) (.-z v))
           (println "Press P to unpause.")))))))


(defn- init-keys []
  (apply-input-mapping
   {:triggers  {:pause  (key-trigger KeyInput/KEY_P)
                :left   (key-trigger KeyInput/KEY_J)
                :right  (key-trigger KeyInput/KEY_K)
                :rotate [(key-trigger KeyInput/KEY_SPACE)
                         (mouse-trigger MouseInput/BUTTON_LEFT)]}
    :listeners {(on-action-listener) :pause
                (on-analog-listener) [:left :right :rotate]}}))


(defn init []
  (let [box    (box 1 1 1)
        player (geo "Box" box)
        mat    (material "Common/MatDefs/Misc/Unshaded.j3md")]
    (set* mat :color "Color" ColorRGBA/Red)
    (set* player :material mat)
    (add-to-root player)
    (init-keys)
    {:player player :running? true}))


(defsimpleapp app :init init)


(comment
 (start app)
 (stop app)

 (re-init app init)

 (unbind-app #'app))
