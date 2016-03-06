(ns nines.entities)

(defn new-board [width height]
  {
   :dimensions {:width width
                :height height}
   :tiles {}
   })

(defn new-tile [id content x y]
  {:id id
   :content content
   :pos {:x x :y y}
   })

(defn add-tile [board tile]
  (update-in board [:tiles] assoc (tile :pos) tile))


; events
; events
; events

(defn new-tile-appearance-event [tile]
  {:key     :tile-appearance
            :tile tile})

(defn new-tile-slide-event [tile target-pos]
  {:key     :tile-slide
            :tile-id (:id tile)
            :slide   {:start-time (js/Date.now)
                      :target-pos target-pos}})
