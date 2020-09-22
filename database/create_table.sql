DROP TABLE IF EXISTS user, user_roles, game, game_version;

CREATE TABLE user
(
    username    VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    avatar      VARCHAR(255),
    balance     INT DEFAULT 0, -- Stored in Cents

    PRIMARY KEY (username)
);

CREATE TABLE IF NOT EXISTS user_roles
(
    username VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,

    PRIMARY KEY (username, role),
    FOREIGN KEY (username) REFERENCES user (username)
);

# default password for admin: 123456
INSERT INTO user (username, password)
VALUES ('admin',
        '$sha512$$58255bd09ab4938bfdfa636fe1a3254be1985762f2ccef2556d67998c9925695$ujJTh2rta8ItSm/1PYQGxq2GQZXtFEq1yHYhtsIztUi66uaVbfNG7IwX9eoQ817jy8UUeX7X3dMUVGTioLq0Ew==');

INSERT INTO user_roles
VALUES ('admin', 'admin');

CREATE TABLE IF NOT EXISTS game
(
    game_id      INT          NOT NULL AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    price        INT          NOT NULL,
    publish_date DATETIME(0)  NOT NULL,
    author       VARCHAR(255) NOT NULL,
    description  VARCHAR(4095),

    PRIMARY KEY (game_id),
    UNIQUE (name),
    FOREIGN KEY (author) REFERENCES user (username)
);

CREATE TABLE IF NOT EXISTS game_version
(
    game_id INT          NOT NULL,
    name    VARCHAR(255) NOT NULL,
    url     VARCHAR(255) NOT NULL,

    UNIQUE (url),
    PRIMARY KEY (game_id, name),
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);

CREATE TABLE IF NOT EXISTS `comment`
(
    username     VARCHAR(255) NOT NULL,
    game_id      INT          NOT NULL,
    comment_time DATETIME(3)  NOT NULL,
    content      VARCHAR(255) NOT NULL,
    score        INT          NOT NULL,

    PRIMARY KEY (username, game_id), -- Each user can comment a game ONLY ONCE
    INDEX (game_id),
    FOREIGN KEY (username) REFERENCES user (username),
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);
