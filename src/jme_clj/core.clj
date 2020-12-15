(ns jme-clj.core
  (:require
   [camel-snake-kebab.core :as csk]
   [kezban.core :as k])
  (:import
   (com.jme3.app SimpleApplication)
   (com.jme3.scene.shape Box)
   (com.jme3.scene Geometry Node Spatial)
   (com.jme3.material Material)
   (com.jme3.math ColorRGBA Vector3f)
   (com.jme3.system AppSettings)
   (com.jme3.light DirectionalLight AmbientLight LightProbe PointLight SpotLight)))


(defonce states (atom {}))


(defn app-settings [load-defaults? & {:keys [fullscreen?
                                             vsync?
                                             width
                                             height
                                             frequency
                                             title
                                             frame-rate
                                             resizable?]}]
  (let [settings (AppSettings. (boolean load-defaults?))]
    (some->> fullscreen? (.setFullscreen settings))
    (some->> vsync? (.setVSync settings))
    (some->> width (.setWidth settings))
    (some->> height (.setHeight settings))
    (some->> title (.setTitle settings))
    (some->> frequency (.setFrequency settings))
    (some->> frame-rate (.setFrameRate settings))
    (some->> resizable? (.setResizable settings))
    settings))


(defn ^Vector3f vec3
  ([x y z]
   (vec3 x y z false))
  ([x y z normalize]
   (if (= normalize :normalize)
     (.normalize (Vector3f. x y z))
     (Vector3f. x y z))))


(defn clear [^SimpleApplication app]
  (let [root-node (.getRootNode app)]
    (.detachAllChildren root-node)
    (.clear (.getLocalLightList root-node))
    root-node))


(defn re-init [app init-fn]
  (clear app)
  (init-fn app))


(defn load-model [asset-manager path]
  (.loadModel asset-manager path))


(defn box [x y z]
  (Box. x y z))


(defn geo [name mesh]
  (Geometry. name mesh))


(defn material [asset-manager path]
  (Material. asset-manager path))


(defn attach-child [^Node node ^Spatial s]
  (doto node (.attachChild s)))


(defn light [type]
  (case type
    :directional (DirectionalLight.)
    :ambient (AmbientLight.)
    :probe (LightProbe.)
    :point (PointLight.)
    :spot (SpotLight.)))


(defn get-manager [app type]
  (case type
    :asset (.getAssetManager app)
    :input (.getInputManager app)
    :app-state (.getStateManager app)
    :render (.getRenderManager app)))


(defn root-node [app]
  (.getRootNode app))


(defmacro set* [obj kw & args]
  `(do
     (~(symbol (csk/->camelCase (str ".set-" (name kw)))) ~obj ~@args)
     ~obj))


(defmacro get* [obj kw & args]
  `(~(symbol (csk/->camelCase (str ".get-" (name kw)))) ~obj ~@args))


(defn- map->app-settings [settings]
  (when (seq settings)
    (apply app-settings
           (into (-> settings :load-defaults? vector)
                 (-> settings seq flatten)))))


(defn add-light [spatial light]
  (doto spatial (.addLight light)))


(defn remove-light [spatial light]
  (doto spatial (.removeLight light)))


(defmacro letj
  [bindings & body]
  (k/assert-all
   (vector? bindings) "a vector for its binding"
   (even? (count bindings)) "an even number of forms in binding vector")
  `(let* ~(destructure bindings)
     ~@body
     (merge ~@(map #(hash-map (keyword %) %) (take-nth 2 bindings)))))


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
  (let [{:keys [counter]} (::app @states)]
    #_(println "Counter: " counter)
    {:counter (inc counter)}))


(defmacro defsimpleapp
  [name & {:keys [opts init update]}]
  `(defonce ~name (let [app# (proxy [SimpleApplication] []
                               (simpleInitApp []
                                 (let [init-result# (~init ~'this)]
                                   (when (map? init-result#)
                                     (swap! states assoc ::app init-result#))))
                               (simpleUpdate [tpf#]
                                 (let [update-result# ((or ~update
                                                           (constantly nil)) ~'this tpf#)]
                                   (when (map? update-result#)
                                     (swap! states update ::app merge update-result#)))))]
                    (when (seq ~opts)
                      (some->> ~opts :show-settings? (.setShowSettings app#))
                      (some->> ~opts :pause-on-lost-focus? (.setPauseOnLostFocus app#))
                      (some->> ~opts :settings map->app-settings (.setSettings app#)))
                    app#)))


(defsimpleapp app
              :opts {:show-settings?       false
                     :pause-on-lost-focus? false
                     :settings             {:load-defaults? true
                                            :title          "My JME Game"
                                            :vsync?         true
                                            :frame-rate     60}}
              :init init
              :update simple-update)



(defn start-app [app]
  (doto app .start))


(defn stop-app [app]
  (doto app .stop))


(defn unbind-app
  "Should be used for development purposes. (unbind-app #'my-ns/app)
   After calling `unbind-app`, `app` can be re-defined using `defsimpleapp`"
  [v]
  (when (bound? v)
    (stop-app @v)
    (.unbindRoot v)))


(defn running? [app]
  (boolean (some-> app .getContext .isCreated)))

(comment
 (start-app app)
 (stop-app app)
 (re-init app init)
 (unbind-app #'app)
 @states
 )
