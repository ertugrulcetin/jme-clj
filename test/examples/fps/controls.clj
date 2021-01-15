(ns examples.fps.controls
  (:require [jme-clj.core :refer :all])
  (:import (com.jme3.input KeyInput MouseInput)))


(defn- find-node-got-shot [geometry]
  (->> geometry
       (iterate (memfn getParent))
       (take-while some?)
       (filter #(get* % :user-data "name"))
       first))


(defn- get-damage [distance]
  (* (Math/pow 2 (/ distance (rand-nth (range 110 130)))) 3))


(defn- on-action-listener []
  (action-listener
   (fn [name* pressed? tpf]
     (let [{:keys [player shootables audio bullet-app-state damage-filter]} (get-state)]
       (cond
         (= ::shoot name*) (when pressed?
                             (when-let [{:keys [distance geometry]} (create-ray-test shootables)]
                               ;(play-ins audio)
                               (let [node   (find-node-got-shot geometry)
                                     damage (get-damage distance)
                                     hp     (get* node :user-data "hp")
                                     hp     (Math/max (long (- hp damage)) 0)]
                                 (set-state :control [::user-input :damage] (long damage))
                                 (set-state :control [::user-input :damage-timer] 0)
                                 (call* (view-port) :add-processor damage-filter)
                                 (set* node :user-data "hp" hp)
                                 (when (= hp 0)
                                   (-> bullet-app-state
                                       (get* :physics-space)
                                       (call* :remove-all node))
                                   (remove-from-parent node)))))
         (= ::jump name*) (when pressed? (call* player :jump))
         :else (set-state :control [::user-input (-> name* name keyword)] pressed?))))))


(defn- set-up-keys []
  (apply-input-mapping
   {:triggers  {::shoot (mouse-trigger MouseInput/BUTTON_LEFT)
                ::left  (key-trigger KeyInput/KEY_A)
                ::right (key-trigger KeyInput/KEY_D)
                ::up    (key-trigger KeyInput/KEY_W)
                ::down  (key-trigger KeyInput/KEY_S)
                ::jump  (key-trigger KeyInput/KEY_SPACE)}
    :listeners {(on-action-listener) [::shoot ::left ::right ::up ::down ::jump]}}))


(defn- get-available-loc [player terrain]
  (let [loc         (get* player :physics-location)
        ^Float size (- (get* terrain :terrain-size) 2)
        x           (Math/max ^Float (- size) (Math/min ^Float (.-x loc) size))
        z           (Math/max ^Float (- size) (Math/min ^Float (.-z loc) size))]
    (doto loc
      (.setX x)
      (.setZ z))))


(defn- render-hit-text [state]
  (detach-child (gui-node) (:damage-text state))
  (call* (view-port) :remove-processor (get-state :app :damage-filter))
  (when (<= (:damage-timer state) 0.5)
    (call* (view-port) :add-processor (get-state :app :damage-filter))
    (let [text     (:damage-text state)
          gui-font (get* text :font)
          settings (get* (context) :settings)
          padding  (if (>= (:damage state) 10) 20 15)]
      (detach-child (gui-node) text)
      (-> text
          (setc :size (-> gui-font
                          (get* :char-set)
                          (get* :rendered-size)
                          (* 1.5))
                :text (str (:damage state))
                :color (color-rgba 0 70 0 1)
                :local-translation [(+ (- (/ (get* settings :width) 2)
                                          (/ (get* text :line-width) 2))
                                       padding)
                                    (+ (/ (get* settings :height) 2)
                                       (/ (get* text :line-height) 2)
                                       padding)
                                    0])
          (#(attach-child (gui-node) %))))))


(defn create-user-input [player terrain]
  (control ::user-input
           :init (fn []
                   (set-up-keys)
                   {:walk-direction (vec3)
                    :cam-dir        (vec3)
                    :cam-left       (vec3)
                    :up             false
                    :down           false
                    :left           false
                    :right          false
                    :damage         0
                    :damage-timer   0
                    :damage-text    (bitmap-text)})
           :update (fn [tpf]
                     (update-state :control [::user-input :damage-timer] + tpf)
                     (let [state          (get-state :control ::user-input)
                           cam-dir        (-> (:cam-dir state) (setv (get* (cam) :direction)) (mult-loc 0.6))
                           cam-left       (-> (:cam-left state) (setv (get* (cam) :left)) (mult-loc 0.4))
                           walk-direction (setv (:walk-direction state) 0 0 0)
                           walk-direction (cond-> walk-direction
                                                  (:left state) (add-loc cam-left)
                                                  (:right state) (add-loc (negate cam-left))
                                                  (:up state) (add-loc cam-dir)
                                                  (:down state) (add-loc (negate cam-dir)))
                           loc            (get-available-loc player terrain)]
                       (set* player :walk-direction walk-direction)
                       (set* player :physics-location loc)
                       (set* (cam) :location loc)
                       (render-hit-text state)))))
