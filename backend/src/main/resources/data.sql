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
    genres (name)
VALUES
    ('Fantasy') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Przygodowy') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Komedia') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Biograficzny') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Familijny') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Muzyczny') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Kryminalny') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Animacja') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Historyczny') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Wojenny') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    genres (name)
VALUES
    ('Sportowy') ON CONFLICT (name) DO NOTHING;

INSERT INTO
    users (nickname, email, password, age, rank, is_verified)
VALUES
    (
        'administrator',
        's092806@student.tu.kielce.pl',
        '$argon2id$v=19$m=65536,t=10,p=1$NyvOOXAAFrXPJmheRtXFIw$5GuaZfvyd+fPbY/3oIfK1NOIPwav7qhO3olvCS1Uaq0',
        30,
        1,
        false
    ) ON CONFLICT (nickname) DO NOTHING;
