;; Please start your REPL with `+test` profile
(ns examples.simple-multiplayer.client
  (:require
   [examples.simple-multiplayer.player-app-state :as app-states.player]
   [jme-clj.core :refer :all]
   [jme-clj.network :refer :all]
   [clojure.set :as set])
  (:import
   (com.jme3.input KeyInput)
   (com.jme3.math Vector3f ColorRGBA)
   (com.jme3.texture Texture$WrapMode)
   (com.jme3.bullet.control BetterCharacterControl)))


(defn- init-materials []
  (letj [floor-mat (unshaded-mat)]
        (set* floor-mat :texture "ColorMap" (-> "Textures/Terrain/Pond/Pond.jpg"
                                                (load-texture)
                                                (set* :wrap Texture$WrapMode/Repeat)))))


(defn- init-floor [bullet-as floor floor-mat]
  (let [floor-geo (-> (geo "Floor" floor)
                      (set* :material floor-mat)
                      (set* :local-translation 0 -0.1 0)
                      (add-to-root))
        floor-phy (rigid-body-control 0.0)]
    (add-control floor-geo floor-phy)
    (-> bullet-as
        (get* :physics-space)
        (call* :add floor-phy))))


(defn- create-player-data []
  {:location       (vec3)
   :rotation       (quat)
   :walk-direction (vec3)
   :id             0})


(defn- add-lights []
  (let [sun     (-> (light :directional)
                    (setc :direction (vec3 -0.5 -0.5 -0.5)
                          :color ColorRGBA/White))
        ambient (-> (light :ambient)
                    (set* :color ColorRGBA/White))]
    (add-light-to-root sun)
    (add-light-to-root ambient)))


;; for keeping internal *bindings* work, also the app. We need to define
;; listeners with `defn`. `def` should NOT be used!
(defn- on-action-listener []
  (action-listener
   (fn [name* pressed? tpf]
     (set-state :app-state [::app-states.player/player (-> name* name keyword)] pressed?))))


(defn- init-keys []
  (apply-input-mapping
   ;; Using qualified keywords for inputs is highly recommended!
   {:triggers  {::up    (key-trigger KeyInput/KEY_UP)
                ::down  (key-trigger KeyInput/KEY_DOWN)
                ::left  (key-trigger KeyInput/KEY_LEFT)
                ::right (key-trigger KeyInput/KEY_RIGHT)}
    :listeners {(on-action-listener) [::left ::right ::up ::down]}}))


(defn- remove-players [players-to-remove players bullet-as]
  (doseq [id players-to-remove]
    (enqueue
     (fn []
       (remove-state [:players id])
       (when-let [spatial (-> players (get id) :spatial)]
         (remove-from-root spatial)
         (-> bullet-as (get* :physics-space) (call* :remove spatial)))))))


(defn- update-players [players-to-update all-players]
  (doseq [id players-to-update]
    (let [player   (-> (get-state) :players (get id))
          spatial  (:spatial player)
          new-data (get all-players id)]
      (update-state :app [:players id] merge new-data)
      (enqueue*
       (-> spatial
           (get* :control BetterCharacterControl)
           (call* :warp (vec->vec3 (:location new-data))))
       (set* spatial :local-rotation (vec->quat (:rotation new-data)))))))


(defn- add-players [players-to-add all-players bullet-as]
  (doseq [id players-to-add]
    (let [data    (get all-players id)
          control (better-character-control 1.5 9 80)
          spatial (-> (load-model "Models/Oto/OtoOldAnim.j3o")
                      (setc :local-translation (vec->vec3 (:location data))
                            :local-rotation (vec->quat (:rotation data)))
                      (add-control control))]
      (-> control
          (set* :gravity (vec3 0 40 0))
          (call* :warp (vec->vec3 (:location data))))
      (set-state [:players id] (assoc data :spatial spatial))
      (enqueue*
       (add-to-root spatial)
       (-> bullet-as (get* :physics-space) (call* :add-all spatial))))))


(defn- process-players [all-players]
  (let [{:keys [player-data players bullet-as]} (get-state)
        player-id         (:id player-data)
        players-ids       (set (keys players))
        all-players-ids   (set (keys all-players))
        players-to-remove (set/difference players-ids all-players-ids)
        players-to-update (set/difference (set/intersection players-ids all-players-ids) (hash-set player-id))
        players-to-add    (set/difference all-players-ids players-ids)]
    (remove-players players-to-remove players bullet-as)
    (update-players players-to-update all-players)
    (add-players players-to-add all-players bullet-as)))


(defn- init-client []
  (-> (create-client :game-name "network game"
                     :version 1
                     :host "localhost"
                     :host-port 5110
                     :remote-udp-port 5110)
      (add-message-listener (fn [source msg]
                              (when-let [msg (get-message msg)]
                                (case (:type msg)
                                  :text (println "Server message:" (:data msg))
                                  :players (process-players (:data msg))
                                  :close-conn (when (= (get-id source) (:data msg))
                                                (-> (get-state) :client close-client))))))
      (add-client-state-listener (fn [client]
                                   (println "ID:" (get-id client))
                                   (set-state [:player-data :id] (get-id client))
                                   (send-message client {:type :text
                                                         :data "Hello!"}))
                                 (fn [client info]))
      (start-client)
      (#(hash-map :client %))))


(defn- init []
  (let [bullet-as (bullet-app-state)
        floor     (box 50 0.1 50)]
    (setc (fly-cam)
          :move-speed 30
          :drag-to-rotate true)
    (set* (cam) :location (vec3 -1.5, 52.5, 97.5))
    (look-at (vec3 0.01 -0.6 -0.79) Vector3f/UNIT_Y)
    (attach bullet-as)
    (scale-texture-coords floor (vec2 3 6))
    (init-floor bullet-as floor (:floor-mat (init-materials)))
    (add-lights)
    (attach (app-states.player/create-player-as bullet-as))
    (init-keys)
    (merge
     (init-client)
     {:player-data (create-player-data)
      :bullet-as   bullet-as})))


(defn- simple-update [tpf]
  (let [{:keys [client player-data]} (get-state)]
    (when (connected? client)
      (send-message client {:type :player-data
                            ;; we need to convert instances, Serialization only works with pure Clojure
                            ;; data structures.
                            :data (-> player-data
                                      (update :location vec3->vec)
                                      (update :walk-direction vec3->vec)
                                      (update :rotation quat->vec))}))))


;;TODO since state is removed, we get NP inside call*
(defn- destroy []
  (let [{:keys [bullet-as client player-data]} (get-state)]
    (call* bullet-as :cleanup)
    (send-message client {:type :close-conn
                          :data {:id (:id player-data)}})))


(defsimpleapp app
              :opts {:show-settings?       false
                     :pause-on-lost-focus? false
                     :settings             {:title          "My JME Game"
                                            :load-defaults? true
                                            :frame-rate     60}}
              :init init
              :update simple-update
              :destroy destroy)


(comment
 ;; first, we need to register serializers
 (init-default-serializers)

 ;; then we can call the start app
 (start app :display)

 (run app
      (re-init init))

 ;;after calling unbind-app, we need to re-define the app with defsimpleapp
 (unbind-app #'app)
 )
