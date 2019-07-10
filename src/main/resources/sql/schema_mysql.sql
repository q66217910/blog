-- CREATE SCHEMA `blog` DEFAULT CHARACTER SET utf8mb4 ;
-- use `blog`;

-- 表：t_attach
CREATE TABLE IF NOT EXISTS `t_attach` (
    `id` INTEGER AUTO_INCREMENT NOT NULL,
    `fname` VARCHAR(128) NOT NULL,
    `ftype` VARCHAR(64),
    `fkey` VARCHAR(128) NOT NULL,
    `author_id` INTEGER NOT NULL,
    `created` INTEGER NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

CREATE TABLE IF NOT EXISTS `t_comments` (
    `coid` INTEGER AUTO_INCREMENT NOT NULL,
    `cid` INTEGER DEFAULT 0 NOT NULL,
    `created` INTEGER NOT NULL,
    `author` VARCHAR(255) NOT NULL,
    `author_id` INTEGER DEFAULT 0,
    `owner_id` INTEGER DEFAULT 0,
    `mail` VARCHAR(255) NOT NULL,
    `url` VARCHAR(255),
    `ip` VARCHAR(255),
    `agent` VARCHAR(255),
    `content` TEXT NOT NULL,
    `type` VARCHAR(16),
    `status` VARCHAR(16),
    `parent` INTEGER DEFAULT 0,
    PRIMARY KEY (`coid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 表：t_contents
CREATE TABLE IF NOT EXISTS `t_contents` (
    `cid` int(11) NOT NULL AUTO_INCREMENT,
    `title` varchar(255) NOT NULL,
    `slug` varchar(255) DEFAULT NULL,
    `thumb_img` varchar(255) DEFAULT NULL,
    `created` int(11) NOT NULL,
    `modified` int(11) DEFAULT NULL,
    `content` text,
    `author_id` int(11) NOT NULL,
    `type` varchar(16) NOT NULL,
    `status` varchar(16) NOT NULL,
    `fmt_type` varchar(16) DEFAULT 'markdown',
    `tags` varchar(255) DEFAULT NULL,
    `categories` varchar(255) DEFAULT NULL,
    `hits` int(11) DEFAULT '0',
    `comments_num` tinyint(1) DEFAULT '0',
    `blog_number` VARCHAR(255) NULL COMMENT 'github 的 data-blog-id',
    `commit_type` TINYINT(1) NULL DEFAULT '1' COMMENT '提交类型： 1 - 普通， 2 - github自动提交',
    `allow_comment` tinyint(1) DEFAULT '1',
    `allow_ping` tinyint(1) DEFAULT NULL,
    `allow_feed` tinyint(1) DEFAULT NULL,
    `rank` INT(11) NULL DEFAULT 100,
    PRIMARY KEY (`cid`),
    UNIQUE KEY `uk_blog_number` (`commit_type` ASC, `blog_number` ASC),
    INDEX `idx_commit_type` (`commit_type` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `t_contents` VALUES 
(1,'关于','about',NULL,1487853610,1487872488,'### Hello World\n\n这是我的关于页面\n\n### 当然还有其他\n\n具体你来写点什么吧',1,'page','publish','markdown',NULL,NULL,0,0,NULL,1,1,1,1),
(3,'友情链接','links',NULL,1487861184,1487861184,'## 友情链接\n\n## 链接须知\n\n> 请确定贵站可以稳定运营\n> 原创博客优先，技术类博客优先\n> 经常过来访问和评论，眼熟的\n\n备注：默认申请友情链接均为内页（当前页面）\n\n## 基本信息\n\n请在当页通过评论来申请友链，其他地方不予回复\n\n暂时先这样，同时欢迎互换友链，这个页面留言即可。 ^_^\n\n还有，我会不定时对无法访问的网址进行清理，请保证自己的链接长期有效。',1,'page','publish','markdown',NULL,NULL,0,0,NULL,1,1,1,NULL);

-- 表：t_logs
CREATE TABLE IF NOT EXISTS `t_logs`(
    `id` INTEGER AUTO_INCREMENT NOT NULL,
    `action` VARCHAR(128) NOT NULL,
    `data` VARCHAR(2000),
    `author_id` INTEGER NOT NULL,
    `ip` VARCHAR(32),
    `created` INTEGER NOT NULL,
PRIMARY KEY(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 表：t_metas
CREATE TABLE IF NOT EXISTS `t_metas`(
    `mid` INTEGER AUTO_INCREMENT NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255),
    `type` VARCHAR(32) NOT NULL,
    `description` VARCHAR(255),
    `sort` INTEGER DEFAULT 0,
    `parent` INTEGER DEFAULT 0,
    PRIMARY KEY(`mid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO t_metas (mid, name, slug, type, description, sort, parent) VALUES (1, '默认分类', NULL, 'category', NULL, 0, 0);

-- 表：t_options
CREATE TABLE IF NOT EXISTS `t_options`(
    `name` VARCHAR(128) NOT NULL,
    `value` TEXT,
    `description` VARCHAR (255),
    PRIMARY KEY(`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO t_options (name, value, description) VALUES ('site_title', '悲回风的个人博客', '');
INSERT INTO t_options (name, value, description) VALUES ('social_weibo', '', NULL);
INSERT INTO t_options (name, value, description) VALUES ('social_zhihu', '', NULL);
INSERT INTO t_options (name, value, description) VALUES ('social_github', '', NULL);
INSERT INTO t_options (name, value, description) VALUES ('social_twitter', '', NULL);
INSERT INTO t_options (name, value, description) VALUES ('allow_install', '0', '是否允许重新安装博客');
INSERT INTO t_options (name, value, description) VALUES ('site_theme', 'default', NULL);
INSERT INTO t_options (name, value, description) VALUES ('site_keywords', 'uetty,悲回风,vincent_field', NULL);
INSERT INTO t_options (name, value, description) VALUES ('site_description', '自16毕业已有3年，偶有收获，常作文以记，时常复习', NULL);

-- 表：t_relationships
CREATE TABLE IF NOT EXISTS `t_relationships`(
    `cid` INTEGER NOT NULL,
    `mid` INTEGER NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO t_relationships(cid, mid) VALUES(2, 1);

-- 表：t_users
CREATE TABLE IF NOT EXISTS `t_users`(
    `uid` INTEGER AUTO_INCREMENT NOT NULL,
    `username` VARCHAR(64) NOT NULL,
    `password` VARCHAR(64) NOT NULL,
    `email` VARCHAR(128),
    `home_url` VARCHAR(255),
    `screen_name` VARCHAR(128),
    `created` INTEGER NOT NULL,
    `activated` INTEGER,
    `logged` INTEGER,
    `group_name` VARCHAR(16),
    PRIMARY KEY(`uid`),
    UNIQUE INDEX `uk_name` (`username` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `t_users` VALUES (1,'vince','395a6186094f5ddf99a72239d824b944','vincent_field@foxmail.com',NULL,'vince',1500756162,0,0,'visitor');
