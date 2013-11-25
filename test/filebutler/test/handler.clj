(ns filebutler.test.handler
(:require [clojure.test :refer :all]
                      [ring.mock.request :refer :all]
                      [filebutler.handler :refer :all]))
(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "Hello World"))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))

(defn mock-admin [token]
  (if (= token "1234")
  true
  false))

(defn mock-token [token]
  (cond
    (= token "exist")
    {:id token :admin true}
    :else
    nil))

(defn mock-create-token [admin]
  "1337")

(deftest token
  (testing "get token"
    (with-redefs [admin? mock-admin
                  filebutler.model/token mock-token]
      (let [response (app (request :post "/token/exist" {:token "1234"}))]
        (is (= (:status response) 200)))
      (let [response (app (request :post "/token/does_not_exist" {:token "1234"}))]
        (is (= (:status response) 404)))))
  (testing "create_token"
    (with-redefs [admin? mock-admin
                  filebutler.model/token mock-token
                  filebutler.model/create_token mock-create-token]
      (let [response (app (request :post "/token" {:token "1234" :admin false}))]
        (is (= (:status response) 200)))
      (let [response (app (request :post "/token" {:token "invalid" :admin false}))]
        (is (= (:status response) 401))))))

