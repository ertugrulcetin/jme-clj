(ns jme-clj.core
  (:require
   [camel-snake-kebab.core :as csk]
   [kezban.core :as k]
   [potemkin :as p])
  (:import
   (clojure.lang Var)
   (com.jme3.animation AnimEventListener AnimControl AnimChannel)
   (com.jme3.app SimpleApplication)
   (com.jme3.app.state AppStateManager)
   (com.jme3.asset AssetManager)
   (com.jme3.audio AudioNode AudioData$DataType)
   (com.jme3.bullet BulletAppState)
   (com.jme3.bullet.collision.shapes CapsuleCollisionShape)
   (com.jme3.bullet.control RigidBodyControl CharacterControl)
   (com.jme3.bullet.util CollisionShapeFactory)
   (com.jme3.collision CollisionResults Collidable)
   (com.jme3.effect ParticleEmitter)
   (com.jme3.font BitmapText)
   (com.jme3.input InputManager FlyByCamera)
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
   (com.jme3.math Vector3f Ray ColorRGBA Vector2f)
   (com.jme3.renderer Camera)
   (com.jme3.scene Geometry Node Spatial Mesh)
   (com.jme3.scene.shape Box Sphere)
   (com.jme3.system AppSettings)
   (com.jme3.terrain Terrain)
   (com.jme3.terrain.geomipmap TerrainQuad TerrainLodControl)
   (com.jme3.terrain.heightmap ImageBasedHeightMap HeightMap)
   (com.jme3.texture Texture)
   (com.jme3.util TangentBinormalGenerator)))

(set! *warn-on-reflection* true)

(defonce ^:private states (atom {}))
;;TODO move it to states
(defonce ^:private listeners (atom []))

(def ^{:dynamic true
       :tag     SimpleApplication}
  *app* nil)


(defn get-state []
  (::app @states))


(defn update-state [ks f & args]
  (let [ks (if (vector? ks) ks [ks])
        ks (into [::app] ks)]
    (apply swap! (into [states update-in ks f] args))))


(defn set-state [ks v]
  (let [ks (if (vector? ks) ks [ks])
        ks (into [::app] ks)]
    (swap! states assoc-in ks v)))


(defn app-settings
  "Creates an AppSettings instance."
  [load-defaults? & {:keys [fullscreen?
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


(defn vec2
  ([]
   (Vector2f.))
  ([x y]
   (vec2 x y false))
  ([x y normalize]
   (if (= normalize :normalize)
     (.normalize (Vector2f. x y))
     (Vector2f. x y))))


(defn detach-all-child [^Node node]
  (doto node .detachAllChildren))


(defn clear
  "Detaches all child nodes and removes all local lights from the root node."
  [^SimpleApplication app]
  (let [root-node (.getRootNode app)]
    (detach-all-child root-node)
    (.clear (.getLocalLightList root-node))
    root-node))


(defn ^AssetManager asset-manager []
  (.getAssetManager *app*))


(defn ^InputManager input-manager []
  (.getInputManager *app*))


(defn ^AppStateManager state-manager []
  (.getStateManager *app*))


(defn ^Node root-node []
  (.getRootNode *app*))


(defn gui-node []
  (.getGuiNode *app*))


(defn audio-node [^String name ^AudioData$DataType type]
  (AudioNode. (asset-manager) name type))


(defn play [^AudioNode an]
  (doto an .play))


(defn play-ins [^AudioNode an]
  (doto an .playInstance))


(defn particle-emitter [name type num-particles]
  (ParticleEmitter. name type num-particles))


(defn emit-all-particles [^ParticleEmitter pe]
  (doto pe .emitAllParticles))


(defn listener []
  (.getListener *app*))


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
  (.loadModel (asset-manager) ^String path))


(defn load-texture [path]
  (.loadTexture (asset-manager) ^String path))


(defn load-font [path]
  (.loadFont (asset-manager) path))


(defn bitmap-text [gui-font right-to-left]
  (BitmapText. gui-font right-to-left))


(defn mult [^Vector3f v ^Float scalar]
  (.mult v scalar))


;;TODO change name to vec3-mult-local or something
;;vec2 has this too
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


(defn material [path]
  (Material. (asset-manager) path))


(defn color-rgba [r g b a]
  (ColorRGBA. r g b a))


(defn register-locator [path locator]
  (doto (asset-manager) (.registerLocator path locator)))


(defn scale-texture-coords [^Mesh mesh scale-factor]
  (doto mesh (.scaleTextureCoordinates scale-factor)))


(defn create-mesh-shape [spatial]
  (CollisionShapeFactory/createMeshShape spatial))


(defn rigid-body-control
  ([^Float mass]
   (RigidBodyControl. mass))
  ([shape mass]
   (RigidBodyControl. shape mass)))


(defn attach [app-state]
  (doto (state-manager) (.attach app-state)))


(defn attach-child [^Node node ^Spatial s]
  (doto node (.attachChild s)))


(defn add-to-root [^Node node]
  (.attachChild (root-node) node)
  node)


(defn remove-from-root [^Node node]
  (.detachChild (root-node) node)
  node)


(defn context []
  (.getContext *app*))


(defn set-display-stat-view [show]
  (doto *app* (.setDisplayStatView show)))


(defn set-display-fps [show]
  (doto *app* (.setDisplayFps show)))


(defn light [type]
  (case type
    :directional (DirectionalLight.)
    :ambient (AmbientLight.)
    :probe (LightProbe.)
    :point (PointLight.)
    :spot (SpotLight.)))


(defn sphere
  ([x y z]
   (sphere x y z false false))
  ([x y z use-even-slices? interior?]
   (Sphere. x y z use-even-slices? interior?)))


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


(defmacro set*
  "Java interop for methods with `set` prefix. Since it returns the given object,
   this fn can be chained with other `set*`s using the thread-first `->` macro.

   e.g.: (set* channel :speed 1.0) -> (.setSpeed channel 1.0)"
  [obj kw & args]
  `(let [result# (eval ~`(do ~obj))]
     (~(symbol (csk/->camelCase (str ".set-" (name kw)))) result# ~@args)
     result#))


(defmacro get*
  "Java interop for methods with `get` prefix.

   e.g.: (get* (cam) :rotation) -> (.getRotation (cam))"
  [obj kw & args]
  `(~(symbol (csk/->camelCase (str ".get-" (name kw)))) ~obj ~@args))


(defmacro call*
  "Java interop for methods. Since we can't wrap all functions of jMonkeyEngine, we can use a shortcut like this.

   e.g.: (call* player :jump (vec3 0 20 0)) -> (.jump player (vec3 0 20 0))"
  [obj kw & args]
  `(~(symbol (csk/->camelCase (str "." (name kw)))) ~obj ~@args))


(defmacro setc
  "Compact version of `set*`
   e.g.:
   (setc debris
         :material mat-debris
         :images-x 3
         :images-y 3
         :rotate-speed 4
         :select-random-image true
         :start-color ColorRGBA/White
         :gravity [0 6 0]
         :low-life 1
         :high-life 3)

    When you need to pass multiple parameters, use a vector.
    e.g.: (setc :local-translation [0.0 -5.0 -2.0])"
  [obj & args]
  (p/unify-gensyms
   `(let [result## (eval ~`(do ~obj))]
      ~@(map (fn [[k# v#]]
               (if (vector? v#)
                 `(set* result## ~k# ~@v#)
                 `(set* result## ~k# ~v#)))
             (partition 2 args)))))


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


(defn- kw->str [kw]
  (if (qualified-keyword? kw)
    (str (namespace kw) "/" (name kw))
    (throw (IllegalArgumentException. (format "%s is not qualified keyword." kw)))))


(defn- create-input-mapping [m]
  (doseq [[k v] m]
    (let [k             (kw->str k)
          input-manager (input-manager)]
      (when (.hasMapping input-manager k)
        (.deleteMapping input-manager k))
      (.addMapping input-manager k (into-array Trigger (if (vector? v) v [v])))
      m)))


(defn- ns-from-q-kw [m-names]
  (if (coll? m-names)
    (-> m-names first namespace)
    (namespace m-names)))


(defn- register-input-mapping [m]
  (let [input-manager (input-manager)
        ns*           (-> m first second ns-from-q-kw)]
    (doseq [listener (get-in @states [:listeners ns*])]
      (.removeListener input-manager listener)
      (swap! states update :listeners dissoc ns*))
    (doseq [[listener m-names] m]
      (.addListener input-manager listener (into-array String (if (vector? m-names)
                                                                (mapv kw->str m-names)
                                                                (vector (kw->str m-names)))))
      (swap! states update-in [:listeners (ns-from-q-kw m-names)] (fnil conj []) listener))
    m))


(defn apply-input-mapping [{:keys [triggers listeners] :as m}]
  (create-input-mapping triggers)
  (register-input-mapping listeners)
  m)


(defn action-listener [f]
  (let [f (bound-fn* f)]
    (proxy [ActionListener] []
      (onAction [name pressed? tpf]
        (f (keyword name) pressed? tpf)))))


(defn analog-listener [f]
  (let [f (bound-fn* f)]
    (proxy [AnalogListener] []
      (onAnalog [name value tpf]
        (f (keyword name) value tpf)))))


(defn anim-listener [on-cycle-done on-anim-change]
  (let [on-cycle-done  (bound-fn* on-cycle-done)
        on-anim-change (bound-fn* on-anim-change)]
    (proxy [AnimEventListener] []
      (onAnimCycleDone [^AnimControl control ^AnimChannel channel ^String name]
        (on-cycle-done control channel name))
      (onAnimChange [^AnimControl control ^AnimChannel channel ^String name]
        (on-anim-change control channel name)))))


;;TODO check here, we might need to remove old ones
;;like in input listeners to avoid duplication!
(defn add-anim-listener [^AnimControl control ^AnimEventListener listener]
  (.addListener control listener))


(defn create-channel [^AnimControl control]
  (.createChannel control))


(defmacro letj
  "Executes the body and returns a hash map with key-val pairs that extracted from bindings. Ignores `_` bindings.

  e.g.:
  (letj [shootables (node \"Shootables\")
         _ (init-keys)
         _ (init-cross-hairs)
         mark (init-mark)]
        (-> shootables
            (add-to-root)
            (attach-child (make-cube \"the Deputy\" 1 0 -4))
            (attach-child (make-floor))
            (attach-child (make-char))))

  => {:mark mark
      :shootables shootables}"
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


(defn look-at
  ([pos world-up-vec]
   (look-at (cam) pos world-up-vec))
  ([^Camera cam pos world-up-vec]
   (doto cam (.lookAt pos world-up-vec))))


(defn collision-results []
  (CollisionResults.))


(defn ray [origin direction]
  (Ray. origin direction))


(defn collide-with [^Collidable o collidable results]
  (.collideWith o collidable results)
  results)


(defn size [^CollisionResults o]
  (.size o))


(defn simple-app
  "Creates a SimpleApplication instance, please have a look at com.jme3.app.SimpleApplication for more."
  [{:keys [opts init] :as m}]
  (let [app (proxy [SimpleApplication] []
              (simpleInitApp []
                (binding [*app* this]
                  ;;re-init and this block has to be same.
                  (let [init-result (init)]
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
      (some->> opts :display-fps? (.setDisplayFps app))
      (some->> opts :display-stat-view? (.setDisplayStatView app))
      (some->> opts :settings map->app-settings (.setSettings app)))
    app))


(defmacro defsimpleapp
  "Creates a SimpleApplication instance and binds with given name. Requires 3 parameters besides name.
   init   (initialize fn, required)
   update (update fn, optional)
   opts   (app settings, optional)

   e.g.:
   (defsimpleapp app
                 :opts {:show-settings?       false
                        :pause-on-lost-focus? false
                        :settings             {:title          \"My JME Game\"
                                               :load-defaults? true
                                               :frame-rate     60}}
                 :init init
                 :update simple-update)

   When init fn returns a hash map, this map registered to the global mutable state so it can be accessed from
   update fn and other fns. Also, this applies for the update fn, it's content merged to the global mutable state.

   For other settings options, please have a look `app-settings` fn.

   It's not recommended to create multiple defsimpleapp instances inside one JVM.
   Some odd behaviours might occur due to shared states. Please run new JVM instance per application.

   If you would like to run another SimpleApplication instance inside the same JVM (same REPL),
   an option could be using `unbind-app` for unbinding current app (var), and re-defining app with `defsimpleapp`."
  [name & {:keys [opts init update] :as m}]
  `(defonce ~name (simple-app ~m)))


(defn running? [^SimpleApplication app]
  (boolean (some-> app .getContext .isCreated)))


(defn start
  "Starts the SimpleApplication instance.

   It's not recommended to call `start` fn after calling `stop` fn. Should be used for development purposes only.
   Some odd behaviours might occur such as JVM crash (based on the app, apps use Bullet API [not always]).

   If you would like to re-start the app then use `unbind-app` instead of `stop`,
   after re-defining app with `defsimpleapp` then call `start` again."
  [^SimpleApplication app]
  (.start app)
  (when (:stopped? @states)
    (binding [*app* app]
      (loop [r? (running? *app*)]
        (when-not r?
          (Thread/sleep 10)
          (recur (running? *app*))))
      (.registerWithInput ^FlyByCamera (fly-cam) (input-manager))))
  app)


(defn stop
  "Stops the SimpleApplication instance. Can be used when the user wants to exit from the game.

   It's not recommended to call `start` fn after calling `stop` fn. Should be used for development purposes only.
   Some odd behaviours might occur such as JVM crash (based on the app, apps use Bullet API [not always]).

   If you would like to re-start the app then use `unbind-app` instead of `stop`,
   after re-defining app with `defsimpleapp` then call `start` again."
  [^SimpleApplication app]
  (clear app)
  (swap! states assoc :stopped? true)
  (try
    (doto app (.stop true))
    (catch Throwable _
      (println "Error occurred on stop."))))


(defn re-init
  "Re-initializes the app with given init fn. It does not stop the app.
   Can be used when new code changes needed for the init fn."
  [init-fn]
  (swap! states assoc :initialized? false)
  (clear *app*)
  (let [init-result (init-fn)]
    (if (map? init-result)
      (swap! states assoc
             ::app init-result
             :initialized? true)
      (swap! states #(assoc (dissoc %1 %2) :initialized? true) ::app))))


(defn unbind-app
  "Unbinds the SimpleApplication instance from the var. Should be used for development purposes only.

   e.g.: (unbind-app #'my-ns/app)

   After calling `unbind-app`, `app` can be re-defined with `defsimpleapp`."
  [^Var v]
  (when (bound? v)
    (stop @v)
    (reset! states {})
    (reset! listeners [])
    (.unbindRoot v)))


(defmacro run
  "Every code that changes the state should be wrapped with `run` macro.
   Otherwise `Make sure you do not modify the scene from another thread!` exception will be thrown."
  [app & body]
  `(binding [*app* ~app]
     (let [^Runnable f# (bound-fn* (fn [] ~@body))]
       (.enqueue *app* f#))))
