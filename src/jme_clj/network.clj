(ns jme-clj.network
  (:require
   [clojure.edn :as edn])
  (:import
   (com.jme3.network
    Client
    ClientStateListener
    MessageListener
    Network
    Server)
   (com.jme3.network.serializing Serializer)
   (org.jme.network JmeMessage)))

(set! *warn-on-reflection* true)


(defn register-serializer [^Class c]
  (Serializer/registerClass c))


(defn init-default-serializers []
  (register-serializer JmeMessage))


(defn message
  "`data` parameter can only be pure Clojure data structures."
  [data]
  (JmeMessage. (pr-str data)))


(defn create-client [& {:keys [game-name version host host-port remote-udp-port]}]
  (Network/connectToServer game-name version host host-port remote-udp-port))


(defn create-server [& {:keys [game-name version host-port remote-udp-port]}]
  (Network/createServer game-name version host-port remote-udp-port))


(defn add-message-listener [obj on-message-received]
  (if (instance? Client obj)
    (doto ^Client obj (.addMessageListener (reify MessageListener
                                             (messageReceived [_ source msg]
                                               (on-message-received source msg)))))
    (doto ^Server obj (.addMessageListener (reify MessageListener
                                             (messageReceived [_ source msg]
                                               (on-message-received source msg)))))))


(defn add-client-state-listener [^Client client on-client-connected on-client-disconnected]
  (doto client (.addClientStateListener (reify ClientStateListener
                                          (clientConnected [_ client]
                                            (on-client-connected client))
                                          (clientDisconnected [_ client info]
                                            (on-client-disconnected client info))))))


(defn start-client [^Client c]
  (doto c .start))


(defn close-client [^Client c]
  (doto c .close))


(defn start-server [^Server s]
  (doto s .start))


(defn close-server [^Server s]
  (doto s .close))


(defn get-message [msg]
  (when (instance? JmeMessage msg)
    (edn/read-string (.getMessage ^JmeMessage msg))))


(defn send-message [^Client c data]
  (.send c (message data))
  data)


(defn broadcast
  ([^Server s msg]
   (.broadcast s msg))
  ([^Server s filter msg]
   (.broadcast s filter msg))
  ([^Server s channel filter msg]
   (.broadcast s channel filter msg)))


(defn get-id [^Client c]
  (.getId c))


(defn started? [^Client c]
  (.isStarted c))


(defn connected? [^Client c]
  (.isConnected c))
