(ns examples.simple-multiplayer.player-app-state
  (:require [jme-clj.core :refer :all]))


(defn- init-as [bullet-as]
  (let [player         (load-model "Models/Oto/OtoOldAnim.j3o")
        player-control (character-control (capsule-collision-shape 3.0 4.0) 0.01)]
    (setc player-control
          :gravity 30
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


(defn create-player-as [bullet-as]
  (app-state ::player
             :init #(init-as bullet-as)
             :update update-as))
