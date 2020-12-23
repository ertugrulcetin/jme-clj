;; Please start your REPL with `+test` profile
(ns examples.custom-control
  "Please have a look https://wiki.jmonkeyengine.org/docs/3.3/core/scene/control/custom_controls.html for more."
  (:require [jme-clj.core :refer :all]))


(defn init []
  (let [sun      (-> (light :directional) (set* :direction (vec3 -0.1 -0.7 -1.0)))
        spatial  (load-model "Models/Oto/Oto.mesh.xml")
        control* (control ::my-control
                          :update (fn [tpf]
                                    (rotate (get-spatial) 0 (* 2 tpf) 0)))]
    (-> spatial
        (scale 0.5)
        (add-control control*)
        (add-light sun)
        (add-to-root))))


(defsimpleapp app :init init)


(comment
 (start app)

 ;;after calling unbind-app, we need to re-define the app with defsimpleapp
 (unbind-app #'app)

 (run app
      (re-init init))
 )
