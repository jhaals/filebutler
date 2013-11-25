(ns filebutler.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response]
            [ring.middleware.json :as middleware]
            [ring.middleware.multipart-params :as paramsmiddleware]
            [clojure.java.io :as io]
            [filebutler.model :as q]))

;; Storage patch for files
(def storage
  "resources/files/")

;; [todo] - let apache/nginx serve file in production
(defn serve_file [id]
  (let [filename (:filename (q/file id))]
  (response/file-response (str storage id "/" filename))))

(defn upload [request]
  "Upload file"
  (cond
    (empty? (:filename (:file request)))
    {:staus 400 :body "No file found"}
    (empty? (:token request))
    {:status 400 :body "missing data: token"}
    (not (q/token (:token request)))
    {:status 401 :body "Invalid api token"}
    :else
    (do
      (let [id (q/store_file_data
                 {:filename (:filename (:file request))
                  :owner (:token request)})]
        (.mkdir (java.io.File. storage id))
        (io/copy
          (:tempfile (:file request))
          (io/file
            (str storage id "/" (:filename (:file request)))))
        {:status 200
         :body (str "http://localhost:3000/file/" id)}))))

(defn admin?
  "Verify if token has admin permissions"
  [token]
  (let [result (q/token token)]
    (cond
      (nil? (:admin result))
      false
      (false? (:admin result))
      false
      :else
      true)))

(defn get_token [token authtoken]
  "Get token from database if authtoken is admin"
  (if (admin? authtoken)
    (let [response (q/token token)]
      (if (nil? response)
      {:status 404 :body "No token found"}
      {:status 200 :body response}))

    {:status 401 :body "authorization failed"}))

(defn create_token
  "Create new token"
  [token admin]
  (if (admin? token)
    {:status 200 :body (q/create_token admin)}
    {:status 401 :body "Authorization failed"}))


(defroutes app-routes
  ;; Database and first token setup
  ;; (GET "/teardown" [] (q/teardown))
  ;; (GET "/setup" [] (q/setup)) ; [todo] - setup should only work once
  (GET "/file/:id" [id] (serve_file id))
  (POST "/file" {params :params} (upload params))
  ;; [todo] - implement delete file
  (DELETE "/file/:id" [id] "deleting")
  ;; [todo] - implement list files
  (POST "/files" [token] "files")

  ; api token routes
  (POST "/token/:id" [id token] (get_token id token))
  (POST "/token" [token admin] (create_token token admin))
  (DELETE "/token/:deltoken" [deltoken] {:status 200 :body "to be continued"})

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-params)
      (middleware/wrap-json-body)
      (paramsmiddleware/wrap-multipart-params)
      (middleware/wrap-json-response)))

