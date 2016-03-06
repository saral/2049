(ns nines.logic
  (:require [nines.util :as util]
            [nines.entities :refer [new-tile-appearance-event new-tile-slide-event]]
            ))
(enable-console-print!)

(defn generate-new-tile-event [model event]
  (new-tile-appearance-event event))

(def orientation {:left  {:row :y :col :x :dir-fn <}
                  :right {:row :y :col :x :dir-fn >}
                  :up    {:row :x :col :y :dir-fn <}
                  :down  {:row :x :col :y :dir-fn >}})

(defn is-not-on-same-row-according-to-direction [pos reference-pos direction]
  (let [orientation  (direction orientation)
        pos-row      ((:row orientation) pos)
        ref-pos-row  ((:row orientation) reference-pos)]
    (= pos-row ref-pos-row)))

(defn is-before-col-according-to-direction [pos reference-pos direction]
  (let [orientation  (direction orientation)
        pos-col      ((:col orientation) pos)
        ref-pos-col  ((:col orientation) reference-pos)
        dir-fn       (:dir-fn orientation)]
    (dir-fn pos-col ref-pos-col)))

(defn is-in-direction-from-position [{:keys [pos]} direction reference-pos]
  (if (is-not-on-same-row-according-to-direction pos reference-pos direction)
    false
    (is-before-col-according-to-direction pos reference-pos direction)))

(defn tiles-in-direction-from-position [tiles direction pos]
  (filter #(is-in-direction-from-position % direction pos) (vals tiles)))

(defn generate-move-events [{:keys [board]} direction]
  (let [tiles (vals (:tiles board))]
  (doseq [tile tiles]
    (let [tiles-in-dir (tiles-in-direction-from-position tiles direction (:pos tile))]
      []))))




