(ns examples.fps.app-states
  (:require [jme-clj.core :refer :all]))


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
