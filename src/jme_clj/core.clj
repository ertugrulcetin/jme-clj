(ns jme-clj.core
  (:require
   [camel-snake-kebab.core :as csk]
   [kezban.core :as k])
  (:import
   (clojure.lang Var)
   (com.jme3.animation AnimEventListener AnimControl AnimChannel)
   (com.jme3.app SimpleApplication)
   (com.jme3.asset AssetManager)
   (com.jme3.collision CollisionResults Collidable)
   (com.jme3.font BitmapText)
   (com.jme3.input InputManager)
   (com.jme3.input.controls ActionListener
                            AnalogListener
                            KeyTrigger
                            MouseAxisTrigger
                            MouseButtonTrigger
                            Trigger)
   (com.jme3.light AmbientLight
                   DirectionalLight
                   LightProbe
                   PointLight
                   SpotLight)
   (com.jme3.material Material)
   (com.jme3.math Vector3f Ray)
   (com.jme3.scene Geometry Node Spatial Mesh)
   (com.jme3.scene.shape Box Sphere)
   (com.jme3.system AppSettings)
   (com.jme3.util TangentBinormalGenerator)
   (java.util Collection)))

(set! *warn-on-reflection* true)

(defonce states (atom {}))
(defonce ^:private listeners (atom []))

(def ^{:dynamic true
       :tag     SimpleApplication}
  *app* nil)


(defn get-state []
  (::app @states))


(defn update-state [f & args]
  (apply swap! (into [states f] args)))


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


(defn get-manager [type]
  (case type
    :asset (.getAssetManager *app*)
    :input (.getInputManager *app*)
    :app-state (.getStateManager *app*)
    :render (.getRenderManager *app*)))


(defn ^Node root-node []
  (.getRootNode *app*))


(defn gui-node []
  (.getGuiNode *app*))


(defn view-port []
  (.getViewPort *app*))


(defn load-model [path]
  (.loadModel ^AssetManager (get-manager :asset) ^String path))


(defn load-texture [path]
  (.loadTexture ^AssetManager (get-manager :asset) ^String path))


(defn load-font [path]
  (.loadFont ^AssetManager (get-manager :asset) path))


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


(defn add-to-root [^Node node]
  (.attachChild (root-node) node)
  node)


(defn remove-from-root [^Node node]
  (.detachChild (root-node) node)
  node)


(defn light [type]
  (case type
    :directional (DirectionalLight.)
    :ambient (AmbientLight.)
    :probe (LightProbe.)
    :point (PointLight.)
    :spot (SpotLight.)))


(defn sphere [x y z]
  (Sphere. x y z))


(defn generate [^Mesh mesh]
  (TangentBinormalGenerator/generate mesh))


(defn node [name]
  (Node. name))


(defn rotate [^Spatial spatial x y z]
  (.rotate spatial x y z))


(defn scale
  ([^Spatial spatial s]
   (.scale spatial s))
  ([^Spatial spatial x y z]
   (.scale spatial x y z)))


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


(defn add-light [^Spatial spatial light]
  (doto spatial (.addLight light)))


(defn remove-light [^Spatial spatial light]
  (doto spatial (.removeLight light)))


(defn add-light-to-root [light]
  (add-light (root-node) light))


(defn key-trigger [code]
  (KeyTrigger. code))


(defn mouse-trigger [code]
  (MouseButtonTrigger. code))


(defn mouse-ax-trigger [code negative?]
  (MouseAxisTrigger. code negative?))


(defn- create-input-mapping [m]
  (doseq [[k v] m]
    (let [^InputManager input-manager (get-manager :input)]
      (.deleteMapping input-manager k)
      (.addMapping input-manager k (into-array Trigger (if (vector? v) v [v])))
      m)))


;;TODO we're still removing all listeners!
(defn- register-input-mapping [m]
  (let [^InputManager input-manager (get-manager :input)]
    (doseq [l @listeners]
      (.removeListener input-manager l)
      (reset! listeners []))
    (doseq [[k v] m]
      (.addListener input-manager k (into-array String (if (vector? v) v [v])))
      (swap! listeners conj k))
    m))


(defn apply-input-mapping [{:keys [triggers listeners] :as m}]
  (create-input-mapping triggers)
  (register-input-mapping listeners)
  m)


(defn create-action-listener [f]
  (let [f (bound-fn* f)]
    (proxy [ActionListener] []
      (onAction [name pressed? tpf]
        (f name pressed? tpf)))))


(defn create-analog-listener [f]
  (let [f (bound-fn* f)]
    (proxy [AnalogListener] []
      (onAnalog [name value tpf]
        (f name value tpf)))))


(defn create-anim-listener [on-cycle-done on-anim-change]
  (let [on-cycle-done  (bound-fn* on-cycle-done)
        on-anim-change (bound-fn* on-anim-change)]
    (proxy [AnimEventListener] []
      (onAnimCycleDone [^AnimControl control ^AnimChannel channel ^String name]
        (on-cycle-done control channel name))
      (onAnimChange [^AnimControl control ^AnimChannel channel ^String name]
        (on-anim-change control channel name)))))


;;TODO check here, we might need to remove old ones
;;TODO like in input listeners to avoid duplication!
(defn add-anim-listener [^AnimControl control ^AnimEventListener listener]
  (.addListener control listener))


(defn create-channel [^AnimControl control]
  (.createChannel control))


(defmacro letj
  [bindings & body]
  (k/assert-all
   (vector? bindings) "a vector for its binding"
   (even? (count bindings)) "an even number of forms in binding vector")
  `(let* ~(destructure bindings)
     ~@body
     (merge ~@(remove :_ (map #(hash-map (keyword %) %) (take-nth 2 bindings))))))


(defn cam []
  (.getCamera *app*))


(defn fly-cam []
  (.getFlyByCamera *app*))


(defn collision-results []
  (CollisionResults.))


(defn ray [origin direction]
  (Ray. origin direction))


(defn collide-with [^Collidable o collidable results]
  (.collideWith o collidable results)
  results)


(defn size [^CollisionResults o]
  (.size o))


(defmacro defsimpleapp
  [name & {:keys [opts init update]}]
  `(defonce ~name (let [app# (proxy [SimpleApplication] []
                               (simpleInitApp []
                                 (binding [*app* ~'this]
                                   ;;re-init and this block has to be same.
                                   (let [init-result# (~init)]
                                     (when (map? init-result#)
                                       (swap! states assoc ::app init-result#)))))
                               (simpleUpdate [tpf#]
                                 (binding [*app* ~'this]
                                   (let [update-result# ((or ~update
                                                             (constantly nil)) tpf#)]
                                     (when (map? update-result#)
                                       (swap! states update ::app merge update-result#))))))]
                    (when (seq ~opts)
                      (some->> ~opts :show-settings? (.setShowSettings app#))
                      (some->> ~opts :pause-on-lost-focus? (.setPauseOnLostFocus app#))
                      (some->> ~opts :settings map->app-settings (.setSettings app#)))
                    app#)))


(defn start [^SimpleApplication app]
  (doto app .start))


;;TODO stop app makes input not working after re-start
;;TODO try to find a way
(defn stop [^SimpleApplication app]
  (clear app)
  (doto app (.stop true)))


(defn re-init [app init-fn]
  (binding [*app* app]
    (clear app)
    (let [init-result (init-fn)]
      (when (map? init-result)
        (swap! states assoc ::app init-result)))))


(defn unbind-app
  "Should be used for development purposes. (unbind-app #'my-ns/app)
   After calling `unbind-app`, `app` can be re-defined using `defsimpleapp`"
  [^Var v]
  (when (bound? v)
    (stop @v)
    (.unbindRoot v)))


(defn app-running? [^SimpleApplication app]
  (boolean (some-> app .getContext .isCreated)))
