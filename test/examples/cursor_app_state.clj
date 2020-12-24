;; Please start your REPL with `+test` profile
(ns examples.cursor-app-state
  "Please have a look https://wiki.jmonkeyengine.org/docs/3.3/core/app/state/application_states.html for more."
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.math ColorRGBA FastMath)))


(defn- raw-input-listener* [app-state-kw]
  (let [x (atom 0)
        y (atom 0)]
    (raw-input-listener
     :on-mouse-motion-event (fn [evt]
                              (swap! x + (.getDX evt))
                              (swap! y + (.getDY evt))
                              (let [app-settings (get* (context) :settings)
                                    x            (int (FastMath/clamp @x 0 (get* app-settings :width)))
                                    y            (int (FastMath/clamp @y 0 (get* app-settings :height)))
                                    {:keys [cursor]} (app-state-kw (get-state :app-state))]
                                ;; we couldn't use `setc` or `set*` due to `setxHotSpot` convention. `x` is lower-case.
                                (doto cursor
                                  (.setxHotSpot x)
                                  (.setyHotSpot (- y (get* cursor :height)))))))))


(defn- cursor-app-state []
  (app-state ::cursor
             :init (fn []
                     (set* (fly-cam) :enabled false)
                     (let [cursor (load-asset "Textures/Cursors/meme.cur")]
                       (-> (input-manager)
                           (set* :cursor-visible true)
                           (set* :mouse-cursor cursor)
                           (call* :add-raw-input-listener (raw-input-listener* ::cursor)))
                       {:cursor cursor}))))


(defn init []
  (let [box  (box 1 1 1)
        geom (geo "Box" box)
        mat  (material "Common/MatDefs/Misc/Unshaded.j3md")]
    (attach (cursor-app-state))
    (set* mat :color "Color" ColorRGBA/Blue)
    (set* geom :material mat)
    (add-to-root geom)))


(defsimpleapp app :init init)


(comment
 (start app)

 ;;after calling unbind-app, we need to re-define the app with defsimpleapp
 (unbind-app #'app)

 (run app
      (re-init init))
 )
