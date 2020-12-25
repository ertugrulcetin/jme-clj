(ns examples.network.client
  (:require [jme-clj.core :refer :all]
            [jme-clj.network :refer :all]))


(defn init []
  (-> (create-client :game-name "network game"
                     :version 1
                     :host "localhost"
                     :host-port 5110
                     :remote-udp-port 5110)
      (add-message-listener (fn [source msg]
                              (some->> msg get-message (println "Client received:"))))
      (add-client-state-listener (fn [client]
                                   (send-message client {:text "Hello!"}))
                                 (fn [client info]))
      (start-client)
      (#(hash-map :client %))))


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
 (start app :display)

 (unbind-all)
 )