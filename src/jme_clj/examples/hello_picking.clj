(ns jme-clj.examples.hello-picking
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_picking.html"
  (:require
   [jme-clj.core :refer :all :as jme])
  (:import
   (com.jme3.input KeyInput MouseInput)
   (com.jme3.math ColorRGBA)))


;; for keeping internal *bindings* work, also the app. We need to define
;; listeners with `defn`. `def` should NOT be used!
(defn action-listener []
  (create-action-listener
   (fn [name pressed? tpf]
     (when (and (= name :shoot) (not pressed?))
       (let [{:keys [mark shootables]} (get-state)
             ray     (ray (get* (cam) :location)
                          (get* (cam) :direction))
             results (collide-with shootables ray (collision-results))]
         (doseq [idx (range (size results))]
           (let [dist (-> results (get* :collision idx) (get* :distance))
                 pt   (-> results (get* :collision idx) (get* :contact-point))
                 hit  (-> results (get* :collision idx) (get* :geometry) (get* :name))]
             (println "* Collision #" idx)
             (println "  You shot " hit " at " pt ", " dist " wu away.")))
         (if (> (size results) 0)
           (let [contact-point (-> results
                                   (get* :closest-collision)
                                   (get* :contact-point))]
             (-> mark
                 (set* :local-translation contact-point)
                 (add-to-root)))
           (remove-from-root mark)))))))


;;A cube object for target practice
(defn make-cube [name x y z]
  (let [box (box 1 1 1)
        mat (-> (get-manager :asset)
                (material "Common/MatDefs/Misc/Unshaded.j3md")
                (set* :color "Color" (ColorRGBA/randomColor)))]
    (-> (geo name box)
        (set* :local-translation x y z)
        (set* :material mat))))


;;A floor to show that the "shot" can go through several objects.
(defn make-floor []
  (let [box (box 15 0.2 15)
        mat (-> (get-manager :asset)
                (material "Common/MatDefs/Misc/Unshaded.j3md")
                (set* :color "Color" ColorRGBA/Gray))]
    (-> (geo "the Floor" box)
        (set* :local-translation 0 -4 -5)
        (set* :material mat))))


(defn make-char []
  (let [sun (-> (light :directional)
                (set* :direction (vec3 -0.1 -0.7 -1.0)))]
    ;;load a character from jme3test-test-data
    (-> (load-model "Models/Oto/Oto.mesh.xml")
        (scale 0.5)
        (set* :local-translation -1.0 -1.5 -0.6)
        ;;We must add a light to make the model visible
        (add-light sun))))


(defn- init-keys []
  (apply-input-mapping
   {:triggers  {:shoot [(key-trigger KeyInput/KEY_SPACE)
                        (mouse-trigger MouseInput/BUTTON_LEFT)]}
    :listeners {(action-listener) :shoot}}))


;;A centred plus sign to help the player aim.
(defn init-cross-hairs []
  (set* jme/*app* :display-stat-view false)
  (let [gui-font (load-font "Interface/Fonts/Default.fnt")
        settings (-> jme/*app* (get* :context) (get* :settings))
        ch       (bitmap-text gui-font false)]
    (-> ch
        (set* :size (-> gui-font
                        (get* :char-set)
                        (get* :rendered-size)
                        (* 2)))
        (set* :text "+")
        (set* :local-translation
              (- (/ (get* settings :width) 2)
                 (/ (get* ch :line-width) 2))
              (+ (/ (get* settings :height) 2)
                 (/ (get* ch :line-height) 2))
              0)
        (#(attach-child (gui-node) %)))))


;;A red ball that marks the last spot that was "hit" by the "shot".
(defn- init-mark []
  (let [sphere (sphere 30 30 0.2)
        mark   (geo "BOOM!" sphere)
        mat    (-> (get-manager :asset)
                   (material "Common/MatDefs/Misc/Unshaded.j3md")
                   (set* :color "Color" ColorRGBA/Red))]
    (set* mark :material mat)))


(defn init []
  ;;`letj` returns hash-map with key-val pairs that extracted from bindings
  ;; ignores `_` bindings. So in this case it returns;
  ;; {:mark mark
  ;;  :shootables shootables}
  ;; When we return hash map, it will be added to jme-clj.core/states with key :jme-clj.core/app
  ;; so we can access from everywhere, for example inside `update` fn etc.
  (letj [shootables (node "Shootables")
         _ (init-keys)
         _ (init-cross-hairs)
         mark (init-mark)]
        (-> shootables
            (add-to-root)
            (attach-child (make-cube "a Dragon" -2 0 1))
            (attach-child (make-cube "a tin can" 1 -2 0))
            (attach-child (make-cube "the Sheriff" 0 1 -2))
            (attach-child (make-cube "the Deputy" 1 0 -4))
            (attach-child (make-floor))
            (attach-child (make-char)))))


(defsimpleapp app :init init)


(comment
 (start app)
 (stop app)

 (re-init app init)

 (unbind-app #'app))
