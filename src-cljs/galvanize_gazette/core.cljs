(ns galvanize-gazette.core
(:require [reagent.core :as r :refer [atom]]
          [reagent.session :as session]
          [secretary.core :as secretary :include-macros true]
          [goog.events :as events]
          [clojure.string :as string]
          [goog.history.EventType :as HistoryEventType]
          [markdown.core :refer [md->html]]
          [ajax.core :refer [GET POST]])
(:import goog.History))

;; (def AJAX-ADDRESS "http://limitless-refuge-7694.herokuapp.com/api/story")
(def AJAX-ADDRESS "http://localhost:3000/api/story")

(defn navbar []
  [:div.row
   [:div.col-md-12 {:style {:background-image "url(img/devils-tower.jpg)"
                            :background-size "100%"
                            :height "200px"
                            :width "100%"}}
    [:p]
    [:div.container
     [:div.row
      [:a {:href "/" :style {:color "black"}}
       [:div.col-md-5 {:style {:margin "25px 0 25px 0" :opacity "0.7" :border-style "solid" :border-color "black" :border-width "2px" :background-color "white"}}
        [:div.row.h1
         [:div.col-md-12 "Galvanize Gazette"]]
        [:div.row.h3
         [:div.col-md-12 "All The News Thats Fit To Link"]]]]]]]])

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     "this is the story of galvanize-gazette... work in progress"]]])

(def app-db (r/atom {:opinions ["I loved it.  Very happy." "What a piece of junk, not very happy" "I suppose it is little comfort that I picked a sort of *hard mode*, at least it's not far from completion"]}))

(defn post-story []
  (POST AJAX-ADDRESS :params {:title (get-in @app-db [:add-story (str "Title")]) 
                              :link (get-in @app-db [:add-story "Link"])
                              :imageurl (get-in @app-db [:add-story "Image URL"])
                              :summary (get-in @app-db [:add-story "summary"])}
        :handler #(println "POST SUCCESFUL!::: " % @app-db)
        :error-handler #(println "POST (un?)SUCCESFUL!::: " %)))

(defn get-stories []
  (GET AJAX-ADDRESS
       {:headers {"Accept" "application/transit+json"}
        :error-handler (fn [err] (println "GET ERROR" err))
        :handler (fn [e] 
                   (swap! app-db assoc-in [:stories] e))}))

(defn add-story-form  [label type]
  (let [id (str "add-story-" label) type (or type "text") input-info (r/atom "")]
    (get-stories)
    (fn []
      ;; (println app-db)
      ;; (println (get-in @app-db [:add-story "Image URL"]))
      [:div.form-group.form-group-md
       [:div.col-md-2
        [:label {:for id :style {:font-size "18px"}} (str label ": ")]]
       [:div.col-md-4
        [:input.form-control {:type type
                              :id id
                              :value @input-info
                              :on-change (fn [e] (reset! input-info (.-target.value e))
                                           (swap! app-db assoc-in [:add-story label] @input-info))}]]])))

(defn story-summary [story]
  [:div.col-md-4
   [:a {:href (str "#/stories/" (:id story)) :on-click #(swap! app-db assoc-in [:stories-info-page] story)}
    [:div {:style {:width "100%" :height "200px" :background-size "100%"
                   :background-image (str "url(" (:imageurl story) ")")}}]]
   [:div.h1 [:a {:href (str "#/stories/" (:id story))
                 :on-click #(swap! app-db assoc-in [:stories-info-page] story)} (:title story)]]])


(defn home-page []
  (get-stories)
  (let [hide-form (r/atom false) input-info (r/atom "") todays-stories (get-in app-db [:stories])]
    (fn []
      [:div.container
       [:div.row
        [:a.pull-right {:on-click #(swap! hide-form not)} (str (if @hide-form "Show" "Hide"))]
        [:div.col-md-12 {:hidden @hide-form}
         [:div.h1 "Add A Story"]
         [:br]
         [:br]
         [:form.form-horizontal 
          [add-story-form "Title" "text"]
          [add-story-form "Link" "url"]
          [add-story-form "Image URL" "url"]
          [:div.form-group.form-group-md
           [:div.col-md-2
            [:label {:for "add-story-summary" :style {:font-size "18px"}} (str "Summary:" " ")]]
           [:div.col-md-6
            [:textarea.form-control {:rows "4" :id "add-story-summary"
                                     :on-change (fn [e] (reset! input-info (.-target.value e))
                                                  (swap! app-db assoc-in [:add-story "summary"] @input-info))}]]]
          [:div.col-md-offset-2
           [:button.btn.btn-lg.btn-primary {:on-click (fn [e]
                                                        (post-story)
                                                        (get-stories))} "Link it up!"]]
          ]]]
       [:div.row
        [:div.col-md-12.h1 "Today's News"]]
       [:div.row
        (for [story (get-in @app-db [:stories])]
          ^{:key (:id story)}[story-summary story])]])))

(def opinions ["I loved it.  Very happy." "What a pieceof junk, not very happy"])

(defn stories-page []
  (println (get-in @app-db [:stories-info-page]))
  (println "stories" (get-in @app-db [:stories-info-page :link]))
  ;; (swap! app-db assoc-in [:story-by-id (get-in [:stories :id] @app-db)])
  
  ;; (println (str "PATHNAME? " js/window.location))
  (let [id (get-in @app-db [:story-page-id]) story (get-in @app-db [:stories-info-page]) input-info (r/atom "")]
    (fn []
      [:div.container
       ;;[:div.h1 (get-in @app-db [:story-page-id])]
       [:div.row
        [:div.col-md-12.h1 (:title story)]]
       [:div.row
        [:div.col-md-4 
         [:div {:style {:width "100%" :height "200px" :background-size "100%"
                        :background-image (str "url(" (:imageurl story) ")")}}]]
        [:div.col-md-8.h2 (:summary story)]
        [:p]
        [:div.col-md-8.h2
         [:a {:href (:link story)} "View Site"]]]
       [:div.row
        [:div.col-md-7.h1 "Opinions"] [:div.col-md-5.h1 "Expert Analysis"]]
       [:div.row
        [:div.col-md-6
         [:div.form-group.form-group-md
          [:label {:for "opinion" :style {:font-size "18px"}} "Whats your opinion?"]
          [:textarea.form-control {:rows "2" :id "opinion"
                                   :on-change (fn [e] (reset! input-info (.-target.value e)))}]
          [:p]
          [:button.btn.btn-md.btn-primary {:on-click (fn [e]
                                                       (println (get-in @app-db [:opinions]))
                                                       (swap! app-db assoc-in [:opinions] (cons @input-info (get-in @app-db [:opinions]))))}
           "Opine"]]]
        [:div.col-md-offset-4
         [:div.col-md-7
          [:div.col-md-offset-4
           [:div.col-md-8.h2 [:ul [:li "word (n)"]
                              [:li "word (n)"]
                              [:li "word (n)"]
                              [:li "word (n)"]
                              [:li "word (n)"]
                              [:li "word (n)"]
                              [:li "word (n)"]
                              [:li "word (n)"]
                              [:li "word (n)"]
                              [:li "word (n)"]
                              [:li "word (n)"]]]]]]
        [:p]
        [:div.col-md-6
         (for [opinion (get-in @app-db [:opinions])]
           [:h3 opinion])]]
       ]
      )))

(def pages
  {:home #'home-page
   :about #'about-page
   :stories #'stories-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/about" []
  (session/put! :page :about))

(secretary/defroute stories-with-id "/stories/:id" {:as params}
  (println "ROUTING HAPPEND " params)
  (swap! app-db assoc-in [:story-page-id] (:id params))
  (session/put! :page :stories))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "navbar"))
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-components))
