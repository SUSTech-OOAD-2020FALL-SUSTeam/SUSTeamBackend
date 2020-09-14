DROP TABLE IF EXISTS user, user_roles;

CREATE TABLE user
(
    username    VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    avatar      VARCHAR(255),

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
