(ns jme-clj.core
  (:require
   [camel-snake-kebab.core :as csk]
   [kezban.core :as k])
  (:import
   (com.jme3.app SimpleApplication)
   (com.jme3.light DirectionalLight AmbientLight LightProbe PointLight SpotLight)
   (com.jme3.material Material)
   (com.jme3.math Vector3f)
   (com.jme3.scene Geometry Node Spatial)
   (com.jme3.scene.shape Box)
   (com.jme3.system AppSettings)
   (com.jme3.font BitmapText)
   (com.jme3.input.controls KeyTrigger Trigger MouseButtonTrigger MouseAxisTrigger ActionListener AnalogListener)))


(defonce states (atom {}))
(defonce ^:private listeners (atom []))
(def ^:dynamic *asset-manager* nil)
(def ^:dynamic *input-manager* nil)


(defn get-simple-app-states []
  (::app @states))


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


(defn detach-all-child [^Node node]
  (doto node (.detachAllChildren)))


(defn clear [^SimpleApplication app]
  (let [root-node (.getRootNode app)]
    (detach-all-child root-node)
    (.clear (.getLocalLightList root-node))
    (reset! states {})
    root-node))


(defn re-init [app init-fn]
  (binding [*asset-manager* (.getAssetManager app)
            *input-manager* (.getInputManager app)]
    (clear app)
    (init-fn app)))


(defn load-model [path]
  (.loadModel *asset-manager* path))


(defn load-texture [path]
  (.loadTexture *asset-manager* path))


(defn load-font [path]
  (.loadFont *asset-manager* path))


(defn bitmap-text [gui-font right-to-left]
  (BitmapText. gui-font right-to-left))


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


(defn gui-node [app]
  (.getGuiNode app))


(defn node [name]
  (Node. name))


(defn rotate [spatial x y z]
  (.rotate spatial x y z))


(defn scale [spatial x y z]
  (.scale spatial x y z))


(defmacro set* [obj kw & args]
  `(let [result# (eval ~`(do ~obj))]
     (~(symbol (csk/->camelCase (str ".set-" (name kw)))) result# ~@args)
     result#))


(defmacro get* [obj kw & args]
  `(~(symbol (csk/->camelCase (str ".get-" (name kw)))) ~obj ~@args))


(defn map->app-settings [settings]
  (when (seq settings)
    (apply app-settings
           (into (-> settings :load-defaults? vector)
                 (-> settings seq flatten)))))


(defn add-light [spatial light]
  (doto spatial (.addLight light)))


(defn remove-light [spatial light]
  (doto spatial (.removeLight light)))


(defn key-trigger [code]
  (KeyTrigger. code))


(defn mouse-trigger [code]
  (MouseButtonTrigger. code))


(defn mouse-ax-trigger [code negative?]
  (MouseAxisTrigger. code negative?))


(defn- create-input-mapping [m]
  (doseq [[k v] m]
    (.deleteMapping *input-manager* k)
    (.addMapping *input-manager* k (into-array Trigger (if (vector? v) v [v])))
    m))


(defn- register-input-mapping [m]
  (doseq [l @listeners]
    (.removeListener *input-manager* l)
    (reset! listeners []))
  (doseq [[k v] m]
    (.addListener *input-manager* k (into-array String (if (vector? v) v [v])))
    (swap! listeners conj k)
    m))


(defn apply-input-mapping [{:keys [triggers listeners]}]
  (create-input-mapping triggers)
  (register-input-mapping listeners))


(defn create-action-listener [f]
  (proxy [ActionListener] []
    (onAction [name pressed? tpf]
      (f name pressed? tpf))))


(defn create-analog-listener [f]
  (proxy [AnalogListener] []
    (onAnalog [name value tpf]
      (f name value tpf))))


(defmacro letj
  [bindings & body]
  (k/assert-all
   (vector? bindings) "a vector for its binding"
   (even? (count bindings)) "an even number of forms in binding vector")
  `(let* ~(destructure bindings)
     ~@body
     (merge ~@(map #(hash-map (keyword %) %) (take-nth 2 bindings)))))


(defmacro defsimpleapp
  [name & {:keys [opts init update]}]
  `(defonce ~name (let [app# (proxy [SimpleApplication] []
                               (simpleInitApp []
                                 (binding [*asset-manager* (get-manager ~'this :asset)
                                           *input-manager* (get-manager ~'this :input)]
                                   (let [init-result# (~init ~'this)]
                                     (when (map? init-result#)
                                       (swap! states assoc ::app init-result#)))))
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


(defn start-app [app]
  (doto app .start))


(defn stop-app [app]
  (clear app)
  (doto app (.stop true)))


(defn re-start [app]
  (doto app .restart))


(defn unbind-app
  "Should be used for development purposes. (unbind-app #'my-ns/app)
   After calling `unbind-app`, `app` can be re-defined using `defsimpleapp`"
  [v]
  (when (bound? v)
    (stop-app @v)
    (.unbindRoot v)))


(defn app-running? [app]
  (boolean (some-> app .getContext .isCreated)))
