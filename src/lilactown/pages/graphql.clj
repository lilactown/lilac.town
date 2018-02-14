(ns lilactown.pages.graphql)

(defn query [query]
  query)

(= (query {}) "{}")

(= (query [:foo]) "{ foo }")

(= (query {:foo [:bar]}) "{ foo { bar } }")

(= (query {:foo [:bar :baz]}) "{ foo { bar baz } }")

(= (query {:foo {:bar {{:id 1} [:baz]}}}) "{ foo { bar(id: 1) { baz } } }")

(= (query {:foo [:bar {:baz [:asdf]}]}) "{ foo { bar baz { asdf } } }")
