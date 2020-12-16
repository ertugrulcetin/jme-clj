(ns jme-clj.examples.hello-input-system
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_input_system.html"
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.app SimpleApplication)
   (com.jme3.math ColorRGBA)
   (com.jme3.input KeyInput MouseInput)))


(def action-listener
  (create-action-listener
   (fn [name pressed? tpf]
     (when (and (= name "Pause") (not pressed?))
       (update-state update :running? not)))))


(def analog-listener
  (create-analog-listener
   (fn [name value tpf]
     (let [speed 1.0
           {:keys [player running?]} (get-state)
           v     (get* player :local-translation)]
       (when running?
         (case name
           "Rotate" (rotate player 0 (* value speed) 0)
           "Right" (set* player :local-translation (+ (.-x v) (* value speed)) (.-y v) (.-z v))
           "Left" (set* player :local-translation (- (.-x v) (* value speed)) (.-y v) (.-z v))
           (println "Press P to unpause.")))))))


(defn- init-keys []
  (apply-input-mapping
   {:triggers  {"Pause"  (key-trigger KeyInput/KEY_P)
                "Left"   (key-trigger KeyInput/KEY_J)
                "Right"  (key-trigger KeyInput/KEY_K)
                "Rotate" [(key-trigger KeyInput/KEY_SPACE)
                          (mouse-trigger MouseInput/BUTTON_LEFT)]}
    :listeners {action-listener "Pause"
                analog-listener ["Left" "Right" "Rotate"]}}))


(defn init [^SimpleApplication app]
  (let [box           (box 1 1 1)
        player        (geo "Box" box)
        asset-manager (get-manager app :asset)
        mat           (material asset-manager "Common/MatDefs/Misc/Unshaded.j3md")
        root-node     (root-node app)]
    (set* mat :color "Color" ColorRGBA/Blue)
    (set* player :material mat)
    (attach-child root-node player)
    (init-keys)
    {:player player :running? true}))


(defsimpleapp app :init init)


(comment
 (start-app app)
 (stop-app app)

 (re-init app init)

 (unbind-app #'app)
 )
