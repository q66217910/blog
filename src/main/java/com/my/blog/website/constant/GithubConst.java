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
    private String preUrl;
    private String url;
    private String name;
    private String indexName;
    private String pathProp;
    private String articleIdProp;
    private String originIdProp;
    private String ignoreProp;
    private String attachPath;
    private Integer authorId;

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

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getPathProp() {
        return pathProp;
    }

    public void setPathProp(String pathProp) {
        this.pathProp = pathProp;
    }

    public String getArticleIdProp() {
        return articleIdProp;
    }

    public void setArticleIdProp(String articleIdProp) {
        this.articleIdProp = articleIdProp;
    }

    public String getOriginIdProp() {
        return originIdProp;
    }

    public void setOriginIdProp(String originIdProp) {
        this.originIdProp = originIdProp;
    }

    public String getIgnoreProp() {
        return ignoreProp;
    }

    public void setIgnoreProp(String ignoreProp) {
        this.ignoreProp = ignoreProp;
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
}
