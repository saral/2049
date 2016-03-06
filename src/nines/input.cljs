(ns nines.input
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [chan put! pipe unique merge map< filter< alts!]]
            [clojure.set :refer [union]]
            [clojure.string :as string]
            [goog.events :as events]
            [goog.dom :as gdom]
            ))

(def keycodes
  "Keycodes that interest us. Taken from
  http://docs.closure-library.googlecode.com/git/closure_goog_events_keynames.js.source.html#line33"
  {37 :left
   38 :up
   39 :right
   40 :down
   32 :space
   13 :enter})

(defn- event->key
  "Transform an js event object into the key name"
  [ev] (get keycodes (.-keyCode ev) :key-not-found))

(defn- event-chan
  "Creates a channel with the events of type event-type and optionally applies
  the function parse-event to each event."
  ([event-type] (event-chan event-type identity false))
  ([event-type parse-event]
   (event-chan event-type parse-event false))
  ([event-type parse-event prevent-default]
   (let [ev-chan (chan)]
     (events/listen (.-body js/document)
                    event-type
                    #(do
                       (if prevent-default (.preventDefault %))
                       (put! ev-chan (parse-event %))))
     ev-chan)))

(defn- keys-chan
  "Returns a channel with the key events of event-type parsed and
  filtered by the allowed-keys"
  [event-type allowed-keys]
  (let [evs (event-chan event-type event->key)]
    (filter< allowed-keys evs)))

(def move-keys "Keys that trigger movement" #{:left :up :right :down})
(def reset-key "Reset key" #{:enter})

(def valid-keys-down
  "Keys we want to listen on key down"
  (union move-keys reset-key))

(defn- keys-down-chan
  "Create a channel of keys pressed down restricted by the valid keys"
  [] (keys-chan (.-KEYDOWN events/EventType) valid-keys-down))

(defn- key-down->command
  "Transform a key pressed down to the command we will send to the game"
  [k]
  (cond
   (move-keys k) [:move k]
   (reset-key k) [:reset]))

(defn- touch-event->coords [e]
  (let [touch (aget (.. e -event_ -touches) 0)]
    {:x (.-pageX touch) :y (.-pageY touch)}))

(defn- touch-move-chan []
  (event-chan (.-TOUCHMOVE events/EventType) touch-event->coords true))

(defn- touch-commands []
  (let [moves (touch-move-chan)
        cmds (chan)]
    (go-loop [previous {:x 0 :y 0}
              coords (<! moves)]
      (let [diff {:x (- (:x coords) (:x previous)) :y (- (:y coords) (:y previous))}]
        (if (> (js/Math.abs (:x diff)) (js/Math.abs (:y diff)))
          (if (> 0 (:x diff))
            (>! cmds (key-down->command :left))
            (>! cmds (key-down->command :right))
            )
          (if (> 0 (:y diff))
            (>! cmds (key-down->command :up))
            (>! cmds (key-down->command :down))
            )))
        (recur coords (<! moves)))
    cmds))

(defn init-input!
  "Initialize event processing. It takes all the key presses and transforms
  them into commands and passes them to the game commands channel"
  [game-commands]
  (let [keys-pressed (keys-down-chan)
        commands (merge [(map< key-down->command keys-pressed)
                                 (touch-commands)])]
    (pipe commands game-commands)))
