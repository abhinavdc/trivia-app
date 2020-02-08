(ns trivia-app.events
  (:require
   [ajax.core :refer [GET POST]]
   [re-frame.core :as re-frame]
   [trivia-app.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 :time-color-change            ;; usage:  (rf/dispatch [:time-color-change 34562])
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
   (-> db
       (assoc :loading? false) ;; take away that "Loading ..." UI 
       (assoc :data (js->clj (get response "results")))))  ;; fairly lame processing
   )  ;; fairly lame processing

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
     "https://opentdb.com/api.php?amount=10"
     {:handler       #(re-frame/dispatch [:process-response %1])   ;; <2> further dispatch !!
      :error-handler #(re-frame/dispatch [:bad-response %1])})     ;; <2> further dispatch !!

     ;; update a flag in `app-db` ... presumably to cause a "Loading..." UI 
   (assoc db :loading? true)))    ;; <3> return an updated db 