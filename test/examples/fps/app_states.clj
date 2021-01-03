(ns examples.fps.app-states
  (:require [jme-clj.core :refer :all]
            [kezban.core :refer [when-let*]])
  (:import (com.jme3.math ColorRGBA)))


(defn create-cross-hairs []
  (app-state ::cross-hair
             :init (fn []
                     (let [gui-font (load-font "Interface/Fonts/Default.fnt")
                           settings (get* (context) :settings)
                           ch       (bitmap-text gui-font false)]
                       (-> ch
                           (setc :size (-> gui-font
                                           (get* :char-set)
                                           (get* :rendered-size)
                                           (* 2))
                                 :text "+"
                                 :local-translation [(- (/ (get* settings :width) 2)
                                                        (/ (get* ch :line-width) 2))
                                                     (+ (/ (get* settings :height) 2)
                                                        (/ (get* ch :line-height) 2))
                                                     0])
                           (#(attach-child (gui-node) %)))
                       {:ch ch}))
             :cleanup #(some->> (get-state :app-state) ::cross-hair :ch (detach-child (gui-node)))))


(defn create-hp []
  (app-state ::hp
             :init (fn []
                     {:hp-text (bitmap-text)})
             :update (fn [tpf]
                       (when-let* [hp-text (get-state :app-state [::hp :hp-text])
                                   hp (-> (get-state) :player-data :hp)
                                   settings (get* (context) :settings)]
                                  (detach-child (gui-node) hp-text)
                                  (-> hp-text
                                      (setc :size (-> (get* hp-text :font)
                                                      (get* :char-set)
                                                      (get* :rendered-size)
                                                      (* 2))
                                            :text (str hp)
                                            :color (cond
                                                     (> hp 70) ColorRGBA/Green
                                                     (<= 40 hp 70) ColorRGBA/Orange
                                                     :else ColorRGBA/Red)
                                            :local-translation [(/ (get* settings :width) 2)
                                                                (get* hp-text :line-height)
                                                                0])
                                      (#(attach-child (gui-node) %)))))))
