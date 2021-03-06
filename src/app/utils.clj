(ns app.utils
  (:require [honey.sql :as sql]))

(defn non-positive-amount-response [amount]
  {:status 400
   :body (str "Amount must be positive. Amount sent was " amount)})

(defn product-not-found-response [name]
  {:status 404
   :body (str "Product " name " does not exist")})

(defn session-id->user-id [querier session-id]
  (let [query {:select [:user-id]
               :from [:sessions]
               :where [:= :id session-id]}
        result (querier (sql/format query))]
    (-> result
        (nth 0)
        :user_id)))

(defn current-product-amount [querier product-name]
  (let [query {:select [:amount]
               :from [:inventory]
               :where [:= :name product-name]}
        result (querier (sql/format query))]
    (-> result
        (nth 0)
        :amount)))

(defn product-exists [querier name]
  (let [query {:select [:id]
               :from [:inventory]
               :where [:= :name name]}
        result (querier (sql/format query))]
    (if (= 1 (count result))
      (-> result
          (nth 0)
          :id)
      nil)))

(defn user-exists [querier name]
  (let [query {:select [:id]
               :from [:users]
               :where [:= :name name]}
        query-result (querier (sql/format query))
        name-occurrences (count query-result)]
    (if (= 1 name-occurrences)
      (-> query-result
          (nth 0)
          :id)
      nil)))

(defn session-is-active [querier session-id]
  (let [session-is-active-query {:select [[[:count :*]]]
                                 :from [:sessions]
                                 :where [:and [:= :id session-id]
                                              [:<= [:now] :expires-on]]}
        session-is-active-result (querier (sql/format session-is-active-query))]
    (-> session-is-active-result
        (nth 0)
        :count
        (= 1))))

(defn admin-session [querier session-id]
  (let [admin-session-query {:select [[[:count :*]]]
                             :from [:sessions :admins]
                             :where [:and [:= :sessions.user-id :admins.user-id]
                                          [:= :sessions.id session-id]]}
        admin-session-result (querier (sql/format admin-session-query))]
    (-> admin-session-result
        (nth 0)
        :count
        (= 1))))
