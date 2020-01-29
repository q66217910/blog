package com.my.blog.website.constant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: Vince
 * @Date: 2019/7/8 9:59
 */

@ConfigurationProperties(prefix = "github.blog")
@Component
public class GithubConst {
    private String preUrl; // github 网络问题导致不好请求url，尝试先请求preUrl预热一下，再请求url
    private String url;
    private String name;
    private String postFolderName;
    private Integer authorId;
    private String attachFolderName;
    private String attachPath;


    private String attrTitle;
    private String attrDate;
    private String attrTag;
    private String attrPermalink;
    private String attrKeywords;
    private String attrRid;
    private String attrOriginId;
    private String attrShadow;

//    private String preUrl;
//    private String indexName;
//    private String pathProp;
//    private String articleIdProp;
//    private String originIdProp;
//    private String ignoreProp;

    public String getPreUrl() {
        return preUrl;
    }

    public void setPreUrl(String preUrl) {
        this.preUrl = preUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPostFolderName(String postFolderName) {
        this.postFolderName = postFolderName;
    }

    public String getPostFolderName() {
        return postFolderName;
    }

    public void setAttachFolderName(String attachFolderName) {
        this.attachFolderName = attachFolderName;
    }

    public String getAttachFolderName() {
        return attachFolderName;
    }

    public String getAttachPath() {
        return attachPath;
    }

    public void setAttachPath(String attachPath) {
        this.attachPath = attachPath;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public void setAttrTitle(String attrTitle) {
        this.attrTitle = attrTitle;
    }

    public String getAttrTitle() {
        return attrTitle;
    }

    public void setAttrDate(String attrDate) {
        this.attrDate = attrDate;
    }

    public String getAttrDate() {
        return attrDate;
    }

    public void setAttrTag(String attrTag) {
        this.attrTag = attrTag;
    }

    public String getAttrTag() {
        return attrTag;
    }

    public void setAttrPermalink(String attrPermalink) {
        this.attrPermalink = attrPermalink;
    }

    public String getAttrPermalink() {
        return attrPermalink;
    }

    public void setAttrKeywords(String attrKeywords) {
        this.attrKeywords = attrKeywords;
    }

    public String getAttrKeywords() {
        return attrKeywords;
    }

    public void setAttrRid(String attrRid) {
        this.attrRid = attrRid;
    }

    public String getAttrRid() {
        return attrRid;
    }

    public void setAttrOriginId(String attrOriginId) {
        this.attrOriginId = attrOriginId;
    }

    public String getAttrOriginId() {
        return attrOriginId;
    }

    public void setAttrShadow(String attrShadow) {
        this.attrShadow = attrShadow;
    }

    public String getAttrShadow() {
        return attrShadow;
    }

//    public String getPreUrl() {
//        return preUrl;
//    }
//
//    public void setPreUrl(String preUrl) {
//        this.preUrl = preUrl;

//    }
//    public String getIndexName() {
//        return indexName;
//    }
//
//    public void setIndexName(String indexName) {
//        this.indexName = indexName;

//    }
//    public String getPathProp() {
//        return pathProp;
//    }
//
//    public void setPathProp(String pathProp) {
//        this.pathProp = pathProp;
//    }
//
//    public String getArticleIdProp() {
//        return articleIdProp;
//    }
//
//    public void setArticleIdProp(String articleIdProp) {
//        this.articleIdProp = articleIdProp;
//    }
//
//    public String getOriginIdProp() {
//        return originIdProp;
//    }
//
//    public void setOriginIdProp(String originIdProp) {
//        this.originIdProp = originIdProp;
//    }
//
//    public String getIgnoreProp() {
//        return ignoreProp;
//    }
//
//    public void setIgnoreProp(String ignoreProp) {
//        this.ignoreProp = ignoreProp;
//    }
//
//

}
