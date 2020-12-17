(ns jme-clj.examples.hello-material
  "Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_material.html"
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.material RenderState$BlendMode)
   (com.jme3.math ColorRGBA)
   (com.jme3.renderer.queue RenderQueue$Bucket)
   (com.jme3.scene.shape Sphere$TextureMode)))


(defn init []
  ;;A simple textured cube -- in good MIP map quality.
  (let [asset-manager (get-manager :asset)
        cube-1-mesh   (box 1 1 1)
        cube-1-geo    (geo "My Textured Box" cube-1-mesh)
        cube-1-mat    (material asset-manager "Common/MatDefs/Misc/Unshaded.j3md")
        cube-1-tex    (load-texture "Interface/Logo/Monkey.jpg")
        ;;A translucent/transparent texture, similar to a window frame.
        cube-2-mesh   (box 1 1 0.01)
        cube-2-geo    (geo "window frame" cube-2-mesh)
        cube-2-mat    (material asset-manager "Common/MatDefs/Misc/Unshaded.j3md")
        ;;A bumpy rock with a shiny light effect.
        sphere-mesh   (sphere 32 32 2)
        sphere-geo    (geo "Shiny rock" sphere-mesh)
        sphere-mesh   (-> sphere-mesh
                          (set* :texture-mode Sphere$TextureMode/Projected)
                          generate)
        sphere-mat    (material asset-manager "Common/MatDefs/Light/Lighting.j3md")]
    (set* cube-1-mat :texture "ColorMap" cube-1-tex)
    (set* cube-1-geo :material cube-1-mat)
    (add-to-root cube-1-geo)

    (set* cube-2-mat :texture "ColorMap" (load-texture "Textures/ColoredTex/Monkey.png"))
    (-> cube-2-mat (get* :additional-render-state) (set* :blend-mode RenderState$BlendMode/Alpha))
    (-> cube-2-geo
        (set* :queue-bucket RenderQueue$Bucket/Transparent)
        (set* :material cube-2-mat)
        (add-to-root))

    (-> sphere-mat
        ;; had to wrap with `float`, interop could not find the correct method
        (set* :float "Shininess" (float 64.0))
        (set* :texture "DiffuseMap" (load-texture "Textures/Terrain/Pond/Pond.jpg"))
        (set* :texture "NormalMap" (load-texture "Textures/Terrain/Pond/Pond_normal.png"))
        (set* :boolean "UseMaterialColors" true)
        (set* :color "Diffuse" ColorRGBA/White))

    (-> sphere-geo
        (set* :material sphere-mat)
        (set* :local-translation 0 2 -2)
        (rotate 1.6 0 0)
        (add-to-root))

    (-> (light :directional)
        (set* :direction (vec3 1 0 -2 :normalize))
        (set* :color ColorRGBA/White)
        (add-light-to-root))))


(defsimpleapp app :init init)


(comment
 (start app)
 (stop app)

 (re-init app init)

 (unbind-app #'app))