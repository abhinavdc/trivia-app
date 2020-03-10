(ns trivia-app.views
  (:require
   [re-frame.core :as re-frame]
   [trivia-app.subs :as subs]))


(defn loader []
  [:div {:class "loader"}])

(defn question-options [_]
  (let [answer- (re-frame/subscribe [:answer])]
    (fn [{:keys [options]}]
      (into [:ul]
            (map (fn [v]
                   [:li {:on-click (when-not @answer-
                                     #(re-frame/dispatch [:sumbit-answer v]))}
                    v])
                 options)))))

(defn buttons [{:keys [status loading?]}]
  [:div 
  (if (and (= status :start) (not loading?))
    [:button {:id       "submit-button"
              :on-click #(re-frame/dispatch [:status :stop])} "Quit"]
    [:button {:id       "submit-button"
              :on-click #(re-frame/dispatch [:get-question])} (if (= status :stop) "Play Again" "Play")])
  (when (= status :start)
      [:button {:id       "next-question-button"
                :on-click #(re-frame/dispatch [:next-question])} "Next Question"])
   ])

(defn result [score]
  [:div {:id "quiz-results"}
   [:p {:id "quiz-results-message"}
    (if (< score 3)
      "You should try little harder"
      (if (> score 8)
        "Take a bow Trivia Guru"
        "You did good"))]
   [:p {:id "quiz-results-score"} (str "Your got " score " /10" " questions correct")]])



(defn main-panel []
  (let [loading-     (re-frame/subscribe [:loading?])
        data-        (re-frame/subscribe [:data])
        index-        (re-frame/subscribe [:index])
        score-        (re-frame/subscribe [:score])
        status-       (re-frame/subscribe [:status])
        answer-       (re-frame/subscribe [:answer])
        cur-question- (nth @data- @index-)]
    [:div
     [:div {:id "quiz"}
      [:h1 {:id "quiz-name"} "Trivia"]
      [:div {:id "question"}
       (when @loading- [loader])
       (when (= @status- :initial) [:h2 "Test your Trivia quotient. Press 'Play' to begin."])
       [:h2 (:question cur-question-)]
       [question-options cur-question-]
       [:h3 @answer-]]
      [buttons {:status   @status-
                :loading? @loading-}]
      (when (= @status- :stop)
        [result @score-])]]))

