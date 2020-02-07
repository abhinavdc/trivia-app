(ns trivia-app.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 :time-color
 (fn [db _]
   (:time-color db)))

(re-frame/reg-sub
 :loading?
 (fn [db _]
   (:loading? db)))

(re-frame/reg-sub
 :answer
 (fn [db _]
   (:answer db)))

(re-frame/reg-sub
 :data
 (fn [db _]
   (:data db)))

(re-frame/reg-sub
 :current-question-index
 (fn [db _]
   (:current-question-index db)))