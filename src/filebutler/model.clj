(ns filebutler.model
  (:require [bitemyapp.revise.connection :refer [connect close]]
            [bitemyapp.revise.query :as r]
            [bitemyapp.revise.core :refer [run run-async]]))

(defn token
  "Get token from database"
  [id]
  (let [conn (connect)]
    (-> (r/db "filebutler")
        (r/table-db "tokens")
        (r/get id)
        (run conn)
        (:response)
        (first))))

(defn create_token
  "Create API token in database"
  [admin]
  (let [conn (connect)]
    (let [response (-> (r/db "filebutler")
                       (r/table-db "tokens")
                       (r/insert {:admin (true? admin)})
                       (run conn)
                       (:response))]
      (-> (first response)
          (:generated_keys)
          (first)))))

(defn file
  "Get file from database based on id"
  [id]
  (let [conn (connect)]
    (-> (r/db "filebutler")
        (r/table-db "files")
        (r/get id)
        (run conn)
        (:response)
        (first))))

(defn store_file_data
  "Store file information in database"
  [data]
  (let [conn (connect)]
    (let [response (-> (r/db "filebutler")
                       (r/table-db "files")
                       (r/insert [data])
                       (run conn)
                       (:response))]
      (-> (first response)
          (:generated_keys)
          (first)))))

;; [todo] - this should only work once
(defn setup []
  "setup database"
  (let [conn (connect)]
    (-> (r/db-create "filebutler") (run conn))
    (-> (r/db "filebutler") (r/table-create-db "files") (run conn))
    (-> (r/db "filebutler") (r/table-create-db "tokens") (run conn)))
  {:status 200 :body (create_token true)})

;; [todo] - implement
(defn teardown [])
