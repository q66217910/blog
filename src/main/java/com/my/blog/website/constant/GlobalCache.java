package com.my.blog.website.constant;

import com.my.blog.website.modal.Mo.ArticleHit;
import com.my.blog.website.utils.IPUtil;
import com.my.blog.website.utils.MapCache;
import com.my.blog.website.utils.TimeRangeBasedHitCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class GlobalCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalCache.class);

    public static MapCache recentHitCache = MapCache.single();;

    public static TimeRangeBasedHitCounter<ArticleHit> articleHitCounter = new TimeRangeBasedHitCounter();

    public static void hit (Integer cid, HttpServletRequest request) {
        String requestIp = IPUtil.getRequestIp(request);

        LOGGER.debug("ip address => {}", requestIp);

        ArticleHit hit = new ArticleHit();
        hit.setCid(cid);
        hit.setIpAddress(requestIp);

        articleHitCounter.addCount(hit);
    }
}
