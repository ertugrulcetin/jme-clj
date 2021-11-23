(ns jme-clj.core
  "Clojure wrappers round many commonly used JME methods."
  (:require
   [camel-snake-kebab.core :as csk]
   [kezban.core :as k]
   [potemkin :as p])
  (:import
   (clojure.lang Var)
   (com.jme3.animation AnimEventListener AnimControl AnimChannel)
   (com.jme3.app SimpleApplication ResetStatsState StatsAppState FlyCamAppState DebugKeysAppState)
   (com.jme3.app.state AppStateManager BaseAppState AppState)
   (com.jme3.asset AssetManager)
   (com.jme3.audio AudioNode AudioData$DataType AudioListenerState)
   (com.jme3.bullet BulletAppState PhysicsSpace$BroadphaseType)
   (com.jme3.bullet.collision.shapes CapsuleCollisionShape)
   (com.jme3.bullet.control RigidBodyControl CharacterControl BetterCharacterControl)
   (com.jme3.bullet.util CollisionShapeFactory)
   (com.jme3.collision CollisionResults Collidable)
   (com.jme3.effect ParticleEmitter)
   (com.jme3.font BitmapText)
   (com.jme3.input InputManager RawInputListener)
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
   (com.jme3.math Vector3f Ray ColorRGBA Vector2f Quaternion)
   (com.jme3.renderer Camera)
   (com.jme3.scene Geometry Node Spatial Mesh Spatial$CullHint)
   (com.jme3.scene.control AbstractControl)
   (com.jme3.scene.shape Box Sphere)
   (com.jme3.system AppSettings JmeContext JmeContext$Type)
   (com.jme3.terrain Terrain)
   (com.jme3.terrain.geomipmap TerrainQuad TerrainLodControl)
   (com.jme3.terrain.heightmap ImageBasedHeightMap HeightMap HillHeightMap)
   (com.jme3.texture Texture)
   (com.jme3.util TangentBinormalGenerator SkyFactory SkyFactory$EnvMapType)))

(set! *warn-on-reflection* true)

(defonce ^{:doc "The mutable global state of the application.
                 It keeps app, app-state, control states and others."}
  states (atom {}))

(defonce ^{:doc "A list of [`SimpleApplication`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/SimpleApplication.html) instances created with [[defsimpleapp]]."}
  instances (atom []))

(def ^{:doc "The currently active [`SimpleApplication`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/SimpleApplication.html) instance, if any.
             
             **Note that** many functions will fail if [[*app*]] is bound to anything
             other than a `SimpleApplication`."
       :dynamic true
       :tag     SimpleApplication}
  *app*
  "The currently active [`SimpleApplication`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/SimpleApplication.html) instance, if any.
             
  **Note that** many functions will fail if [[*app*]] is not bound, or is bound to anything
  other than a `SimpleApplication`."
  nil)

(def ^{:dynamic true
       :tag     AbstractControl}
  *control*
  "The currently active [`AbstractControl`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/control/AbstractControl.html), if any.
                
  **Note that** many functions will fail if `*control*` is not bound, or is bound to anything
  other than an `AbstractControl`."
  nil)


(defn get-main-state
  "Returns the mutable global state map `[[states]]`."
  []
  @states)


(defn get-state
  "With no arguments, returns the current value of `::app` in `[[states]]`;
   with one argument, `type`, expected to be one of `:app`, `app-state` or
   `:control`, returns the appropriate value from `states`; with two arguments, 
   returns the value identified by the key or sequence of keys `kw` within that part
   of the `states` map identified by `type`.
   
   An exception is thrown if `type` is not one of the expected values."
  ([]
   (get-state :app))
  ([type]
   (case type
     :app (::app @states)
     :app-state (::app-states @states)
     :control (::controls @states)))
  ([type kw]
   (let [kw (if (vector? kw) kw [kw])]
     (get-in (get-state type) kw))))


(defn update-state
  "Prepends `type`, expected to be one of `:app`, `app-state` or `:control`, to
   `ks`, expected to be a key or sequence of keys, as a path through 
   `[[states]]` and updates states by replacing the value identified
   with the result of applying the function `f`  to that value, with any 
   additional `args` supplied.
   
   An exception is thrown if `type` is not one of the expected values."
  [type ks f & args]
  (let [kw (case type
             :app ::app
             :app-state ::app-states
             :control ::controls)
        ks (if (vector? ks) ks [ks])
        ks (into [kw] ks)]
    (apply swap! (into [states update-in ks f] args))))


(defn set-state
  "Treats `ks`, expected to be a key or sequence of keys, as a path
   prefixed by `type` (or `::app` if `type` is not supplied) through 
   [`states`](#var-states) and updates states by setting the value 
   identified by that path to `v`.
   
   An exception is thrown if any sub-path of `ks` already identifies
   something which is not an [associative](https://clojuredocs.org/clojure.core/associative_q)."
  ([ks v]
   (set-state :app ks v))
  ([type ks v]
   (let [kw (case type
              :app ::app
              :app-state ::app-states
              :control ::controls)
         ks (if (vector? ks) ks [ks])
         ks (into [kw] ks)]
     (swap! states assoc-in ks v))))


(defn remove-state
  "Treats `ks`, expected to be a key or sequence of keys, as a path
   prefixed by `type` (or `::app` if `type` is not supplied) through 
   [`states`](#var-states) and updates states by removing the value 
   identified by that path."
  ([ks]
   (remove-state :app ks))
  ([type ks]
   (let [kw (case type
              :app ::app
              :app-state ::app-states
              :control ::controls)
         ks (if (vector? ks) ks [ks])]
     (swap! states update kw k/dissoc-in ks))))


(defn app-settings
  "Creates an [AppSettings](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/system/AppSettings.html) instance."
  [load-defaults? & {:keys [dialog-image
                            frame-rate
                            frequency
                            fullscreen?
                            height
                            resizable?
                            title
                            vsync?
                            width]}]
  (let [settings (AppSettings. (boolean load-defaults?))]
    (some->> dialog-image (.setSettingsDialogImage settings))
    (some->> frame-rate (.setFrameRate settings))
    (some->> frequency (.setFrequency settings))
    (some->> fullscreen? (.setFullscreen settings))
    (some->> height (.setHeight settings))
    (some->> resizable? (.setResizable settings))
    (some->> vsync? (.setVSync settings))
    (some->> title (.setTitle settings))
    (some->> width (.setWidth settings))
    settings))


(defprotocol Matrix
  (mult [this scalar])
  (mult-loc [this scalar])
  (add [this v] [this x y] [this x y z])
  (add-loc [this v] [this x y z])
  (setv [this v] [this x y] [this x y z])
  (negate [this]))


(extend-protocol Matrix
  Vector3f
  (mult [this ^Float scalar]
    (.mult this scalar))
  (mult-loc [this ^Float scalar]
    (.multLocal this scalar))
  (add
    ([this v]
     (.add this v))
    ([this x y z]
     (.add this x y z)))
  (add-loc
    ([this v]
     (.addLocal this v))
    ([this x y z]
     (.addLocal this x y z)))
  (setv
    ([this v]
     (.set this v))
    ([this x y z]
     (.set this x y z)))
  (negate [this]
    (.negate this))

  Vector2f
  (mult [this ^Float scalar]
    (.mult this scalar))
  (mult-loc [this ^Float scalar]
    (.multLocal this scalar))
  (add
    ([this v]
     (.add this v))
    ([this x y]
     (.add this x y)))
  (add-loc
    ([this v]
     (.addLocal this v))
    ([this x y]
     (.addLocal this x y)))
  (setv
    ([this v]
     (.set this v))
    ([this x y]
     (.set this x y)))
  (negate [this]
    (.negate this)))


(defn vec3
  "With zero arguments, returns a three dimensional vector <0.0, 0.0, 0.0>;
   with one argument, expected to be a [Vector3f](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/Vector3f.html) object, returns
   a copy of that vector; with three arguments, expected all to be 
   numbers, returns a three dimensional vector of those numbers; with three
   arguments, where the first three are numbers, returns a three dimensional
   vector of those numbers, which will be [normalised](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/Vector2f.html#normalize--) 
   to the unit vector if and only if the fourth argument is the keyword
   `:normalize`.
   
   **Exceptions will be thrown** if
   1. a single argument is not a Vector2f object;
   2. any numeric argument is too large to be converted to a Java float.
   
   **Note also** that any fourth argument which is not the keyword `:normalize` 
   will be ignored."
  ([]
   (Vector3f.))
  ([v]
   (Vector3f. v))
  ([x y z]
   (vec3 x y z false))
  ([x y z normalize]
   (if (= normalize :normalize)
     (.normalize (Vector3f. x y z))
     (Vector3f. x y z))))


(defn vec2
  "With zero arguments, returns a two dimensional vector <0.0, 0.0>;
   with one argument, expected to be a [Vector2f](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/Vector2f.html) object, returns
   a copy of that vector; with two arguments, expected both to be 
   numbers, returns a two dimensional vector of those numbers; with three
   arguments, where the first two of numbers, returns a two dimensional
   vector of those numbers, which will be [normalised](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/Vector2f.html#normalize--) 
   to the unit vector if and only if the third argument is the keyword
   `:normalize`.
   
   **Exceptions will be thrown** if
   1. a single argument is not a Vector2f object;
   2. any numeric argument is too large to be converted to a Java float.
   
   **Note also** that any third argument which is not the keyword `:normalize` 
   will be ignored."
  ([]
   (Vector2f.))
  ([v]
   (Vector2f. v))
  ([x y]
   (vec2 x y false))
  ([x y normalize]
   (if (= normalize :normalize)
     (.normalize (Vector2f. x y))
     (Vector2f. x y))))


(defn quat
  "Returns a [Quaternion](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/Quaternion.html) object. If no args are passed, the object will
   have the values <0, 0, 0, 1>; if four arguments `x`, `y`, `z`, `w` are 
   passed, such that all are numbers which can be cast to floats, the object 
   will have those values.

   If three arguments are passed, either the first two should be `Quaternions` 
   and the third a number castable to float, or all three should be numbers 
   castable to floats; otherwise, `nil` will be returned."
  ([]
   (Quaternion.))
  ([x y z]
   (cond (and (instance? Quaternion x) (instance? Quaternion y) (number? z)) (Quaternion. x y z)
         (reduce #(and %1 %2) (map number? [x y z])) (Quaternion. x y z 1.0)))
  ([^Float x ^Float y ^Float z ^Float w]
   (Quaternion. x y z w)))


(defn vec3->vec
  "Given a [Vector3f](https://javadoc.jmonkeyengine.org/v3.x/com/jme3/math/Vector3f.html)
   object `v`, return a Clojure vector having the components of `v`."
  [^Vector3f v]
  [(.-x v) (.-y v) (.-z v)])


(defn vec->vec3
  "Given a Clojure sequence `v` of three numbers, returns a corresponding 
   [Vector3f](https://javadoc.jmonkeyengine.org/v3.x/com/jme3/math/Vector3f.html) object. 
   Any fourth element in `v` will be ignored unless it is the keyword `normalize`, in
   which case the vector returned will be normalised to the unit vector.
   
   **An exception will be thrown** if 
   1. there are fewer than three elements in `v`;
   2. the first three elements of `v` are not numbers castable to Java float;
   3. there are more than four elements in `v`."
  [v]
  (apply vec3 v))


(defn quat->vec
  "Given a [Quaternion](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/Quaternion.html)
   object `q`, return a Clojure vector of four elements being the components of
   `q`.
   
   **An exception may be thrown** if `q` is not a Quaternion."
  [^Quaternion q]
  [(.getX q) (.getY q) (.getZ q) (.getW q)])


(defn vec->quat
  "Given a Clojure sequence `v` of four numbers, returns a corresponding 
   [Quaternion](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/Quaternion.html)
   object. Essentially, if the sequence `v` matches any of the signatures of a
   Quaternion constructor, this will work; but four numbers is the expected case.
   
   **An exception may be thrown** if the elements of `v` are not four numbers each
   of which can be cast to Java float."
  [v]
  (apply quat v))

(defn detach-all-child
  "Removes all children attached to this `node`.
   
   **An exception may be thrown** if `node` is not an instance of 
   [`Node`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Node.html)"
  [^Node node]
  (doto node .detachAllChildren))


(defn ^AssetManager asset-manager
  "Return the [AssetManager](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/asset/AssetManager.html) of the current [[*app*]], if any."
  []
  (.getAssetManager *app*))


(defn ^InputManager input-manager
  "Return the [InputManager](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/input/InputManager.html) of the current [[*app*]], if any."
  []
  (.getInputManager *app*))


(defn ^AppStateManager state-manager
  "Return the [AppStateManager](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/state/AppStateManager.html) of the current [[*app*]], if any."
  []
  (.getStateManager *app*))


(defn ^Node root-node
  "Return the root [Node](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Node.html) of the current [[*app*]], if any."
  []
  (.getRootNode *app*))


(defn gui-node
  "Return the GUI [Node](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Node.html) of the current [[*app*]], if any."
  []
  (.getGuiNode *app*))


(defn audio-node
  "Creates and returns an instance of [`AudioNode`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/audio/AudioNode.html) 
   with this `name` and this `type`.
   
   Possible `type` options:
  
  * `:buffer`
  * `:stream`"
  [^String name type]
  (let [^AudioData$DataType type (case type
                                   :buffer AudioData$DataType/Buffer
                                   :stream AudioData$DataType/Stream)]
    (AudioNode. (asset-manager) name type)))


(defn play
  "Causes the [`AudioNode`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/audio/AudioNode.html) `an`
   to start playing its audio track, if any."
  [^AudioNode an]
  (doto an .play))


(defn play-ins
  "Causes the [`AudioNode`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/audio/AudioNode.html) `an`
   to start playing an instance of its audio track, if any."
  [^AudioNode an]
  (doto an .playInstance))


(defn particle-emitter
  "Creates and returns an instance of [`ParticleEmitter`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/effect/ParticleEmitter.html),
   with this `name`, `type` and `num_particles`.
   
   `type` is expected to be an instance of com.jme3.effect.ParticleMesh$Type, 
   or else one of the keywords `:triangle` or `:point`."
  [name type num-particles]
  (let [ptype (if (instance? type com.jme3.effect.ParticleMesh$Type)
                type
                (case type
                  :triangle com.jme3.effect.ParticleMesh$Type/Triangle
                  :point com.jme3.effect.ParticleMesh$Type/Point))]
    (ParticleEmitter. name ptype num-particles)))


(defn emit-all-particles
  "Instantly causes `pe`, an instance of [`ParticleEmitter`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/effect/ParticleEmitter.html),
   to emit all the particles possible to be emitted. Any particles which are currently inactive will be spawned immediately."
  [^ParticleEmitter pe]
  (doto pe .emitAllParticles))


(defn listener
  "Returns the audio [`Listener`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/audio/Listener.html)
   instance associated with the current application, if any."
  []
  (.getListener *app*))


(defn view-port
  "Returns the [`ViewPort`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/renderer/ViewPort.html)
   instance associated with the current application, if any."
  []
  (.getViewPort *app*))


(defn bullet-app-state
  "Creates and returns a new [`BulletAppState`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/bullet/BulletAppState.html) instance.
   This is an app state to manage a single Bullet physics space.
   
   With zero args, returns a bullet space which searches for collisions using 
   Dynamic Bounding Volume Trees, which may be inefficient.
   
   With two args, `world-min` and `world-max`, both of which must be instances of
   [`Vector3f`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/Vector3f.html)
   (e.g. created with [[vec3]]), creates an explicitly bounded space to search for collisions.
   
   With three args, where the first two are instances of `Vector3f`, the third should be a keyword
   with one of the following values:
   
   * `:axis-sweep-3` improved broadphase as in the two-arg signature, tracks up to 16384 objects;
   * `:axis-sweep-3-32` improved broadphase, tracks up to 65536 objects;
   * `:dbvt` broadphase as in the zero-args signature, allowing quicker adding/removing of physics objects;
   * `:simple` naive and probably inefficient broadphase search."
  ([]
   (BulletAppState.))
  ([^Vector3f world-min ^Vector3f world-max]
   (BulletAppState. world-min world-max))
  ([^Vector3f world-min ^Vector3f world-max ^clojure.lang.Keyword broadphase-type]
   (BulletAppState. world-min world-max (case broadphase-type
                                          :axis-sweep-3 PhysicsSpace$BroadphaseType/AXIS_SWEEP_3
                                          :axis-sweep-3-32 PhysicsSpace$BroadphaseType/AXIS_SWEEP_3_32
                                          :dbvt PhysicsSpace$BroadphaseType/DBVT
                                          :simple PhysicsSpace$BroadphaseType/SIMPLE))))


(defn capsule-collision-shape
  "Creates and returns a new [`CapsuleCollisionShape`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/bullet/collision/shapes/CapsuleCollisionShape.html) object."
  ([radius height]
   (CapsuleCollisionShape. radius height))
  ([radius height axi]
   (CapsuleCollisionShape. radius height axi)))


(defn add-control
  "Adds the specified `control` to the specified `spatial`.
   
   **An exception will be thrown** if
   
   1. `control` is not an instance of [`Control`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/control/Control.html);
   2. `spatial` is not an instance of [`Spatial`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Spatial.html)"
  [^Spatial spatial control]
  (doto spatial (.addControl control)))


(defn get-control
  "Get from this `spatial` its control of class `c`, if any.
   
   **An exception will be thrown** if

   1. `spatial` is not an instance of [`Spatial`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Spatial.html);
   2. `c` is not a class conforming to [`Control`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/control/Control.html)"
  [^Spatial spatial ^Class c]
  (.getControl spatial c))


(defn character-control
  "Creates and returns a new [`CharacterControl`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/bullet/control/CharacterControl.html) object. 
   
   **Note that** [[better-character-control]] may be preferable."
  [shape step-height]
  (CharacterControl. shape step-height))


(defn better-character-control
  "Creates and returns a new [`BetterCharacterControl`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/bullet/control/BetterCharacterControl.html) object."
  [radius height mass]
  (BetterCharacterControl. radius height mass))


(defn load-model
  "Loads, instantiates and returns the model at the specified `path` from the [`AssetManager`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/asset/AssetManager.html) of the current [[*app*]].
   
   Models can be jME3 object files (`J3O`), OgreXML (`mesh.xml`), `BLEND`, `FBX` or `OBJ` files."
  [path]
  (.loadModel (asset-manager) ^String path))


(defn load-texture
  "Loads, instantiates and returns the texture at the specified `path` from the 
   [`AssetManager`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/asset/AssetManager.html) of the current [[*app*]].
   
   supported types are `BMP`, `JPG`, `PNG`, `GIF`, `TGA`, `DDS`, `PFM`, and `HDR`. The texture 
   will be loaded with mip-mapping enabled."
  [path]
  (.loadTexture (asset-manager) ^String path))


(defn load-font
  "Loads, instantiates and returns the [`BitmapFont`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/font/BitmapFont.html) at the specified `path` from the 
   [`AssetManager`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/asset/AssetManager.html) 
   of the current [[*app*]].

   Font files are in AngelCode text format, and are with the extension `fnt`."
  [path]
  (.loadFont (asset-manager) path))


(defn load-asset
  "Loads, instantiates and returns the asset at the specified `path` from the 
   [`AssetManager`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/asset/AssetManager.html) 
   of the current [[*app*]].

   Assets of any type will be loaded. If the specified asset is not found, returns `nil`."
  [path]
  (.loadAsset (asset-manager) ^String path))


(defn bitmap-text
  "Creates and returns a new, empty, [`BitmapText`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/font/BitmapText.html) object.

   The object created has no actual text; to assign it text, call its `.setText` method.

   With zero args, create the text in the default font. 
   With one arg, `gui-font`, expected to be an instance of 
   [`BitmapFont`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/font/BitmapFont.html),
   create the text in that font.
   With two arge, `gui-font` and `right-to-left`, where `gui-font` is expected to be a 
   `BitmapFont`, create tha text in
   that font and, if `right-to-left` is truthy, written from right to left."
  ([]
   (bitmap-text (load-font "Interface/Fonts/Default.fnt") false))
  ([gui-font]
   (BitmapText. gui-font false))
  ([gui-font right-to-left]
   (BitmapText. gui-font right-to-left)))


(defn box
  "Create and return a new [`Box`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/shape/Box.html) 
   (cuboid) with these dimensions, expected to be numbers castable to float."
  [x y z]
  (Box. x y z))


(defn geo
  "Create and return a new [`Geometry`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Geometry.html) 
   object with this `name` based on this `mesh`. `name` is expected to be 
   a `String`; `mesh` is expected to be an instance of
   [`Mesh`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Mesh.html)."
  [name mesh]
  (Geometry. name mesh))


(defn material
  "Loads, instantiates and returns the [`Material`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/material/Material.html) at the specified `path` from the 
   [`AssetManager`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/asset/AssetManager.html) 
   of the current [[*app*]].

   Material files conform to [the J3MD format](https://wiki.jmonkeyengine.org/docs/3.4/core/material/material_specification.html)
   and have the extension `J3MD`."
  [path]
  (Material. (asset-manager) path))


(defn unshaded-mat
  "Create and return an instance of the unshaded material."
  []
  (material "Common/MatDefs/Misc/Unshaded.j3md"))


(defn cull-hint
  "Set the cull hint for the [`Spatial`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Spatial.html) `s` to the specified `type`.

   Possible `type` options -> :always, :dynamic, :inherit and :never"
  [^Spatial s type]
  (doto s (.setCullHint (case type
                          :always Spatial$CullHint/Always
                          :dynamic Spatial$CullHint/Dynamic
                          :inherit Spatial$CullHint/Inherit
                          :never Spatial$CullHint/Never))))


(defn color-rgba
  "Create and return a new instance of [`ColorRGBA`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/ColorRGBA.html) with the specified values."
  [red green blue alpha]
  (ColorRGBA. red green blue alpha))


(defn register-locator
  "Registers the given `locator` class, locating assets from `path`, with the 
   [`AssetManager`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/asset/AssetManager.html)
   associated with the current value of [[*app*]]. 
   [`AssetLocators`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/asset/AssetLocator.html) 
   are invoked in the order they were registered, to locate the asset by the AssetKey. 
   Once an AssetLocator returns a non-null AssetInfo, it is sent to the AssetLoader to load the asset.
   
   **An exception will be thrown** if
   
   1. `locator` is not an instance of `AssetLocator`;
   2. `path` is not a valid path.
   
   See [`registerLocator`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/asset/AssetManager.html#registerLocator-java.lang.String-java.lang.Class-)"
  [path locator]
  (doto (asset-manager) (.registerLocator path locator)))


(defn scale-texture-coords
  "Scales the texture coordinate buffer on this `mesh` by the given `scale-factor`.

   Note that values above 1 will cause the texture to tile, while values 
   below 1 will cause the texture to stretch. 
   
   **An exception will be thrown** if
   
   1. `mesh` is not an instance of [`Mesh`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Mesh.html);
   2. there's no texture coordinate buffer on the mesh;
   3. the texture coordinate buffer is not in 2D float format."
  [^Mesh mesh scale-factor]
  (doto mesh (.scaleTextureCoordinates scale-factor)))


(defn create-mesh-shape
  "Create a mesh shape for the given `spatial`, expected to be an instance of 
   [`Spatial`]().

   This type of collision shape is mesh-accurate and meant for immovable 'world objects'. 
   Examples include terrain, houses or whole shooter levels.

   Objects with 'mesh' type collision shape will not collide with each other.

   Creates a [`HeightfieldCollisionShape`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/bullet/collision/shapes/HeightfieldCollisionShape.html)
   if the supplied spatial is a [`TerrainQuad`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/terrain/geomipmap/TerrainQuad.html)."
  [spatial]
  (CollisionShapeFactory/createMeshShape spatial))


(defn create-sky
  "Creates and returns a [`Spatial`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Spatial.html)
   object representing a sky. The argument `path` should be the path name 
   of the texture asset to use. Possible values for `type` are `:cube`, 
   `:sphere` and `:equirect`"
  [path type]
  (SkyFactory/createSky (asset-manager)
                        ^String path
                        ^SkyFactory$EnvMapType (case type
                                                 :cube SkyFactory$EnvMapType/CubeMap
                                                 :sphere SkyFactory$EnvMapType/SphereMap
                                                 :equirect SkyFactory$EnvMapType/EquirectMap)))


(defn rigid-body-control
  "Create and return a new instance of [`RigidBodyControl`](),
   controlling a body with this `mass` (a positive number castable to float),
   and optionally this `shape`, an instance of
[`CollisionShape`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/bullet/collision/shapes/CollisionShape.html)."
  ([^Float mass]
   (RigidBodyControl. mass))
  ([shape mass]
   (RigidBodyControl. shape mass)))


(defn attach
  "Attach this `app-state`, expected to be an instance of [`AppState`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/state/AppState.html),
   to the [`AppStateManager`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/state/AppStateManager.html)
   associated with the current [[*app*]], if any."
  [app-state]
  (.attach (state-manager) app-state)
  app-state)


(defn attach-all
  "Attach all these `app-states`, expected to be an instances of [`AppState`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/state/AppState.html),
   to the [`AppStateManager`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/state/AppStateManager.html)
   associated with the current [[*app*]], if any."
  [& app-states]
  (doseq [a app-states]
    (attach a))
  app-states)


(defn detach
  "Detach this `app-state`, expected to be an instance of [`AppState`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/state/AppState.html),
  from the [`AppStateManager`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/state/AppStateManager.html)
   associated with the current [[*app*]], if any."
  [app-state]
  (.detach (state-manager) app-state)
  app-state)


(defn attach-child
  "Attach this `spatial`, an instance of [`Spatial`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Spatial.html),
   as a child of this `node`, an instance of [`Node`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Node.html).

   **Note that** Node is a subclass of Spatial, so all Nodes are Spatials but not vice versa."
  [^Node node ^Spatial spatial]
  (doto node (.attachChild spatial)))


(defn detach-child
  "Detach this `spatial`, an instance of [`Spatial`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Spatial.html), 
   from among the children of this `node` (an instance of [`Node`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Node.html)).

   **Note that** Node is a subclass of Spatial, so all Nodes are Spatials but not vice versa."
  [^Node node ^Spatial spatial]
  (doto node (.detachChild spatial)))


(defn add-to-root
  "Attach this `node`, an instance of [`Node`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Node.html), 
   as a child of the root node of the current binding of [[*app*]]."
  [^Node node]
  (.attachChild (root-node) node)
  node)


(defn remove-from-root
  "Detach this `node`, an instance of [`Node`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Node.html),
   from among the children of the root node of the current binding of [[*app*]]."
  [^Node node]
  (.detachChild (root-node) node)
  node)


(defn remove-from-parent
  "Detach this `node`, an instance of [`Node`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Node.html),
   from among the children of its immediate parent."
  [^Node node]
  (doto node .removeFromParent))


(defn ^JmeContext context
  "Returns the [`JmeContext`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/system/JmeContext.html) 
   associated with the current value of [[*app*]], if any."
  []
  (.getContext *app*))


(defn set-display-stat-view
  ;; TODO: no upstream documentation either. Need to work out what this does!
  [show]
  (doto *app* (.setDisplayStatView show)))


(defn set-display-fps
   ;; TODO: no upstream documentation either. Need to work out what this does!
  [show]
  (doto *app* (.setDisplayFps show)))


(defn light
  "Creates a new instance of a subclass of 
   [`Light`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/light/Light.html).
   
   Supported values of `type` are:
   
   * `:ambient` returns an instance of [`AmbientLight`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/light/AmbientLight.html);
   * `:directional` returns an instance of [`DirectionalLight`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/light/DirectionalLight.html);
   * `:point` returns an instance of [`PointLight`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/light/PointLight.html);
   * `:probe` returns an instance of [`LightProbe`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/light/LightProbe.html);
   * `:spot` returns an instance of [`SpotLight`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/light/SpotLight.html)."
  [type]
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


(defn node
  "Creates and returns a new instance of 
   [`Node`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Node.html) 
   with the specified `name`."
  [name]
  (Node. name))


(defn rotate
  "Rotate this `spatial` by the angles `x`, `y`, `z` in their respective axes; angles
   specified in radians."
  [^Spatial spatial x y z]
  (.rotate spatial x y z))


(defn scale
  "Scale this `spatial` by the fixed amount `s`, or by differential amounts  `x`, `y`,
   `z` in their respective axes."
  ([^Spatial spatial s]
   (.scale spatial s))
  ([^Spatial spatial x y z]
   (.scale spatial x y z)))


(defn image-based-height-map
  "Create and return a new instance of [`ImageBasedHeightMap`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/terrain/heightmap/ImageBasedHeightMap.html)
   based on this `img`, expected to be an instance of [`Image`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/texture/Image.html)."
  [img]
  (ImageBasedHeightMap. img))


(defn image
  "Return the [`Image`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/texture/Image.html) 
   associated with this `texture`."
  [^Texture texture]
  (.getImage texture))


(defn load-height-map
  "Populate [`HeightMap`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/terrain/heightmap/HeightMap.html) `hm` with actual heightmap data from its source."
  [^HeightMap hm]
  (doto hm .load))


(defn get-height-map
  "Return an array of real numbers representing the heights in this [`HeightMap`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/terrain/heightmap/HeightMap.html) `hm`"
  [^HeightMap hm]
  (.getHeightMap hm))


(defn hill-height-map
  "Create and return a new instance of [`HillHeightMap`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/terrain/heightmap/HillHeightMap.html) 
   - essentially, a heightmap of random hilly terrain."
  [size iterations min-radius max-radius seed]
  (HillHeightMap. size iterations min-radius max-radius seed))


(defn terrain-quad
  "Create and return a new instance of [`TerrainQuad`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/terrain/geomipmap/TerrainQuad.html)"
  [name path-size total-size hm]
  (TerrainQuad. name path-size total-size hm))


(defn terrain-lod-control
  "Creates and returns a new instance of [`TerrainLodControl`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/terrain/geomipmap/TerrainLodControl.html), 
   controlling the level of detail of this `terrain` as viewed by this `camera`.
   
   `terrain` is expected to be an instance of [`Terrain`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/terrain/Terrain.html); 
   `camera` is expected to be an instance of [`Camera`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/renderer/Camera.html)."
  [^Terrain terrain ^Camera camera]
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


(defn add-light
  "Add this `light`, expected to be an instance of [`Light`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/light/Light.html)
  to this `spatial`, expected to be an instance of [`Spatial`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Spatial.html)"
  [^Spatial spatial light]
  (doto spatial (.addLight light)))


(defn remove-light
  "Remove this `light`, expected to be an instance of [`Light`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/light/Light.html)
  from this `spatial`, expected to be an instance of [`Spatial`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/Spatial.html)"
  [^Spatial spatial light]
  (doto spatial (.removeLight light)))


(defn add-light-to-root
  "Add this `light`, expected to be an instance of [`Light`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/light/Light.html)
  to the root node of the current value of [[*app*]]."
  [light]
  (add-light (root-node) light))


(defn key-trigger
  "Create and return a new instance of [`KeyTrigger`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/input/controls/KeyTrigger.html)
   responding to this key `code`, which should be a number castable to integer, 
   although **note that** there are suitable values defined as static variables in
   [`KeyInput`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/input/KeyInput.html)."
  [code]
  (KeyTrigger. code))


(defn mouse-trigger
  "Create and return a new instance of [`MouseButtonTrigger`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/input/controls/MouseButtonTrigger.html), 
   responding to this mouse button `code`, which should be a number castable to integer, 
   although **note that** there are suitable values defined as static variables in
   [`MouseInput`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/input/MouseInput.html)."
  [code]
  (MouseButtonTrigger. code))


(defn mouse-ax-trigger
  "Create and return a new instance of [`MouseAxisTrigger`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/input/controls/MouseAxisTrigger.html), 
   responding to this this mouse axis `code`, which should be a number castable to integer, 
   although **note that** there are suitable values defined as static variables in
   [`MouseInput`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/input/MouseInput.html).
   
   If `negative?` is truthy, the trigger will respond to negative events on this axis; 
   otherwise, it will respond to positive events."
  [code negative?]
  (MouseAxisTrigger. code negative?))


(defn- raise
  ([msg]
   (raise msg {}))
  ([msg map]
   (raise msg map nil))
  ([msg map cause]
   (throw (ex-info msg map cause))))


(defn- check-qualified-keyword [kw]
  (when-not (qualified-keyword? kw)
    (raise (format "%s is not qualified keyword." kw))))


(defn- kw->str [kw]
  (check-qualified-keyword kw)
  (str (namespace kw) "/" (name kw)))


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


(defn apply-input-mapping
  "Takes a map, `m`, with two keys, `:triggers`, `:listeners` such that:
   
   1. the value of `:triggers` is a map which maps keywords onto triggers;
   2. the value of `:listeners` is a map which maps listeners onto
   keywords or sequences of keywords for which triggers have been registered.
   
   Using qualified keywords (e.g. '::foo') for inputs is highly recommended!
   
   Example:
   ```
   (apply-input-mapping
   ;;
   {:triggers  {::pause  (key-trigger KeyInput/KEY_P)
                ::left   (key-trigger KeyInput/KEY_J)
                ::right  (key-trigger KeyInput/KEY_K)
                ::rotate [(key-trigger KeyInput/KEY_SPACE)
                          (mouse-trigger MouseInput/BUTTON_LEFT)]}
    :listeners {(on-action-listener) ::pause
                (on-analog-listener) [::left ::right ::rotate]}})
   ```
   
   See [tutorial code](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_input_system.clj)."
  [{:keys [triggers listeners] :as m}]
  (create-input-mapping triggers)
  (register-input-mapping listeners)
  m)


(defn action-listener
  [f]
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


(defn- wrap-with-bound [f]
  (if f (bound-fn* f) (constantly nil)))


(defn raw-input-listener [& {:keys [begin-input
                                    end-input
                                    on-joy-axis-event
                                    on-joy-button-event
                                    on-mouse-motion-event
                                    on-mouse-button-event
                                    on-key-event
                                    on-touch-event]}]
  (let [begin-input           (wrap-with-bound begin-input)
        end-input             (wrap-with-bound end-input)
        on-joy-axis-event     (wrap-with-bound on-joy-axis-event)
        on-joy-button-event   (wrap-with-bound on-joy-button-event)
        on-mouse-motion-event (wrap-with-bound on-mouse-motion-event)
        on-mouse-button-event (wrap-with-bound on-mouse-button-event)
        on-key-event          (wrap-with-bound on-key-event)
        on-touch-event        (wrap-with-bound on-touch-event)]
    (reify RawInputListener
      (beginInput [this]
        (begin-input))
      (endInput [this]
        (end-input))
      (onJoyAxisEvent [this evt]
        (on-joy-axis-event evt))
      (onJoyButtonEvent [this evt]
        (on-joy-button-event evt))
      (onMouseMotionEvent [this evt]
        (on-mouse-motion-event evt))
      (onMouseButtonEvent [this evt]
        (on-mouse-button-event evt))
      (onKeyEvent [this evt]
        (on-key-event evt))
      (onTouchEvent [this evt]
        (on-touch-event evt)))))


;;TODO check here, we might need to remove old ones
;;like in input listeners to avoid duplication
;;UPDATE: so far so good, no problem occurred, let's keep this comment for a while
(defn add-anim-listener [^AnimControl control ^AnimEventListener listener]
  (.addListener control listener))


(defn create-channel
  "Creates and returns a new instance of [`AnimChannel`]() on the [`AnimControl`]() `control`.
   
   **Note that** `AnimChannel` and `AnimControl` are deprecated."
  [^AnimControl control]
  (.createChannel control))


(defmacro letj
  "Executes the body and returns a hash map with key-val pairs that extracted from bindings. Ignores `_` bindings.

  e.g.:
  ```
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
      :shootables shootables}
  ```"
  [bindings & body]
  (k/assert-all
   (vector? bindings) "a vector for its binding"
   (even? (count bindings)) "an even number of forms in binding vector")
  `(let* ~(destructure bindings)
         ~@body
         (merge ~@(remove :_ (map #(hash-map (keyword %) %) (take-nth 2 bindings))))))


(defn ^Camera cam
  "Returns the main camera of the current value of [[*app*]], if any."
  []
  (.getCamera *app*))


(defn fly-cam
  "Returns the 'flyCam' of the current value of [[*app*]], if any."
  []
  (.getFlyByCamera *app*))


(defn look-at
  "Direct the [`Camera`]() that is passed as `cam`, or the main camera of the current 
   value of [[*app*]] if `cam` is not passed, to look at the 
   [`Vector3f`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/Vector3f.html) `pos`, 
   using the `Vector3f` `world-up-vector` to orient the camera."
  ([pos world-up-vec]
   (look-at (cam) pos world-up-vec))
  ([^Camera cam pos world-up-vec]
   (doto cam (.lookAt pos world-up-vec))))


(defn collision-results
  "Create and return a new instance of [`CollisionResults`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/collision/CollisionResults.html)."
  []
  (CollisionResults.))


(defn ray
  "Create and return a new instance of [`Ray`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/Ray.html)
   - a line segment which has a [`Vector3f`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/math/Vector3f.html) 
   `origin` and a `Vector3f` `direction`. That is, a point and an infinite ray is cast from this point"
  [origin direction]
  (Ray. origin direction))


(defn ^CollisionResults collide-with
  "Check for a collision between [`Collidable`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/collision/Collidable.html)`s` `o` and `collidable`, and,
   if detected, add it to these [`CollisionResults`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/collision/CollisionResults.html) `results`."
  ;; TODO: I'd suggest renaming the arg `collidable` to `other`; and then, possibly, `o` to `collidable`.
  [^Collidable o collidable results]
  (.collideWith o collidable results)
  results)


(defn size
  "Returns the number of collisions represented by this [`CollisionResults`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/collision/CollisionResults.html) `o`."
  [^CollisionResults o]
  (.size o))


(defn create-ray-test [collidable]
  (let [ray     (ray (.getLocation (cam)) (.getDirection (cam)))
        results (collide-with collidable ray (collision-results))]
    (when (> (size results) 0)
      (let [closest (.getClosestCollision results)]
        {:contact-point (.getContactPoint closest)
         :distance      (.getDistance closest)
         :geometry      (.getGeometry closest)}))))


(defmacro ^{:ignore-long-fn? true} defsimpleapp
  "Creates a SimpleApplication instance and binds with given name. Requires 3 parameters besides name.
   
   * `init`   (initialize fn, required)
   * `update` (update fn, optional)
   * `opts`   (app settings, optional)

   e.g.:
   ```
   (defsimpleapp app
                 :opts {:show-settings?       false
                        :pause-on-lost-focus? false
                        :settings             {:title          \"My JME Game\"
                                               :load-defaults? true
                                               :frame-rate     60}}
                 :init init
                 :update simple-update)
   ```

   When init fn returns a hash map, this map registered to the mutable global state so it can be accessed from
   update fn and other fns. Also, this applies for the update fn, it's content merged to the mutable global state.

   There is also `:destroy` callback, so you might want to release some resources when the app is shutting down.

   For other settings options, please have a look `app-settings` fn.

   It's not recommended to create multiple defsimpleapp instances inside one JVM.
   Some odd behaviours might occur due to shared states. Please run new JVM instance per application.

   If you would like to run another [SimpleApplication](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/SimpleApplication.html) instance inside the same JVM (same REPL),
   an option could be using `unbind-app` for unbinding current app (var), and re-defining app with `defsimpleapp`.

   Please have a look at [com.jme3.app.SimpleApplication](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/SimpleApplication.html) for more."
  [name & {:keys [opts init update] :as m}]
  `(when-let [r# (defonce ~name
                   (let [app# (proxy [SimpleApplication] []
                                (simpleInitApp []
                                  (binding [*app* ~'this]
                                           ;;re-init and this block has to be same.
                                    (let [init-result# (~init)]
                                      (when (map? init-result#)
                                        (swap! states assoc ::app init-result#)))))
                                (simpleUpdate [tpf#]
                                  (when (-> @states :initialized? false? not)
                                    (binding [*app* ~'this]
                                      (let [update-result# ((or ~update
                                                                (constantly nil)) tpf#)]
                                        (when (map? update-result#)
                                          (swap! states clojure.core/update ::app merge update-result#))))))
                                (destroy []
                                  (when-let [destroy# (:destroy ~m)]
                                    (destroy#))
                                  (proxy-super destroy)))]
                     (when (seq ~opts)
                       (some->> ~opts :show-settings? (.setShowSettings app#))
                       (some->> ~opts :pause-on-lost-focus? (.setPauseOnLostFocus app#))
                       (some->> ~opts :display-fps? (.setDisplayFps app#))
                       (some->> ~opts :display-stat-view? (.setDisplayStatView app#))
                       (some->> ~opts :settings map->app-settings (.setSettings app#)))
                     app#))]
     (swap! instances (fnil conj []) r#)
     r#))


(defn- apply-fn-in-app [type app kw f & args]
  (binding [*app* app]
    (let [result (apply f args)
          type   (case type
                   :app-state ::app-states
                   :control ::controls)]
      (when (map? result)
        (swap! states update-in [type kw] merge result)))))


(defn app-state
  "AppState represents continuously executing code inside the main loop.

   e.g.:
   ```
       (app-state ::my-app-state
              :init (fn [] (println \"App State initialized.\"))
              :update (fn [tpf] (println \"update:\" tpf))
              :on-enable (fn [] (println \"on enable\"))
              :on-disable (fn [] (println \"on disable\"))
              :cleanup (fn [] (println \"cleaning\")))
   ```
   
   If any function returns a hash map, the hash map will be registered to the mutable global state under
   app-states entry.

   Please have a look [`AppState`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/state/AppState.html)
   and [`BaseAppState`](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/app/state/BaseAppState.html) for more."
  [kw & {:keys [init update on-enable on-disable cleanup]}]
  (check-qualified-keyword kw)
  (let [simple-app (atom nil)
        default-f  (constantly nil)]
    (proxy [BaseAppState] []
      (initialize [app]
        (reset! simple-app (cast SimpleApplication app))
        (binding [*app* app]
          (let [result (init)]
            (when (map? result)
              (swap! states assoc-in [::app-states kw] result)))))
      (update [tpf]
        (apply-fn-in-app :app-state @simple-app kw (or update default-f) tpf))
      (onEnable []
        (apply-fn-in-app :app-state @simple-app kw (or on-enable default-f)))
      (onDisable []
        (apply-fn-in-app :app-state @simple-app kw (or on-disable default-f)))
      (cleanup [app]
        (apply-fn-in-app :app-state app kw (or cleanup default-f))))))


(defn control
  "Controls are used to specify certain update and render logic for a Spatial.

   If any function returns a hash map, the hash map will be registered to the mutable global state under
   controls entry.
   ```
   (control ::my-control
            :init (fn [] (println \"init\" tpf))
            :update (fn [tpf] (println \"update\" tpf))
            :render (fn [rm vp] (println \"render\" rm vp)))
   ```
   Also, there is `:set-spatial` callback that you can provide your custom fn. Either way, you'll have `spatial` in
   your control's state with `:spatial` key.

   Please have a look 
   [Control](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/control/Control.html) and 
   [AbstractControl](https://javadoc.jmonkeyengine.org/v3.4.0-stable/com/jme3/scene/control/AbstractControl.html) for more."
  [kw & {:keys [init update render] :as m}]
  (check-qualified-keyword kw)
  (let [init        (wrap-with-bound init)
        update      (wrap-with-bound update)
        render      (wrap-with-bound render)
        set-spatial (wrap-with-bound (:set-spatial m))
        r           (proxy [AbstractControl] []
                      (controlUpdate [tpf]
                        (binding [*control* this]
                          (apply-fn-in-app :control *app* kw update tpf)))
                      (controlRender [rm vp]
                        (binding [*control* this]
                          (apply-fn-in-app :control *app* kw render rm vp)))
                      (setSpatial [spatial]
                       ;; to avoid reflection warning
                        (let [^AbstractControl this this]
                          (binding [*control* this]
                            (proxy-super setSpatial spatial)
                            (set-state :control [kw :spatial] spatial)
                            (apply-fn-in-app :control *app* kw set-spatial spatial)))))]
    (binding [*control* r]
      (apply-fn-in-app :control *app* kw init))
    r))


(defn get-spatial []
  (.getSpatial *control*))


(defn control-enabled? []
  (.isEnabled *control*))


(defn running?
  [^SimpleApplication app]
  (boolean (some-> app .getContext .isCreated)))


(defn start
  "Starts the SimpleApplication instance.

   It's not recommended to call `start` fn after calling `stop` fn.
   Some odd behaviours might occur such as JVM crash (based on the app, apps use Bullet API [not always]).

   If you would like to re-start the app then use `unbind-app` instead of `stop`,
   after re-defining app with `defsimpleapp` then call `start` again."
  ([^SimpleApplication app]
   (doto app .start))
  ([^SimpleApplication app type]
   (let [type (case type
                :canvas JmeContext$Type/Canvas
                :display JmeContext$Type/Display
                :headless JmeContext$Type/Headless
                :offscreen-surface JmeContext$Type/OffscreenSurface)]
     (doto app (.start ^JmeContext$Type type)))))


(set! *warn-on-reflection* false)
(defn- invoke-method [obj fn-name-string & args]
  (let [m (first (filter (fn [x] (.. x getName (equals fn-name-string)))
                         (.. obj getClass getDeclaredMethods)))]
    (.setAccessible m true)
    (.invoke m obj args)))
(set! *warn-on-reflection* true)


(defn- not-default-app-state? [s]
  (not-any? #(instance? % s) #{AudioListenerState
                               DebugKeysAppState
                               FlyCamAppState
                               ResetStatsState
                               StatsAppState}))


(defn clear
  "Detaches all child nodes and removes all local lights from the root node of the application which is the current value of [[*app*]]."
  [^SimpleApplication app]

  (let [root-node     (.getRootNode app)
        state-manager (.getStateManager app)
        app-states    (filter not-default-app-state? (invoke-method state-manager "getStates"))]
    (detach-all-child root-node)
    (.clear (.getLocalLightList root-node))
    (doseq [^AppState s app-states]
      (.detach state-manager s))
    (invoke-method state-manager "terminatePending")
    (reset! states {})))


(defn stop
  "Stops the SimpleApplication instance. Can be used when the user wants to exit from the game.

   It's not recommended to call `start` fn after calling `stop` fn.
   Some odd behaviours might occur such as JVM crash (based on the app, apps use Bullet API [not always]).

   If you would like to re-start the app then use `unbind-app` instead of `stop`,
   after re-defining app with `defsimpleapp` then call `start` again."
  [^SimpleApplication app]
  (try
    (.stop app true)
    (catch Throwable t
      (println "Error occurred on stop." (.getMessage t)))
    (finally
      (clear app))))


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
    (.unbindRoot v)))


(defn unbind-all
  "Unbinds all SimpleApplication instances from the vars. Should be used for development purposes only.
   Can be used for some leftover instances in the REPL.

   e.g.: (unbind-all)

   After calling `unbind-all`, `app` can be re-defined with `defsimpleapp`."
  []
  (doseq [v @instances]
    (unbind-app v))
  (reset! instances []))


(defn enqueue
  "Engue a form `f` to be executed within the current binding environment, 
   but in the main rendering loop thread of the current [[*app*]]."
  [f]
  (let [^Runnable f (bound-fn* f)]
    (.enqueue *app* f)))


(defmacro enqueue*
  "Macro version of `enqueue`. `fn` wrapping not needed."
  [& body]
  `(let [^Runnable f# (bound-fn* (fn [] ~@body))]
     (.enqueue *app* f#)))


(defmacro run
  "Every code that changes the state should be wrapped with `run` macro.
   Otherwise `Make sure you do not modify the scene from another thread!` exception will be thrown.
   
   Returns `nil`."
  [app & body]
  `(binding [*app* ~app]
     (enqueue* ~@body)))
