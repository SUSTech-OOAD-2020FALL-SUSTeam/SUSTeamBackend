DROP TABLE IF EXISTS user, user_roles, game, game_version, comment, storage, game_image, game_tag, announcement, `order`, game_save, relationship, achievement, user_achievement_progress;

CREATE TABLE user
(
    username    VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    mail        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    avatar      VARCHAR(255),
    balance     INT DEFAULT 0, -- Stored in Cents

    PRIMARY KEY (username),
    UNIQUE (mail)
);

CREATE TABLE IF NOT EXISTS user_roles
(
    username VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,

    PRIMARY KEY (username, role),
    FOREIGN KEY (username) REFERENCES user (username)
);

# default password for admin: 123456
INSERT INTO user (username, password, mail)
VALUES ('admin',
        '$sha512$$58255bd09ab4938bfdfa636fe1a3254be1985762f2ccef2556d67998c9925695$ujJTh2rta8ItSm/1PYQGxq2GQZXtFEq1yHYhtsIztUi66uaVbfNG7IwX9eoQ817jy8UUeX7X3dMUVGTioLq0Ew==',
        'admin@susteam.com');

# default password for test001: test001
INSERT INTO user (username, password, mail)
VALUES ('test001',
        '$sha512$$2e40a83fe4eea63f1f30bc2d1425aa69b4b09bf614c19ce5db0d04daed1f2362$JqOOMgATFHFhi0CMGSHqa0JCqhOZ5UOEm3d+e6XsvfjUCe2TRd8JhbYLhNE1CIDFDuyiWgvX5KoNrwRhUq3J+A==',
        'test001@susteam.com');

INSERT INTO user_roles
VALUES ('admin', 'admin');

INSERT INTO user_roles
VALUES ('admin', 'developer');

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
    game_id     INT          NOT NULL,
    upload_time DATETIME(3)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    url         VARCHAR(255) NOT NULL,

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
    game_id       INT           NOT NULL,
    announce_time DATETIME(3)   NOT NULL,
    title         VARCHAR(255)  NOT NULL,
    content       VARCHAR(4095) NOT NULL,

    PRIMARY KEY (game_id, title),
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);

CREATE TABLE IF NOT EXISTS `order`
(
    order_id      INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(255) NOT NULL,
    game_id       INT          NOT NULL,
    status        VARCHAR(255) NOT NULL,
    purchase_time DATETIME(3)  NOT NULL,
    price         INT          NOT NULL,

    PRIMARY KEY (order_id),
    FOREIGN KEY (username) REFERENCES user (username),
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);

CREATE TABLE IF NOT EXISTS `game_save`
(
    username   VARCHAR(255) NOT NULL,
    game_id    INT          NOT NULL,
    save_name  VARCHAR(255) NOT NULL,
    saved_time DATETIME(3)  NOT NULL,
    url        VARCHAR(255) NOT NULL,

    PRIMARY KEY (username, game_id, save_name),
    FOREIGN KEY (username) REFERENCES user (username),
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);

CREATE TABLE IF NOT EXISTS relationship
(
    user1  VARCHAR(255) NOT NULL,
    user2  VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,

    FOREIGN KEY (user1) REFERENCES user (username),
    FOREIGN KEY (user2) REFERENCES user (username)
);

CREATE TABLE IF NOT EXISTS achievement
(
    achievement_id   INT          NOT NULL AUTO_INCREMENT,
    game_id          INT          NOT NULL,
    achievement_name VARCHAR(255) NOT NULL,
    description      VARCHAR(255) NOT NULL,
    achieve_count    INT          NOT NULL,

    PRIMARY KEY (achievement_id),
    UNIQUE (game_id, achievement_name),
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);

CREATE TABLE IF NOT EXISTS user_achievement_progress
(
    username        VARCHAR(255) NOT NULL,
    achievement_id  INT          NOT NULL,
    rate_of_process INT          NOT NULL,
    finished        BOOLEAN      NOT NULL,

    PRIMARY KEY (username, achievement_id),
    FOREIGN KEY (username) REFERENCES user (username),
    FOREIGN KEY (achievement_id) REFERENCES achievement (achievement_id)
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

UPDATE game
SET description = '
### 挑战全新领域，VANILLAWARE最新作登场！

ATLUS×VANILLAWARE曾创作出『公主之冕』、『奥丁领域』、『龙之皇冠』等极具特色的奇幻世界，进而树立了该类游戏领域的标杆。现在，VANILLAWARE神谷盛治全力创作的新作在此登场！体验这绝美壮丽的模拟冒险游戏吧！

### 穿越过去与未来的时空，十三位少年少女的多线共构剧本

他们为了反抗无可避免的毁灭命运，驾驶着名为「机兵」的机器人，赌上人类存亡投入最后一战。

### 13人「全部」都是主角

故事是以「13名主角」各自的观点分别进行。

看完所有人的故事就能揭开「毁灭的命运」的真相。

### 「崩坏篇」、「追忆篇」、「究明篇」分为3个部分进行。

「追忆篇」体验故事剧情，「崩坏篇」投入作战、「究明篇」回顾故事深度内容……

进行这3个部分就能更加广泛地享受『十三机兵防卫圈』地游戏乐趣！

### 「追忆篇」

##### 解开13人各自的谜团

思考在对话中搜集到的关键词，就能将其转换为不同的关键词，或是把关键词用在其他对象身上，也有可能获得新的关键词。

##### 13位主角之间的复杂关系性

每个主角都有各自关联较为密切的角色与专用章节，不断推进故事剧情，就能窥探出隐藏在各角色之间的复杂关联性，以及各种分歧点。

### 「究明篇」

##### 回顾解开的事实找出真相

档案中收录了各式各样细致架构出游戏世界观的要素。可在此重看已经看过的「事件」场景，也能以纵观全貌的方式反复浏览充满谜团的故事内容，进行各种考察。

### 「崩坏篇」

##### 驾驶「机兵」迎战世界危机

「近距离格斗型」、「万能型」、「远距离型」、「飞行支援型」，编组并驾驶这4种类「机兵」，与攻击城镇的神秘敌人「怪兽」作战。游玩方式为爽快的模拟战斗类型！
'
WHERE game_id = 1;

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

UPDATE game
SET description = '
《巫师：狂猎》是一款情节驱动的开放世界角色扮演游戏。本作的故事发生在一个令人目眩神迷的奇幻宇宙里，玩家做出的每一个选择都意义重大，同时也要面对影响深远的后果。在《巫师》这个广袤的开放世界中，您可以尽情探索商业都市、海盗岛屿、危险的山隘和被遗忘的洞窟。

### 扮演一名训练有素，收钱办事的怪物杀手

猎魔人身处的世界怪物横行。作为一支制衡力量，他们自小接受训练，通过变异来获得超人类的战斗技巧、力量和反应。

- 扮演一名职业怪物杀手，装备一系列可以升级的武器、变种魔药和战斗法术，无情地把对手大卸八块。
- 凶残的野兽徘徊于山间，狡猾的灵异掠食者潜伏在人烟稠密的城镇暗处，而您则要捕杀这些各色的奇异怪物。
- 把您赚取的奖赏用来升级武器，购买订制的盔甲，或者花在赛马、打牌、拳击，还有其它的夜间娱乐生活上。

### 探索道德沦丧的开放式奇幻世界

巫师宏大的开放世界为玩家带来无尽的冒险，并且在规模、深度和复杂性等方面树立了新的标准。

- 遍历奇幻的开放世界：探索被遗忘的废墟、洞穴和沉船，与城市里的商人和矮人工匠做买卖，在开阔的平原、山地和海洋上进行狩猎。
- 与谋反的将领、阴险的女巫还有腐败的皇室打交道，替他们去完成见不得光的危险勾当。
- 做出超越善恶的选择，直面影响深远的后果。

### 追寻预言之子

接下最为重要的一笔委托，追寻预言之子。那是一个可以拯救或是摧毁这个世界的关键。

- 根据古代精灵的预言，预言之子强大无比，是一件活生生的武器。而您则要在纷飞的战火中找到对方的下落。
- 和一心想要控制这个世界的残暴统治者、野外的精怪，乃至于来自来世的威胁斗智斗勇。
- 在这个或许并不值得拯救的世界里，左右自己的命运。

### 次世代机能的全面发挥

- 专为次世代硬件量身打造的 REDengine 3，把《巫师》的逼真奇幻世界渲染得细腻生动。
- 动态的天气系统和日夜循环，将会改变城镇居民以及野外怪兽的行为模式。
- 通过主线和支线的丰富剧情选择，玩家可以通过前所未有的方式，影响这个宏大的开放世界。
'
WHERE game_id = 2;

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

UPDATE game
SET description = '
你继承了你爷爷在星露谷留下的老旧农场。带着爷爷留下的残旧工具和几枚硬币开始了你的新生活。
你能适应这小镇上的生活并且将这个杂草丛生的老旧农场变成一个繁荣的家吗？这不是一件容易的事情。
尤其是当Joja企业进驻镇上，导致以前旧的生活方式都消失了。
交流中心这个以前举办过众多活动并充满活力的地方现在变成再也无人愿意踏进的一片废墟。
但这山谷似乎充满机会，只要做出一点奉献你就可能是会成为让星露谷重回繁荣的人之一！
'
WHERE game_id = 3;

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

UPDATE game
SET description = '
让大家翘首以盼的的《求生之路 2》（L4D2）以僵尸大灾难为背景，是 2008 年最受欢迎且屡获殊荣的合作游戏《求生之路》的续集。

这个第一人称射击恐怖合作动作游戏将带领玩家和好友穿过美国南部的城市、沼泽和墓地，从萨凡纳到新奥尔良，沿途经过五个漫长的战役。

玩家将扮演四名新生还者中的一名，装备有种类繁多、数量惊人的经典及先进武器。
除了枪支之外，玩家还有机会用各种可制造屠杀的近战武器在感染者上泄愤，例如电锯、斧头、甚至是致命的平底锅。

玩家将在对抗三种恐怖无比、令人生畏的新特殊感染者（或者在对抗模式中扮演这些新的特殊感染者）时，考验这些武器的威力， 还会遭遇五种“不寻常”的普通感染者，包括可怕的泥人。

正是 AI 总监 2.0 将《求生之路》那疯狂无比、动作场面连续不断的游戏玩法推上了新的高度。
优化后的总监系统能程序化地改变沿途天气和玩家行走的路径，还可以根据玩家的表现调整敌人的数量、效果和音效。
《求生之路 2》保证每次游戏都会带来令人满意、充满独特挑战的体验，符合玩家各自的游戏风格。

- 来自《半衰期》、《传送门》、《军团要塞》和《反恐精英》制作者的下一代合作动作游戏。
- 超过 20 种新武器和物品，超过 10 种近战武器——斧头、电锯、平底锅、棒球棍——让玩家和僵尸亲密接触。
- 全新生还者。 全新故事。 全新对白。
- 五个辽阔的战役，可以在合作、对抗和生还者模式中进行。
- 全新多人游戏模式。
- “不寻常”的普通感染者。 五个新战役的每一个都至少有一种该战役专属的“不寻常”普通感染者。
- AI 总监 2.0：被称为“AI 总监”的先进技术，推进了《求生之路》独特的游戏玩法， 根据玩家的表现自定义敌人的数量、效果和音乐。 《求生之路 2》则以 AI 总监 2.0 为特点，扩展了总监系统的能力，可以自定义关卡布局、游戏世界内的物体、天气和照明来反映一天中的不同时间。
- 统计、排名和奖励系统促使玩家们合作游戏。
'
WHERE game_id = 4;

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

UPDATE game
SET description = '
### 再下一程
经过长时间发展进化，《NBA 2K》系列俨然已成为超越篮球模拟游戏的存在。
《NBA 2K20》不但拥有顶级的图像画面与游戏性、还拥有创新的游戏模式和无与伦比的球员操纵及自定义，重新定义了体育游戏的全新可能。
此外，本作还拥有巨大的开放世界街区，如同全新上线的社交平台。玩家和球友们齐聚于此，帮助篮球文化再下一程。

本作使用升级版运动引擎，不但拥有最真实的球员控制，还特别加入招牌球风、高级投篮控制、全新花式运球系统、改良无球碰撞，以及全新“阅读和反应”防守系统。让你的技巧再下一程！

### 辉煌生涯
才华横溢且前途无量的Sheldon Candis接过导筒，呈现画面效果惊人的辉煌生涯过场动画。
Idris Elba、Rosario Dawson等现役与退役NBA全明星球员倾情出演，以焕然一新的方式呈现出引人入胜的故事情节。

### 全新街区
在新作中体验更富生机、更加活跃的街区。参加更多2K竞技活动、解锁全新炫耀摇杆动画、进行9洞飞盘高尔夫赛，并获得数量空前的专属装备。

### 提升球技与声望
公园依然是玩家提升球技和相互比拼的中央舞台。公园声望再度回归，便于大家了解谁球技高超、谁球技欠佳。
声望提升后将解锁专属物品，并能将其用于所有自创球员类型！全新改良的声望系统还带来了大量全新奖品。

### 自创球员创建器
玩家将拥有更多选择权，可以通过全新的创建器来决定自创球员的各方面潜力，其中包括选择自身专属的主宰能力。
游戏拥有超过100个模板和50个新徽章，带来无穷的选择与组合。

### 欢迎来到WNBA的世界
全部12支WNBA球队和140多位女球员首度加入游戏，可在“快速比赛”和赛季模式中进行选择。
比赛动画、战术打法和画面效果皆针对女篮单独打造。

### 梦幻球队
NBA 2K梦幻卡片收集模式。本模式含每日目标、卡片升级、全新改版三威胁、限时活动与诸多奖励，完全掌控梦幻球队的方方面面。
简化的用户体验让老手和新手均能畅快游戏。储物柜代码、排行榜、开发者小贴士、本周最佳球队等功能同时也增强了社区联动性。

我们与Steve Stoute和United Masters亲密合作，在今年的作品中呈现全球当红炸子鸡与后起之秀劲歌金曲。

### 传奇球队
新作中加入了十余支全新传奇球队。其中包括2009-10赛季的Portland Trail Blazers、2015-16赛季的Cleveland Cavaliers、2013-14赛季的San Antonio Spurs、2002-03赛季的Phoenix Suns，以及NBA历史上各个时代的十年最佳阵容。本作中共有100多支球队供玩家选择。

### 深度呈现
本作的动态直播式比赛呈现囊括了体育游戏历史上为数最多的播音员，其中有Kevin Harlan、Ernie Johnson等著名解说。
本作拥有6万余条全新对话、全新直播秀和比赛开场、自创球员访谈、纪录与里程碑，以及2000多条球馆特有的观众反应与声音效果，将为大家带来独一无二的听觉盛宴。

### 传奇经理/终极联盟
你能打造出新的王朝吗？完全掌控一支球队，从零开始打造冠军之师。
此模式中加入了全新技能树、改良的人际关系系统、模拟器自定义、改版球探系统等诸多要素。

### 2KTV——第六季
作为NBA 2K的新闻中心，由Alexis Morgan和Chris Manning主持的新一季2KTV再度回归。
它将呈现2K社区成员、NBA及WNBA球星的独家采访、直接来自开发者的最新《2K20》新闻、要点及见解，以及周度精彩片段！
'
WHERE game_id = 5;

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

UPDATE game
SET description = '
一个初涉江湖的街头新丁、一个洗手多年的银行劫匪和一个丧心病狂的杀人狂魔，误打误撞中深陷犯罪集团、美国政府和娱乐产业之间盘根错杂的恐怖困境。
他们必须齐心协力，接连完成九死一生的惊天劫案，才能在这个冷血无情的城市中苟延残喘。不要相信任何人，尤其是你的同伙！

PC 版Grand Theft Auto V 能够以超越 4K 的最高分辨率和 60 帧每秒的帧率，为您呈现屡获殊荣、令人痴迷的游戏世界——洛桑托斯市和布雷恩郡。

游戏为 PC 玩家提供了巨细无遗的独享自定义选项，包括纹理质量、着色器、曲面细分、反锯齿等超过 25 种不同设定，还有支持键鼠操控的广泛自定义功能。
其他选项包括可控制车辆和行人流量的人口密度滑杆，以及对双屏、三屏、3D 和即插即用手柄的支持。

Grand Theft Auto V 同时包含 Grand Theft Auto 在线模式，这个活力四射、瞬息万变的联网世界支持多达 30 位玩家同时进行游戏，并且囊括了自发布以来的所有游戏升级以及内容更新。
您可以凭借非法贸易发家致富，以 CEO 的身份一手打造自己的犯罪帝国；创立摩托车会以称霸街头；携手好友上演逆天抢劫任务；体验命悬一线的特技竞速；在独特的竞争模式中一展身手；更可以信马由缰，亲手为游戏创作新内容，并与全世界的 GTA 玩家共享。

PC 版 Grand Theft Auto V 和 GTA 在线模式同时提供第一视角模式，让玩家能够以全新方式探索游戏中洛桑托斯市和布雷恩郡细腻逼真、令人惊艳的种种细节。

随着 PC 版 Grand Theft Auto V 的推出，Rockstar 编辑器也一并登场。作为一套功能强大的创作工具，它能让玩家快速轻松地录制、编辑和分享在 Grand Theft Auto V 及 GTA 在线模式中的游戏影像。
Rockstar 编辑器的导演模式能让玩家使用故事模式中的主要角色、路人，甚至是动物进行搭台布景，任由玩家天马行空，随心创作。除了高级镜头控制和剪辑特效 （包括快动作和慢动作） 以及多种镜头滤镜以外，玩家还可以将游戏中的电台曲目作为配乐，或者对游戏配乐的强度进行动态控制。
影片制作完成后，可以直接从 Rockstar 编辑器上传至社交网络和 Rockstar Games Social Club，与其他玩家轻松分享您的游戏激情。

游戏原声音乐的创作者 The Alchemist 和 Oh No 同时回归游戏，作为 The Lab FM 电台的主持人。
这个电台主打二者以游戏原声音乐为灵感创作的全新独家曲目，合作艺人包括 Earl Sweatshirt、Freddie Gibbs、Little Dragon、Killer Mike、Future Islands 的 Sam Herring 等等。
此外，玩家也可以在洛桑托斯市和布雷恩郡寻幽探胜之际，通过由个人电台创建的自定义歌单，聆听属于自己的音乐。
'
WHERE game_id = 6;

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

UPDATE game
SET description = '
### 世界任您创造
准备好体验拥有无限可能的冒险，您可以尽情建造、挖掘、与生物战斗、探索千变万化的 Minecraft 景观。

### 始终有新的发现
我们对游戏进行定期更新，新的工具、地点和空间等待您的探索。检查最新更新。

### 进入创造模式
建您所想。在创造模式下，尽情发挥您的想象力并利用无限资源进行建造。

### 进入生存模式
您在生存模式下努力生存与发展时，仅需一天即可体验与生物战斗，建造避难所和探索景观。

### 与朋友同乐
与您的朋友一道建造、探寻并获得乐趣！与朋友一起探索，Minecraft 的世界变得更加绚丽多彩。

### 保持足智多谋
成为能工巧匠，充分利用周围环境收集建造材料 - 参见砍伐树木如何助您创造新的作品。

### 挺过黑夜
始终远离游走的生物以防不测 - 如果靠的太近，从来没有人知道后果。

### 建造令人惊奇的作品
发现红石粉的各种使用方法，从而提升您的创造作品，让作品栩栩如生，或让作品引入注目。
'
WHERE game_id = 7;

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

UPDATE game
SET description = '
### 全球突破2000万下载数的奇幻节奏游戏

拥有多采多姿的乐曲和美丽的图像，
让全世界为之疯狂的音乐游戏系列巅峰作『DEEMO』。
是由台湾厂商Rayark所製作的。

### 各式各样的乐曲类型，以及包含各种难度的乐曲

收录超过250首由日本和台湾的创作者孕育出的众多类型的乐曲。
为了让习惯音乐游戏的人，或是对音乐游戏较为陌生的人，
都可以配合自己的技巧，尽情享受所有乐曲带来的乐趣，
因此每首乐曲都有[Easy][Normal][Hard]３种难度可供选择。

※一部分的乐曲提供了比[Hard]更困难的[Extra]难度。
「DEEMO(for Nintendo Switch)」今后也预定会透过更新追加乐曲。

### 丰富精彩的故事

主角Deemo是在城堡内独自弹奏钢琴的神祕人物。
某天突然有位丧失记忆的少女从天而降，来到孤独的他身旁。

希望让少女回到上面的世界，而为此烦恼的Deemo。
此时他發现有株听了钢琴弹奏的乐曲后，就会成长的神奇树苗。

只要让那株树苗成长茁壮，直到天花板的话，
少女或许就能窗口回去原本的世界。

在与少女接触的过程中，逐渐了解人心温暖的Deemo，
以及取回记忆的少女，他们的结局究竟为何？

这是两人之间奇妙而又有些令人不捨的故事──
'
WHERE game_id = 8;

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

UPDATE game
SET description = '
和三位可爱的小姐姐一起修复被镜像代码篡改的世界谱吧！
这里是狂拽跑酷和传统音游结合的世界★——Muse Dash!!
是的没错
您就是我们命中注定的Master啊！
什么？！手残？喵喵喵？tan90°！
没有关系！就算不擅长动作类游戏,
也可以踏着动次打次的音乐节拍跨越重重难关哦！
选择你喜爱的卡哇伊小姐姐穿过童话般的布景⋯⋯踹爆一个又一个蠢萌蠢萌的小怪兽取得胜利吧!!!

### 游戏玩法
跟随音乐节奏通过简单的左右操作击打从空中和地面而来的敌人，小心躲避偶尔出现的障碍物！！虽然操作简单，但丰富的音乐曲目和精心设计的节奏点让游戏变得非常有趣和耐玩！

### 游戏特性
- 传统的音乐游戏玩法与跑酷游戏爽快的视觉表现相结合为你带来前所未有的心流体验。
- 华丽绚烂时尚酷炫的美术别具一格。
- 基础包内置 30 首精心挑选的曲目，持续更新曲目！
- 不同风格的音乐对应特定的场景主题，敌人与BOSS。
- 小姐姐敲可爱！小宠物敲可爱！敌人也敲可爱！就连反派大魔王都能萌得你流鼻血！！
- 精心设计的台词，日语声优配音。

(๑•ㅂ•́)و✧
'
WHERE game_id = 9;

INSERT INTO storage (uuid, file_name, uploader, upload_time, is_public)
VALUES ('c678f433-0a14-40c2-8437-99af8dc0bd1c', 'game.txt', 'admin', NOW(), false);
