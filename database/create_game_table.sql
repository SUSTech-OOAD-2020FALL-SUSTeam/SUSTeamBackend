DROP TABLE IF EXISTS game, game_version;

CREATE TABLE IF NOT EXISTS game
(
    game_id      INT(11) NOT NULL AUTO_INCREMENT,
    game_name    VARCHAR(255) NOT NULL,
    price        DOUBLE NOT NULL,
    publish_date DATE NOT NULL,
    author_id    INT(11) NOT NULL,
    description  VARCHAR(255),

    PRIMARY KEY (game_id),
    UNIQUE (game_name)
);

CREATE TABLE IF NOT EXISTS game_version
(
    game_id         INT(11) NOT NULL,
    version_name    VARCHAR(255) NOT NULL,
    url             VARCHAR(255) NOT NULL,

    PRIMARY KEY (game_id, version_name),
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);

