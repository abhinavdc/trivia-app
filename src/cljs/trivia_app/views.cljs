(ns trivia-app.views
  (:require
   [re-frame.core :as re-frame]
   [trivia-app.subs :as subs]
   ))

(defn get-question []
  (re-frame/dispatch [:request-it])
  )

(defn loader [loading] 
  ([:div (if loading "true" "false")]))

(defn lock-answer [answer correct_answer] 
  (if (= answer correct_answer)
    (re-frame/dispatch [:answer "Correct"])
    (re-frame/dispatch [:answer "Incorrect"])))

(defn parse-question [data] 
  (if (some-> data) (get (first data) "question") "no questions"))

(defn parse-options [data]
  (if (some-> data)
    (for [item 
          (shuffle (conj (get (first data) "incorrect_answers") (get (first data) "correct_answer")))]
      [:li [:a {:on-click #(lock-answer item (get (first data) "correct_answer"))} item]])
  [:li "no options"]))

(defn main-panel []
  (let [name    (re-frame/subscribe [::subs/name])
        loading (re-frame/subscribe [:loading?])
        data    (re-frame/subscribe [:data])
        answer   (re-frame/subscribe [:answer])]
    [:div
     [:h1 "Hello from " @name]
     [:div "loading" (if @loading "true" "false")]
     [:p "question" (parse-question @data)]
     [:ul "options" (parse-options @data)]
     [:p "answer" @answer]
     [:button {:on-click #(get-question)} "Get Questions"]]))