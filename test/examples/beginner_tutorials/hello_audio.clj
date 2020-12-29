;; Please start your REPL with `+test` profile
(ns examples.beginner-tutorials.hello-audio
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_audio.html"
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.input MouseInput)
   (com.jme3.math ColorRGBA)))


;; for keeping internal *bindings* work, also the app. We need to define
;; listeners with `defn`. `def` should NOT be used!
(defn on-action-listener []
  (action-listener
   (fn [name pressed? tpf]
     (when (and (= name ::shoot) (not pressed?))
       (play (:audio-gun (get-state)))))))


(defn- init-keys []
  (apply-input-mapping
   ;; Using qualified keywords for inputs is highly recommended!
   {:triggers  {::shoot (mouse-trigger MouseInput/BUTTON_LEFT)}
    :listeners {(on-action-listener) ::shoot}}))


(defn init-audio []
  ;;`letj` returns hash-map with key-val pairs that extracted from bindings
  ;; ignores `_` bindings. So in this case it returns;
  ;; {:audio-gun audio-gun
  ;;  :audio-nature audio-nature}
  ;; When we return hash map, it will be added to jme-clj.core/states with key :jme-clj.core/app
  ;; so we can access from everywhere, for example inside `update` fn etc.
  (letj [audio-gun (audio-node "Sound/Effects/Gun.wav" :buffer)
         audio-nature (audio-node "Sound/Environment/Ocean Waves.ogg" :stream)]
        (setc audio-gun
              :positional false
              :looping false
              :volume 2)
        (setc audio-nature
              :positional true
              :looping true
              :volume 3)
        (-> (root-node)
            (attach-child audio-gun)
            (attach-child audio-nature))
        (play audio-nature)))


(defn simple-update [tpf]
  (setc (listener)
        :location (get* (cam) :location)
        :rotation (get* (cam) :rotation)))


(defn init []
  (set* (fly-cam) :move-speed 40)
  (let [box    (box 1 1 1)
        player (geo "Player" box)
        mat    (material "Common/MatDefs/Misc/Unshaded.j3md")]
    (set* mat :color "Color" ColorRGBA/Blue)
    (set* player :material mat)
    (add-to-root player)
    (init-keys)
    (assoc (init-audio) :player player)))


(defsimpleapp app
              :init init
              :update simple-update)


(comment
 (start app)

 ;;after calling unbind-app, we need to re-define the app with defsimpleapp
 (unbind-app #'app)

 (run app
      (re-init init))
 )
