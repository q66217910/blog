-- MySQL dump 10.13  Distrib 5.7.26, for Linux (x86_64)
--
-- Host: localhost    Database: iblog
-- ------------------------------------------------------
-- Server version	5.7.26-0ubuntu0.16.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `t_attach`
--

DROP TABLE IF EXISTS `t_attach`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_attach` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fname` varchar(128) NOT NULL,
  `ftype` varchar(64) DEFAULT NULL,
  `fkey` varchar(128) NOT NULL,
  `author_id` int(11) NOT NULL,
  `created` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_attach`
--

LOCK TABLES `t_attach` WRITE;
/*!40000 ALTER TABLE `t_attach` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_attach` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_comments`
--

DROP TABLE IF EXISTS `t_comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_comments` (
  `coid` int(11) NOT NULL AUTO_INCREMENT,
  `cid` int(11) NOT NULL DEFAULT '0',
  `created` int(11) NOT NULL,
  `author` varchar(255) NOT NULL,
  `author_id` int(11) DEFAULT '0',
  `owner_id` int(11) DEFAULT '0',
  `mail` varchar(255) NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `agent` varchar(255) DEFAULT NULL,
  `content` text NOT NULL,
  `type` varchar(16) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `parent` int(11) DEFAULT '0',
  PRIMARY KEY (`coid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_comments`
--

LOCK TABLES `t_comments` WRITE;
/*!40000 ALTER TABLE `t_comments` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_contents`
--

DROP TABLE IF EXISTS `t_contents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_contents` (
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
  `blog_number` varchar(255) DEFAULT NULL COMMENT 'github 的 data-blog-id',
  `commit_type` tinyint(1) DEFAULT '1' COMMENT '提交类型： 1 - 普通， 2 - github自动提交',
  `allow_comment` tinyint(1) DEFAULT '1',
  `allow_ping` tinyint(1) DEFAULT NULL,
  `allow_feed` tinyint(1) DEFAULT NULL,
  `rank` int(11) DEFAULT '100',
  `is_shadow` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`cid`),
  UNIQUE KEY `uk_blog_number` (`commit_type`,`blog_number`),
  KEY `idx_commit_type` (`commit_type`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_contents`
--

LOCK TABLES `t_contents` WRITE;
/*!40000 ALTER TABLE `t_contents` DISABLE KEYS */;
INSERT INTO `t_contents` VALUES (1,'关于','about',NULL,1487853610,1487872488,'### Hello World\n\n这是我的关于页面\n\n',1,'page','publish','markdown',NULL,NULL,19,0,NULL,1,1,1,1,60,_binary '\0'),(2,'友情链接','links',NULL,1487861184,1566212874,'## 友情链接\r\n\r\n## 链接须知\r\n\r\n> 请确定贵站可以稳定运营\r\n> 原创博客优先，技术类、算法类博客\r\n> 经常过来访问和评论，眼熟的\r\n\r\n备注：默认申请友情链接均为内页（当前页面）\r\n\r\n## 基本信息\r\n\r\n请在当页通过评论来申请友链，其他地方不予回复\r\n\r\n暂时先这样，同时欢迎互换友链，这个页面留言即可。 ^_^\r\n\r\n还有，我会不定时对无法访问的网址进行清理，请保证自己的链接长期有效。',1,'page','publish','markdown',NULL,NULL,0,0,NULL,1,1,1,0,60,_binary '\0');
/*!40000 ALTER TABLE `t_contents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_logs`
--

DROP TABLE IF EXISTS `t_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `action` varchar(128) NOT NULL,
  `data` varchar(2000) DEFAULT NULL,
  `author_id` int(11) NOT NULL,
  `ip` varchar(32) DEFAULT NULL,
  `created` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_logs`
--

--
-- Table structure for table `t_metas`
--

DROP TABLE IF EXISTS `t_metas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_metas` (
  `mid` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `slug` varchar(255) DEFAULT NULL,
  `type` varchar(32) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `sort` int(11) DEFAULT '0',
  `parent` int(11) DEFAULT '0',
  PRIMARY KEY (`mid`)
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_metas`
--

LOCK TABLES `t_metas` WRITE;
/*!40000 ALTER TABLE `t_metas` DISABLE KEYS */;
INSERT INTO `t_metas` VALUES (1,'默认分类',NULL,'category',NULL,0,0);
/*!40000 ALTER TABLE `t_metas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_options`
--

DROP TABLE IF EXISTS `t_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_options` (
  `name` varchar(128) NOT NULL,
  `value` text,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_options`
--

LOCK TABLES `t_options` WRITE;
/*!40000 ALTER TABLE `t_options` DISABLE KEYS */;
INSERT INTO `t_options` VALUES ('allow_install','0','是否允许重新安装博客'),('site_csdn','https://blog.csdn.net/vincent_field',NULL),('site_description','个人博客',NULL),('site_github','https://github.com/uetty',NULL),('site_keywords','uetty,vincent_field',NULL),('site_motto','每一首歌都是一种生活方式',NULL),('site_theme','default',NULL),('site_title','Vince的个人博客',''),('social_github','',NULL),('social_twitter','',NULL),('social_weibo','',NULL),('social_zhihu','',NULL);
/*!40000 ALTER TABLE `t_options` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_relationships`
--

DROP TABLE IF EXISTS `t_relationships`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_relationships` (
  `cid` int(11) NOT NULL,
  `mid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_relationships`
--

LOCK TABLES `t_relationships` WRITE;
/*!40000 ALTER TABLE `t_relationships` DISABLE KEYS */;
INSERT INTO `t_relationships` VALUES (15,1),(16,1),(17,1),(31,1),(32,6),(32,7),(32,8),(32,9),(32,4),(32,3),(30,14),(30,9),(30,15),(30,11),(30,10),(29,18),(29,19),(29,14),(29,17),(29,12),(29,10),(28,21),(28,7),(28,9),(28,20),(28,11),(28,5),(28,3),(27,23),(27,24),(27,22),(26,28),(26,29),(26,30),(26,26),(26,25),(25,32),(25,31),(24,35),(24,36),(24,37),(24,34),(24,33),(22,7),(22,38),(22,3),(21,40),(21,41),(21,42),(21,43),(21,39),(20,45),(20,14),(20,46),(20,44),(20,17),(20,10),(18,48),(18,49),(18,50),(18,47),(14,52),(14,14),(14,53),(14,51),(14,10),(13,54),(13,14),(13,55),(13,56),(13,10),(11,59),(11,60),(11,57),(10,61),(10,62),(10,63),(10,13),(9,59),(9,64),(9,65),(9,66),(9,57),(8,69),(8,70),(8,71),(8,72),(8,68),(7,73),(7,74),(7,61),(7,13),(7,11),(6,73),(6,61),(6,75),(6,13),(6,11),(5,61),(5,62),(5,76),(5,13),(4,18),(4,61),(4,78),(4,79),(4,13),(4,12),(3,80),(3,81),(3,82),(3,77),(3,11),(19,59),(19,83),(19,52),(19,57),(12,85),(12,86),(12,61),(12,84),(12,13),(33,87),(33,88),(33,89),(33,90),(33,39),(33,11),(23,91),(23,43),(23,39),(23,11),(34,1),(35,1);
/*!40000 ALTER TABLE `t_relationships` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_users`
--

DROP TABLE IF EXISTS `t_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_users` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password` varchar(64) NOT NULL,
  `email` varchar(128) DEFAULT NULL,
  `home_url` varchar(255) DEFAULT NULL,
  `screen_name` varchar(128) DEFAULT NULL,
  `created` int(11) NOT NULL,
  `activated` int(11) DEFAULT NULL,
  `logged` int(11) DEFAULT NULL,
  `group_name` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `uk_name` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_users`
--

LOCK TABLES `t_users` WRITE;
/*!40000 ALTER TABLE `t_users` DISABLE KEYS */;
INSERT INTO `t_users` VALUES (1,'vince','395a6186094f5ddf99a72239d824b944','vincent_field@foxmail.com',NULL,'vince',1500756162,0,0,'visitor');
/*!40000 ALTER TABLE `t_users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-09-22 23:10:31
