(ns examples.network.server
  (:require [jme-clj.core :refer :all]
            [jme-clj.network :refer :all])
  (:import (com.jme3.network Filters)))


(defn init []
  (-> (create-server :game-name "network game"
                     :version 1
                     :host-port 5110
                     :remote-udp-port 5110)
      (add-message-listener (fn [source msg]
                              (some->> msg get-message (println "Server received:"))
                              (broadcast (:server (get-state))
                                         (Filters/notEqualTo source)
                                         (message {:source-id (.getId source)}))))
      (start-server)
      (#(hash-map :server %))))


(defsimpleapp app
              :opts {:show-settings?       false
                     :pause-on-lost-focus? false
                     :settings             {:title          "My JME Game"
                                            :load-defaults? true
                                            :frame-rate     60}}
              :init init)

(comment
 ;; first, we need to register serializers
 (init-default-serializers)

 ;; then we can call the start app
 (start app :headless)

 (unbind-all)
 )