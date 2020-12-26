;; Please start your REPL with `+test` profile
(ns examples.simple-multiplayer.server
  (:require [jme-clj.core :refer :all]
            [jme-clj.network :refer :all])
  (:import (com.jme3.network Filters)))


(defn init []
  (-> (create-server :game-name "network game"
                     :version 1
                     :host-port 5110
                     :remote-udp-port 5110)
      (add-message-listener (fn [source msg]
                              (let [server (:server (get-state))
                                    msg    (get-message msg)]
                                (case (:type msg)
                                  :text (do
                                          (println "Received: " (:data msg) " source: " (.getId source))
                                          (broadcast server
                                                     (Filters/notEqualTo source)
                                                     (message {:type :text
                                                               :data (format "Client id %s joined."
                                                                             (.getId source))})))
                                  :player-data (set-state [:players (:id (:data msg))] (:data msg))
                                  :close-conn (do)))))
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