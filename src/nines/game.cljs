(ns nines.game
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [nines.drawing :as drawing]
            [nines.util :as util]
            [monet.core :as mnt-core]
            [cljs.core.async :refer [<! >! timeout chan]]))

(enable-console-print!)

(defn- new-board [width height]
  {
   :dimensions {:width width
                :height height}
   :tiles {}
   })

(defn- new-tile [id content x y]
  {:id id
   :content content
   :pos {:x x :y y}
   })

(defn- add-tile [board tile]
  (update-in board [:tiles] assoc (tile :pos) tile))

(defn- setup! [width height]
  (let [board   (-> (new-board width height)
                  (add-tile (new-tile (util/next-id! :tile) 7 2 2))
                  (add-tile (new-tile (util/next-id! :tile) 8 0 1)))
        ]
    board))

(defn- loop! [timestamp state]
  (do
    ;;(println state)
    (mnt-core/animation-frame #(loop! %1 state))))

(loop! 0 (setup! 4 4))




(def denBoard (setup! 4 4))

(def ch (drawing/start-engine "game" denBoard))

(go (>! ch {:key :tile-appearance
            :tile (new-tile (util/next-id! :tile) 12 0 3)}))

 (go (>! ch {:key     :tile-slide
             :tile-id (:id (second (vals (:tiles denBoard))))
             :slide   {:start-time (js/Date.now)
                       :target-pos {:x 2 :y 0}}}))
