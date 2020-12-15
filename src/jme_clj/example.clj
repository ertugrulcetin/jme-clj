(ns jme-clj.example
  (:require
   [jme-clj.core :refer :all :as jme])
  (:import
   (com.jme3.app SimpleApplication)
   (com.jme3.math ColorRGBA)))


(defn init [^SimpleApplication app]
  (letj [b (box 1 1 1)
         geom (geo "Box" b)
         asset-manager (get-manager app :asset)
         mat (material asset-manager "Common/MatDefs/Misc/Unshaded.j3md")
         model (load-model asset-manager "Models/Oto/OtoOldAnim.j3o")
         light (light :directional)
         root-node (root-node app)
         counter 1]
        (add-light root-node light)
        (set* light :direction (vec3 -0.1 -1 -1 :normalize))
        (set* mat :color "Color" ColorRGBA/Blue)
        (set* geom :material mat)
        (-> root-node
            (attach-child geom)
            (attach-child model))))


(defn simple-update [^SimpleApplication app tpf]
  (let [{:keys [counter]} (::jme/app @states)]
    {:counter (inc counter)}))


(defsimpleapp app
              :opts {:show-settings?       false
                     :pause-on-lost-focus? false
                     :settings             {:load-defaults? true
                                            :title          "My JME Game"
                                            :vsync?         true
                                            :frame-rate     60}}
              :init init
              :update simple-update)


(comment
 (start-app app)
 (stop-app app)
 (re-init app init)
 (unbind-app #'app)
 @states
 )
