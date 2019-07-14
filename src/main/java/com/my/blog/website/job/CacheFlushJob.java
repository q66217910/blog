package com.my.blog.website.job;

import com.my.blog.website.constant.GlobalCache;
import com.my.blog.website.dao.ContentVoMapper;
import com.my.blog.website.modal.Mo.ArticleHit;
import com.my.blog.website.modal.Vo.ContentVo;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.HashMap;
import java.util.Map;

public class CacheFlushJob extends QuartzJobBean {

    private static final Logger LOG = LoggerFactory.getLogger(CacheFlushJob.class);

    @Autowired
    ContentVoMapper contentVoMapper;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        LOG.debug("EXECUTE HIT CACHE FLUSH JOB");
        GlobalCache.articleHitCounter.flushCount();

        Map<ArticleHit, Integer> cached = GlobalCache.articleHitCounter.getAndCleanCountCached();

        Map<Integer, Integer> count = new HashMap<>();
        cached.forEach((hit, c) -> {
            if (c == null) return;
            Integer cc = count.get(hit.getCid());
            if (cc == null) cc = 0;
            cc += c;
            count.put(hit.getCid(), cc);
        });

        //noinspection UnusedAssignment
        cached = null;

        count.forEach((cid, c) -> {
            ContentVo contentVo = contentVoMapper.selectByPrimaryKey(cid);
            if (contentVo == null) return;
            Integer hits = contentVo.getHits();
            hits = hits != null ? hits : 0;

            contentVo.setHits(hits + c);
            contentVoMapper.updateByPrimaryKeySelective(contentVo);
        });
    }
}
