;; A REPL based, jme-clj tutorial
;; Please visit https://github.com/ertugrulcetin/jme-clj for more info
;;
;; This is a very basic intro that covers jme-clj's basic features and philosophy.
;;
;; Don't forget to check Beginner Tutorials:
;; https://github.com/ertugrulcetin/jme-clj/tree/master/test/examples/beginner_tutorials
(ns examples.repl-based-tutorial)


;; Run your REPL with `+test` profile, so the code will be able to access the `resources` folder.
(comment

 (use 'jme-clj.core)

 (import '(com.jme3.math ColorRGBA))

 ;; Our simple init fn, it creates a cube with Monkey.jpg on the surface.
 (defn init []
   (let [player (geo "jMonkey cube" (box 1 1 1))
         mat    (unshaded-mat)]
     (set* mat :texture "ColorMap" (load-texture "Interface/Logo/Monkey.jpg"))
     (set* player :material mat)
     (add-to-root player)))

 ;; Let's create simple-update fn with no body for now.
 (defn simple-update [tpf])

 ;; We define a `app` var.
 (defsimpleapp app
               :init init
               :update simple-update)

 ;; Here we go. You'll see jMonkeyEngine's dialog box, after hitting the "Continue" button
 ;; our app will start.
 (start app)

 ;; It's time to add some magic, let's rotate the cube. First of all we're going to re-define the init fn
 ;; Can you spot the only change? Yes, we're returning hash map that holds player data.
 ;; When init fn returns a hash map, it's value registered into the app's global mutable state,
 ;; so other fns can access for later use. This applies for the update fn as well.
 (defn init []
   (let [player (geo "jMonkey cube" (box 1 1 1))
         mat    (unshaded-mat)]
     (set* mat :texture "ColorMap" (load-texture "Interface/Logo/Monkey.jpg"))
     (set* player :material mat)
     (add-to-root player)
     {:player player}))

 ;; Let's call re-init (Re-initializes the app) so we can let the app know about the changes.
 ;; re-init is like restart but does not call stop and start fns.
 (run app
      (re-init init))

 ;; When you focus your cursor to the app, you'll see that the cube is rotating. Update fn (simple-update) does not
 ;; need something like `re-update` fn. init fn is only called once that's why we re-initialize with re-init,
 ;; on the other hand simple-update is constantly called by the app.
 (defn simple-update [tpf]
   (let [{:keys [player]} (get-state)]
     (rotate player 0 (* 2 tpf) 0)))

 ;; `unbind-app` destroys the app, unbinds the `app` var. Which means it's gone for good. It can be used for
 ;; development purposes, e.g. when you want to create an app with different attributes.
 (unbind-app #'app)

 ;; Let's create an app with different options.
 (defsimpleapp app
               :opts {:show-settings?       false
                      :pause-on-lost-focus? false
                      :settings             {:title          "My JME Game"
                                             :load-defaults? true
                                             :frame-rate     60
                                             :width          800
                                             :height         600}}
               :init init
               :update simple-update)

 ;; Starting the new app. Now, you won't see jMonkeyEngine's dialog because we set :show-settings? to false.
 ;; Since we set :pause-on-lost-focus? to false, we don't have to focus the cursor, cube is rotating.
 ;; FPS limit set to 60, and we have a new title `My JME Game`. Finally, resolution is 800x600.
 (start app)

 ;; We can change the cube's position by setting its local translation.
 ;; Please note that every state change related code should we wrapped with run macro!
 (run app
      (let [{:keys [player]} (get-state)]
        (set* player :local-translation (add (get* player :local-translation) 1 1 1))))

 ;; By default, there is Fly Camera attached to the app that you can control with W,A,S,D keys. Let's increase it's
 ;; move speed. Now, you fly faster :)
 (run app
      (set* (fly-cam) :move-speed 15))

 )
