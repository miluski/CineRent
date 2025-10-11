INSERT INTO
    genres (name)
VALUES
    ('Science-Fiction') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Akcja') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Dramat') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Horror') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Thriller') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    users (nickname, password, age, rank)
VALUES
    (
        'administrator',
        '$argon2id$v=19$m=65536,t=10,p=1$NyvOOXAAFrXPJmheRtXFIw$5GuaZfvyd+fPbY/3oIfK1NOIPwav7qhO3olvCS1Uaq0',
        30,
        1
    ) ON CONFLICT (nickname) DO NOTHING;
