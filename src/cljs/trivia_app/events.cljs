(ns trivia-app.events
  (:require
   [ajax.core :refer [GET]]
   [re-frame.core :as re-frame]
   [trivia-app.db :as db]
   [camel-snake-kebab.core :refer [->kebab-case-keyword]]
   [camel-snake-kebab.extras :refer [transform-keys]]
   [goog.string :as gstring]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 :time-color-change
 (fn [db [_ new-color-value]]
   (assoc db :time-color new-color-value)))

(re-frame/reg-event-db
 :answer
 (fn [db [_ val]]
   (assoc db :answer val)))

(re-frame/reg-event-db
 :index
 (fn [db [_ val]]
   (assoc db :index val)))

(re-frame/reg-event-db
 :status
 (fn [db [_ val]]
   (assoc db :status val)))

(re-frame/reg-event-db
 :score
 (fn [db [_ val]]
   (assoc db :score val)))

(re-frame/reg-event-db
 :data
 (fn [db [_ val]]
   (assoc db :data val)))

(re-frame/reg-event-db
 :process-response
 (fn
   [db [_ response]]           ;; destructure the response from the event vector
   (let [results-data (->> (js->clj response)
                           (transform-keys ->kebab-case-keyword)
                           :results
                           (map (fn [{:keys [question correct-answer incorrect-answers]
                                      :as   q}]
                                  {:options        (->> (concat incorrect-answers [correct-answer])
                                                        (map gstring/unescapeEntities)
                                                        (shuffle))
                                   :question       (gstring/unescapeEntities question)
                                   :correct-answer (gstring/unescapeEntities correct-answer)})))]
   (-> db
       (assoc :loading? false) ;; take away that "Loading ..." UI 
       (assoc :data results-data)
       ))  
     )
 )

(re-frame/reg-event-db                   
 :bad-response             
 (fn
   [db [_ _]]           ;; destructure the response from the event vector
   (-> db
       (assoc :loading? false) ;; take away that "Loading ..." UI 
       (println "Error in http call" ))))


(re-frame/reg-event-db        ;; <-- register an event handler
 :request-it        ;; <-- the event id
 (fn                ;; <-- the handler function
   [db _]

    ;; kick off the GET, making sure to supply a callback for success and failure
   (GET
     "https://opentdb.com/api.php?amount=10&difficulty=easy"
     {:handler       #(re-frame/dispatch [:process-response %1])   ;; <2> further dispatch !!
      :error-handler #(re-frame/dispatch [:bad-response %1])})     ;; <2> further dispatch !!

     ;; update a flag in `app-db` ... presumably to cause a "Loading..." UI 
   (assoc db :loading? true)))    ;; <3> return an updated db 

(defn current-question [db]
  (nth (:data db) (:index db)))

(re-frame/reg-event-db
 :get-question
(fn [db] 
  (re-frame/dispatch [:request-it])
  (assoc (dissoc db :answer)
         :index 0
         :score 0
         :status :start)))

(re-frame/reg-event-db
 :next-question
 (fn [db] 
   (if (>= (:index db) 9)
     (assoc db :status :stop)
     (assoc (dissoc db :answer)
          :index (inc (:index db))))))

(re-frame/reg-event-db 
 :sumbit-answer
 (fn
   [db [_ attempted-answer]]
   (let [correct-answer (:correct-answer (current-question db))
         correct?       (= attempted-answer correct-answer)]
     (cond-> (assoc db :answer (if correct?
                                 "Right Answer"
                                 (str "Incorrect answer, it is "  correct-answer)))
       correct? (assoc :score (inc (:score db))))
     )))
