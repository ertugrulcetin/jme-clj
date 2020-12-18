(ns jme-clj.core
  (:require
   [camel-snake-kebab.core :as csk]
   [kezban.core :as k])
  (:import
   (clojure.lang Var)
   (com.jme3.animation AnimEventListener AnimControl AnimChannel)
   (com.jme3.app SimpleApplication)
   (com.jme3.app.state AppStateManager)
   (com.jme3.asset AssetManager)
   (com.jme3.bullet BulletAppState)
   (com.jme3.bullet.collision.shapes CapsuleCollisionShape)
   (com.jme3.bullet.control RigidBodyControl CharacterControl)
   (com.jme3.bullet.util CollisionShapeFactory)
   (com.jme3.collision CollisionResults Collidable)
   (com.jme3.font BitmapText)
   (com.jme3.input InputManager)
   (com.jme3.input.controls
    ActionListener
    AnalogListener
    KeyTrigger
    MouseAxisTrigger
    MouseButtonTrigger
    Trigger)
   (com.jme3.light
    AmbientLight
    DirectionalLight
    LightProbe
    PointLight
    SpotLight)
   (com.jme3.material Material)
   (com.jme3.math Vector3f Ray ColorRGBA)
   (com.jme3.scene Geometry Node Spatial Mesh)
   (com.jme3.scene.shape Box Sphere)
   (com.jme3.system AppSettings)
   (com.jme3.util TangentBinormalGenerator)
   (com.jme3.terrain.heightmap ImageBasedHeightMap HeightMap)
   (com.jme3.texture Texture)
   (com.jme3.terrain.geomipmap TerrainQuad TerrainLodControl)
   (com.jme3.terrain Terrain)
   (com.jme3.renderer Camera)))

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


(defn set-state [kws v]
  (let [kws (if (vector? kws) kws [kws])
        kws (into [::app] kws)]
    (swap! states assoc-in kws v)))


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


(defn vec3
  ([]
   (Vector3f.))
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


(defn bullet-app-state []
  (BulletAppState.))


(defn capsule-collision-shape [radius height axi]
  (CapsuleCollisionShape. radius height axi))


(defn add-control [^Spatial spatial control]
  (doto spatial (.addControl control)))


(defn character-control [shape step-height]
  (CharacterControl. shape step-height))


(defn load-model [path]
  (.loadModel ^AssetManager (get-manager :asset) ^String path))


(defn load-texture [path]
  (.loadTexture ^AssetManager (get-manager :asset) ^String path))


(defn load-font [path]
  (.loadFont ^AssetManager (get-manager :asset) path))


(defn bitmap-text [gui-font right-to-left]
  (BitmapText. gui-font right-to-left))


(defn mult [^Vector3f v ^Float scalar]
  (.mult v scalar))


(defn mult-local [^Vector3f v ^Float scalar]
  (.multLocal v scalar))


(defn add-v3
  ([^Vector3f v ^Vector3f v2]
   (.add v v2))
  ([^Vector3f v x y z]
   (.add v x y z)))


(defn add-v3-local
  ([^Vector3f v ^Vector3f v2]
   (.addLocal v v2))
  ([^Vector3f v x y z]
   (.addLocal v x y z)))


(defn set-v3
  ([^Vector3f v ^Vector3f v2]
   (.set v v2))
  ([^Vector3f v x y z]
   (.set v x y z)))


(defn negate [^Vector3f v]
  (.negate v))


(defn box [x y z]
  (Box. x y z))


(defn geo [name mesh]
  (Geometry. name mesh))


(defn material [asset-manager path]
  (Material. asset-manager path))


(defn color-rgba [r g b a]
  (ColorRGBA. r g b a))


(defn register-locator [path locator]
  (doto ^AssetManager (get-manager :asset)
    (.registerLocator path locator)))


(defn create-mesh-shape [spatial]
  (CollisionShapeFactory/createMeshShape spatial))


(defn rigid-body-control [shape mass]
  (RigidBodyControl. shape mass))


(defn attach [app-state]
  (doto ^AppStateManager (get-manager :app-state)
    (.attach app-state)))


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


(defn image-based-hm [img]
  (ImageBasedHeightMap. img))


(defn image [^Texture texture]
  (.getImage texture))


(defn load-hm [^HeightMap hm]
  (doto hm .load))


(defn get-hm [^HeightMap hm]
  (.getHeightMap hm))


(defn terrain-quad [name path-size total-size hm]
  (TerrainQuad. name path-size total-size hm))


(defn terrain-lod-control [^Terrain terrain ^Camera camera]
  (TerrainLodControl. terrain camera))


;;TODO consider this form;
;;TODO (set* (character-control capsule-shape 0.05)
;          :jump-speed 20
;          :fall-speed [30 24]
;          :gravity (vec3 0 -30 0)
;          :physics-location (vec3 0 10 0))
(defmacro set* [obj kw & args]
  `(let [result# (eval ~`(do ~obj))]
     (~(symbol (csk/->camelCase (str ".set-" (name kw)))) result# ~@args)
     result#))


(defmacro get* [obj kw & args]
  `(~(symbol (csk/->camelCase (str ".get-" (name kw)))) ~obj ~@args))


(defmacro call* [obj kw & args]
  `(~(symbol (csk/->camelCase (str "." (name kw)))) ~obj ~@args))


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


(defn simple-app [& {:keys [opts] :as m}]
  (let [app (proxy [SimpleApplication] []
              (simpleInitApp []
                (binding [*app* this]
                  ;;re-init and this block has to be same.
                  (let [init-result ((:init m))]
                    (when (map? init-result)
                      (swap! states assoc ::app init-result)))))
              (simpleUpdate [tpf]
                (when (-> @states :initialized? false? not)
                  (binding [*app* this]
                    (let [update-result ((or (:update m)
                                             (constantly nil)) tpf)]
                      (when (map? update-result)
                        (swap! states update ::app merge update-result)))))))]
    (when (seq opts)
      (some->> opts :show-settings? (.setShowSettings app))
      (some->> opts :pause-on-lost-focus? (.setPauseOnLostFocus app))
      (some->> opts :settings map->app-settings (.setSettings app)))
    app))


(defmacro defsimpleapp
  [name & {:keys [opts init update] :as m}]
  `(defonce ~name (simple-app ~m)))


(defn start [^SimpleApplication app]
  (doto app .start))


;;TODO stop app makes input not working after re-start
;;TODO try to find a way
(defn stop [^SimpleApplication app]
  (clear app)
  (doto app (.stop true)))


(defn re-init [app init-fn]
  (binding [*app* app]
    (swap! states assoc :initialized? false)
    (clear app)
    (let [init-result (init-fn)]
      (if (map? init-result)
        (swap! states assoc
               ::app init-result
               :initialized? true)
        (swap! states #(assoc (dissoc %1 %2) :initialized? true) ::app)))))


(defn unbind-app
  "Should be used for development purposes. (unbind-app #'my-ns/app)
   After calling `unbind-app`, `app` can be re-defined using `defsimpleapp`"
  [^Var v]
  (when (bound? v)
    (stop @v)
    (.unbindRoot v)))


(defn running? [^SimpleApplication app]
  (boolean (some-> app .getContext .isCreated)))


(defmacro run
  [app & body]
  `(binding [*app* ~app]
     ~@body))
