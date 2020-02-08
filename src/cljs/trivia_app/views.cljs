(ns trivia-app.views
  (:require
   [re-frame.core :as re-frame]
   [trivia-app.subs :as subs]
   [goog.string :as gstring]))

(defn get-question []
  (re-frame/dispatch [:data nil])
  (re-frame/dispatch [:request-it])
  (re-frame/dispatch [:index 0])
  (re-frame/dispatch [:score 0])
  (re-frame/dispatch [:status :start])
  (re-frame/dispatch [:answer ""]))

(defn loader []
  [:div {:class "loader"}])


(defn lock-answer [answer correct_answer]
  (let [score          (re-frame/subscribe [:score])
        current-answer (re-frame/subscribe [:answer])]
    (if (= @current-answer "")
      (if (= answer correct_answer)
        (do
          (re-frame/dispatch [:answer "Right Answer"])
          (re-frame/dispatch [:score (+ @score 1)]))
        (re-frame/dispatch [:answer (str "Incorrect answer, it is " (gstring/unescapeEntities correct_answer))])) nil)))

(defn parse-question [data index]
  (if (some-> data) (gstring/unescapeEntities (get (nth data index) "question")) nil))

(defn parse-options [data index]
  (if (some-> data)
    (for [item
          (shuffle (conj (get (nth data index) "incorrect_answers") (get (nth data index) "correct_answer")))]
      [:label {:on-click #(lock-answer item (get (nth data index) "correct_answer"))} (gstring/unescapeEntities item)])
    nil))

(defn stop-quiz []
  (re-frame/dispatch [:status :stop]))

(defn next-question [index]
  (if (>= index 9)
    (stop-quiz)
    (do
      (re-frame/dispatch [:answer ""])
      (re-frame/dispatch [:index (+ index 1)]))))

(defn main-panel []
  (let [loading (re-frame/subscribe [:loading?])
        data    (re-frame/subscribe [:data])
        index   (re-frame/subscribe [:index])
        score   (re-frame/subscribe [:score])
        status  (re-frame/subscribe [:status])
        answer  (re-frame/subscribe [:answer])]
    [:div
     [:div {:id "quiz"}
      [:h1 {:id "quiz-name"} "Trivia Quiz"]
      [:div {:id "question"}
       (if @loading (loader) nil)
       (if (= @status :initial) [:h2 "Welcome to Trivia Quiz. Press 'Play' to begin."] nil)
       [:h2 (parse-question @data @index)]
       (parse-options @data @index)
       [:h3 @answer]]
      (if (and (= @status :start) (not @loading))
        [:button {:id       "submit-button"
                  :on-click #(stop-quiz)} "Quit"]
        [:button {:id       "submit-button"
                  :on-click #(get-question)} (if (= @status :stop) "Play Again" "Play")])
      (if (= @status :start)
        [:button {:id       "next-question-button"
                  :on-click #(next-question @index)} "Next Question"]
        nil)

      (if (= @status :stop)
        [:div {:id "quiz-results"}
         [:p {:id "quiz-results-message"}
          (if (< @score 5)
            "You should try little harder"
            "You did great")]
         [:p {:id "quiz-results-score"} (str "Your got " @score " /10" " questions correct")]]
        nil)]]))

