(ns jme-clj.examples.hello-update-loop
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_main_event_loop.html"
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.app SimpleApplication)
   (com.jme3.math ColorRGBA)))


(defn init [^SimpleApplication app]
  (let [box           (box 1 1 1)
        player        (geo "blue cube" box)
        asset-manager (get-manager app :asset)
        mat           (material asset-manager "Common/MatDefs/Misc/Unshaded.j3md")]
    (set* mat :color "Color" ColorRGBA/Blue)
    (set* player :material mat)
    (attach-child (root-node app) player)
    ;; When we return hash map, it will be added to jme-clj.core/states with key :jme-clj.core/app
    ;; so we can access inside `update` fn
    {:player player}))


(defn simple-update [^SimpleApplication app tpf]
  ;; also can be accessed like (::jme/app @states)
  (let [{:keys [player]} (get-state)]
    (rotate player 0 (* 2 tpf) 0)))


(defsimpleapp app
              :init init
              :update simple-update)


(comment
 (start-app app)
 (stop-app app)

 (unbind-app #'app)
 )
