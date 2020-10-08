DROP TABLE IF EXISTS user, user_roles, game, game_version, comment, storage, game_image, game_tag, announcement;

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
    introduction VARCHAR(255),
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

CREATE TABLE IF NOT EXISTS `storage`
(
    uuid        VARCHAR(255) NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    uploader    VARCHAR(255) NOT NULL,
    upload_time DATETIME(3)  NOT NULL,
    is_public   BOOLEAN      NOT NULL,

    PRIMARY KEY (uuid),
    FOREIGN KEY (uploader) REFERENCES user (username)
);

CREATE TABLE IF NOT EXISTS `game_image`
(
    game_id INT          NOT NULL,
    url     VARCHAR(255) NOT NULL,
    type    CHAR(1)      NOT NULL,

    INDEX (game_id, type),
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);

CREATE TABLE IF NOT EXISTS `game_tag`
(
    game_id INT          NOT NULL,
    tag     VARCHAR(255) NOT NULL,

    PRIMARY KEY (game_id, tag),
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);

CREATE TABLE IF NOT EXISTS `announcement`
(
    game_id       INT          NOT NULL,
    announce_time DATETIME(3)  NOT NULL,
    title         VARCHAR(255) NOT NULL,
    content       VARCHAR(4095) NOT NULL,

    PRIMARY KEY (game_id, title),
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);

INSERT INTO game (game_id, name, price, publish_date, author, introduction)
VALUES (1,
        '十三机兵防卫圈',
        448,
        '2020-03-19T00:00:01.000',
        'admin',
        '穿越时代相遇的十三名少男少女搭乘名为「机兵」的巨大机器人，面对关乎人类存亡的最后一战。');

INSERT INTO game_image(game_id, url, type)
VALUES (1, 'A5161ACAB07F7940B967FE24999895C8.jpg', 'F'),
       (1, '5A124BEF0DC68A3FC209F972C9DE8713.jpg', 'C');

INSERT INTO game_tag(game_id, tag)
VALUES (1, '模拟'),
       (1, '视觉小说'),
       (1, '科幻'),
       (1, '动漫');

INSERT INTO game (game_id, name, price, publish_date, author, introduction)
VALUES (2,
        '巫师3：狂猎',
        127,
        '2015-05-18T00:00:01.000',
        'admin',
        '杰洛特寻求改变自己的生活，着手于新的个人使命，而世界的秩序也在悄然改变。');

INSERT INTO game_image(game_id, url, type)
VALUES (2, '2BBFDD261CBBA68B2449ADBAA9125C22.jpg', 'F'),
       (2, 'C6041EEB233D2D76BADB5502126448AC.jpg', 'C');

INSERT INTO game_tag(game_id, tag)
VALUES (2, '角色扮演'),
       (2, '冒险'),
       (2, '动作');

INSERT INTO game (game_id, name, price, publish_date, author, introduction)
VALUES (3,
        '星露谷物语',
        48,
        '2016-02-26T00:00:01.000',
        'admin',
        '你能适应这小镇上的生活并且将杂草丛生的老旧农场变成一个繁荣的家吗？');

INSERT INTO game_image(game_id, url, type)
VALUES (3, '9C02C8CF4D73F30B3730936E6A8FCCA2.jpg', 'F'),
       (3, 'E460BAAD9A38BA4706FD1062C81677A6.jpg', 'C');

INSERT INTO game_tag(game_id, tag)
VALUES (3, '模拟'),
       (3, '角色扮演'),
       (3, '休闲');

INSERT INTO game (game_id, name, price, publish_date, author, introduction)
VALUES (4,
        '求生之路2',
        37,
        '2009-11-16T00:00:01.000',
        'admin',
        '这个游戏将带领玩家和好友穿过美国南部的城市、沼泽和墓地，从萨凡纳到新奥尔良，沿途经过五个漫长的战役。');

INSERT INTO game_image(game_id, url, type)
VALUES (4, '33C60B77C76CABF696EC2701C76E13B0.jpg', 'F'),
       (4, '6BD81A2F26C9B1D627C338C8AE7770AB.jpg', 'C');

INSERT INTO game_tag(game_id, tag)
VALUES (4, '射击'),
       (4, '冒险'),
       (4, '动作'),
       (4, '合作');

INSERT INTO game (game_id, name, price, publish_date, author, introduction)
VALUES (5,
        'NBA 2K20',
        199,
        '2019-09-05T00:00:01.000',
        'admin',
        '《NBA 2K20》将以突破性的游戏模拟以及无与伦比的玩家控制重新定义运动游戏的可能性。');

INSERT INTO game_image(game_id, url, type)
VALUES (5, '62E6C776DEA60147E360A675E934A234.jpg', 'F'),
       (5, '78D8A6DCF9176ACE8C557CD8EECC98DD.jpg', 'C');

INSERT INTO game_tag(game_id, tag)
VALUES (5, '模拟'),
       (5, '体育');

INSERT INTO game (game_id, name, price, publish_date, author, introduction)
VALUES (6,
        '侠盗猎车手5',
        119,
        '2015-04-13T00:00:01.000',
        'admin',
        '当一个街头骗子、一个银行劫匪和一个精神病患者陷入困境时，他们必须在这个他们谁也不能相信的城市中生存下去。');

INSERT INTO game_image(game_id, url, type)
VALUES (6, 'EC71D1F8A1C03765856CFD0069A370F9.jpg', 'F'),
       (6, '91B9FF64E84F3B9C42F9EDE45A422AD0.jpg', 'C');

INSERT INTO game_tag(game_id, tag)
VALUES (6, '动作'),
       (6, '射击'),
       (6, '冒险');

INSERT INTO game (game_id, name, price, publish_date, author, introduction)
VALUES (7,
        'Minecraft',
        165,
        '2011-11-18T00:00:01.000',
        'admin',
        '探索无限世界，建造包括了从最简单的家园到最宏伟的城堡的一切。');

INSERT INTO game_image(game_id, url, type)
VALUES (7, '22268973DF2A38124DBF4F6832E4F0D9.jpg', 'F'),
       (7, 'E34652CB2BD5ABC9D5ECD9DED7EDBB4E.jpg', 'C');

INSERT INTO game_tag(game_id, tag)
VALUES (7, '沙盒'),
       (7, '冒险');

INSERT INTO game (game_id, name, price, publish_date, author, introduction)
VALUES (8,
        'Deemo -Reborn-',
        35,
        '2020-09-03T00:00:01.000',
        'admin',
        '雷亚首款次时代主机游戏，延续《DEEMO》的经典探索元素，透过生动逼真的3D画面，呈现音乐游玩与冒险解谜并行的丰富内容。');

INSERT INTO game_image(game_id, url, type)
VALUES (8, '2620700EACFA26A6396424B10809FE62.jpg', 'F'),
       (8, '5CD577EBF54927C87FFBBFA5116F6220.jpg', 'C');

INSERT INTO game_tag(game_id, tag)
VALUES (8, '解密'),
       (8, '节奏'),
       (8, '冒险');

INSERT INTO game (game_id, name, price, publish_date, author, introduction)
VALUES (9,
        'Muse Dash',
        18,
        '2019-06-19T00:00:01.000',
        'admin',
        '选择个性可爱的小姐姐，穿过各式各样童话般的场景，跟随动次打次的音乐节拍，踹飞迎面而来的蠢萌敌人吧！');

INSERT INTO game_image(game_id, url, type)
VALUES (9, '196129816DBE0D4F16E9AB90A6B44639.jpg', 'F'),
       (9, '2F002A3FED69F4F4EB461E2BDBB1E5C4.jpg', 'C');

INSERT INTO game_tag(game_id, tag)
VALUES (9, '音乐'),
       (9, '节奏'),
       (9, '休闲'),
       (9, '动漫');
