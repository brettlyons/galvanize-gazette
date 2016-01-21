-- name: create-user!
-- creates a new user record
-- INSERT INTO users
-- (id, first_name, last_name, email, pass)
-- VALUES (:id, :first_name, :last_name, :email, :pass)

-- -- name: update-user!
-- -- update an existing user record
-- UPDATE users
-- SET first_name = :first_name, last_name = :last_name, email = :email
-- WHERE id = :id

-- -- name: get-user
-- -- retrieve a user given the id.
-- SELECT * FROM users
-- WHERE id = :id

-- -- name: delete-user!
-- -- delete a user given the id
-- DELETE FROM users
-- WHERE id = :id

-- name: get-stories
-- get the table of stories from the db
SELECT * FROM stories

-- name: get-story
-- get a single story based on id
SELECT * FROM story
WHERE id = :id

-- name: create-story!
-- add a story to the db
INSERT INTO stories
(id, title, link, imageurl, summary, dayOfCommit)
VALUES (DEFAULT, :title, :link, :imageurl, :summary, now())

-- name: create-opinion!
-- add an opinion to the db
INSERT INTO opinions
(id, story, content)
VALUES (DEFAULT, :story_id, :content)

-- name: get-opinions
-- get opinions from the db
SELECT * FROM opinions
WHERE story = :story_id
