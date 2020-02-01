DROP TABLE IF EXISTS developer;

CREATE TABLE if not exists `developer`
(
    `name`      varchar(128) NOT NULL COMMENT '姓名',
    `username`  varchar(128) NOT NULL COMMENT '用户名',
    `email`     varchar(128) NOT NULL COMMENT '邮箱',
    `mobile`    varchar(128) NOT NULL COMMENT '手机号',
    `receivers` text         NOT NULL COMMENT '手机号',
    PRIMARY KEY (`name`)
);
