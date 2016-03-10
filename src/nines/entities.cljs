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
   :counting? false
   })

(defn add-tile [board tile]
  (update-in board [:tiles] assoc (:id tile) tile))


(defn new-model [board]
  {:board board
   :status :idle})

; events
; events
; events

(defn new-tile-appearance-event [tile]
  {:key     :tile-appearance
   :tile tile})

(defn new-tile-slide-event [tile-id target-pos merging-with]
  {:key     :tile-slide
   :tile-id tile-id
   :slide   {:start-time   (js/Date.now)
             :target-pos   target-pos
             :merging-with merging-with}})

(defn new-countdown-event [tile-id new-content]
  {:key          :tile-countdown
   :tile-id   tile-id
   :new-content  new-content})
