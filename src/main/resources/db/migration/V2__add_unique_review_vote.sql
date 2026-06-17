ALTER TABLE review_votes
    ADD CONSTRAINT uk_review_vote_review_user UNIQUE (review_id, user_id);
