package com.my.blog.website.modal.Mo;

import java.util.Objects;

public class ArticleHit {

    Integer cid;
    String ipAddress;

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public int hashCode() {
        int ch = cid != null ? cid.hashCode() : 0;
        int ih = ipAddress != null ? ipAddress.hashCode() : 0;
        return ch ^ ih;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ArticleHit)) return false;
        ArticleHit ohit = (ArticleHit) obj;
        return Objects.equals(cid, ohit.cid) && Objects.equals(ipAddress, ohit.ipAddress);
    }
}
