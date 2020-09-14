CREATE DATABASE susteam;

CREATE USER 'susteam'@'%' IDENTIFIED BY 'susteam';

GRANT ALL ON susteam.* TO 'susteam'@'%';

FLUSH PRIVILEGES;