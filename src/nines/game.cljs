(ns nines.game
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [nines.entities :refer [new-model new-board new-tile add-tile new-tile-appearance-event new-tile-slide-event]]
            [nines.util     :refer [next-id!]]
            [nines.drawing  :as    drawing]
            [nines.input    :as    input]
            [nines.logic    :as    logic]
            [nines.tests]
            [monet.core :as mnt-core]
            [cljs.core.async :refer [<! >! timeout chan]]))
(enable-console-print!)

(defn- setup! [width height]
  (let [board   (-> (new-board width height)
                  (add-tile (new-tile (next-id! :tile) 8 2 2))
                  (add-tile (new-tile (next-id! :tile) 8 0 1)))
        ]
    (new-model board)))

(defn- generate-events [model type event]
  (case type
    :move     (logic/generate-move-events model event)
    :new-tile (logic/generate-new-tile-event model event)
    (throw (js/Error. (str "Unrecognized game command: " type)))
  ))

(defn- apply-tile-slide [model event]
  (let [tile-id    (:tile-id event)
        slide      (:slide event)
        target-pos (:target-pos slide)]
    (println target-pos)
    (assoc-in model [:board :tiles tile-id :pos] target-pos)))

(defn- apply-event [model event]
  (case (:key event)
    :tile-slide (apply-tile-slide model event)
    model))

(defn- apply-events [model events]
  (reduce apply-event model events))

(defn- start [canvas-id width height]
  (let [model  (setup! width height)
        drawCh (drawing/start-engine canvas-id (:board model))
        gameCh (chan)]
    (input/init-input! gameCh)
    (go-loop [model model]
             (let [[type event] (<! gameCh)
                   events       (generate-events model type event)
                   new-model    (apply-events model events)]
               (doseq [event events] (>! drawCh event))
               (recur new-model)))))

(start "game" 4 4)

;(def denBoard (:board (setup! 4 4)))
;(def ch (drawing/start-engine "game" denBoard))
;(go (>! ch (new-tile-appearance-event
;              (new-tile (next-id! :tile) 12 0 3))))
;(go (>! ch (new-tile-slide-event
;              (:id (second (vals (:tiles denBoard))))
;              {:x 2 :y 0})))

