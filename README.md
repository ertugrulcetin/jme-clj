# jme-clj

A Clojure wrapper library for [jMonkeyEngine](https://github.com/jMonkeyEngine/jmonkeyengine). jMonkeyEngine is a 3D
game engine for adventurous Java developers (now Clojure developers). It is open-source, cross-platform, and
cutting-edge. Please check [jMonkeyEngine Wiki](https://wiki.jmonkeyengine.org/docs/3.3/documentation.html) if you would
like to learn more about the engine, highly recommended! Also, there
is [jMonkeyEngine Hub](https://hub.jmonkeyengine.org/) that you can ask/search for questions, the community is very
responsive and friendly.

The engine is used by several commercial game studios and computer-science courses. Here's a taste:
![jME3 Games Mashup](https://i.imgur.com/nF8WOW6.jpg)

## Justification

> The best thing about making a game in Clojure is that you can modify it in a REPL while it's running. By simply reloading a namespace, your code will be injected into the game, uninhibited by the restrictions posed by tools like HotSwap. Additionally, a REPL lets you read and modify the state of your game at runtime, so you can quickly experiment and diagnose problems.

> Clojure also brings the benefits of functional programming. This is becoming a big topic of discussion in gamedev circles, including by John Carmack. Part of this is due to the prevalence of multi-core hardware, making concurrency more important. Additionally, there is a general difficulty of maintaining object-oriented game codebases as they grow, due to complicated class hierarchies and state mutations.

It is from Zach Oakes's **play-clj** library. This summarises the delicacy of the situation perfectly.

## Installation
[![Clojars Project](https://clojars.org/jme-clj/latest-version.svg)](https://clojars.org/jme-clj)

## Usage

Please note that the library still in the development (alpha) stage, so there might be some breaking changes. Please
have a look [/test/examples](https://github.com/ertugrulcetin/jme-clj/tree/master/test/examples) for more.

```clojure
(require '[jme-clj.core :refer :all])

(import '(com.jme3.math ColorRGBA))

(defn init []
  (let [box  (box 1 1 1)
        geom (geo "Box" box)
        mat  (material "Common/MatDefs/Misc/Unshaded.j3md")]
    (set* mat :color "Color" ColorRGBA/Blue)
    (set* geom :material mat)
    (add-to-root geom)))

(defsimpleapp app :init init)

(start app)
```

## Recommended Learning Path

- [REPL Based Tutorial](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/repl_based_tutorial.clj)
- [Hello SimpleApplication](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_simple_app.clj)
- [Hello Node](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_node.clj)
- [Hello Asset](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_asset.clj)
- [Hello Update Loop](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_update_loop.clj)
- [Hello Input System](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_input_system.clj)
- [Hello Material](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_material.clj)
- [Hello Animation](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_animation.clj)
- [Hello Picking](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_picking.clj)
- [Hello Collision](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_collision.clj)
- [Hello Terrain](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_terrain.clj)
- [Hello Audio](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_audio.clj)
- [Hello Effects](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_effects.clj)
- [Hello Physics](https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/beginner_tutorials/hello_physics.clj)

## Demo Video

[![jme-clj | Clojure 3D Game Development Demo](https://img.youtube.com/vi/IOPz9I49snM/0.jpg)](https://www.youtube.com/watch?v=IOPz9I49snM)

## License

MIT License

Copyright (c) 2020 Ertuğrul Çetin

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.