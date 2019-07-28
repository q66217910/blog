package com.my.blog.website.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.my.blog.website.constant.WebConst;
import com.my.blog.website.dao.ContentVoMapper;
import com.my.blog.website.dao.MetaVoMapper;
import com.my.blog.website.dto.Types;
import com.my.blog.website.exception.TipException;
import com.my.blog.website.modal.Vo.*;
import com.my.blog.website.service.IContentService;
import com.my.blog.website.service.IMetaService;
import com.my.blog.website.service.IRelationshipService;
import com.my.blog.website.utils.DateKit;
import com.my.blog.website.utils.TaleUtils;
import com.my.blog.website.utils.Tools;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13 013.
 */
@Service
public class ContentServiceImpl implements IContentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentServiceImpl.class);

    @Resource
    private ContentVoMapper contentDao;

    @Resource
    private MetaVoMapper metaDao;

    @Resource
    private IRelationshipService relationshipService;

    @Resource
    private IMetaService metasService;

    @Override
    public void publish(ContentVo contents) {
        if (null == contents) {
            throw new TipException("文章对象为空");
        }
        if (StringUtils.isBlank(contents.getTitle())) {
            throw new TipException("文章标题不能为空");
        }
        if (StringUtils.isBlank(contents.getContent())) {
            throw new TipException("文章内容不能为空");
        }
        int titleLength = contents.getTitle().length();
        if (titleLength > WebConst.MAX_TITLE_COUNT) {
            throw new TipException("文章标题过长");
        }
        int contentLength = contents.getContent().length();
        if (contentLength > WebConst.MAX_TEXT_COUNT) {
            throw new TipException("文章内容过长");
        }
        if (null == contents.getAuthorId()) {
            throw new TipException("请登录后发布文章");
        }
        if (StringUtils.isNotBlank(contents.getSlug())) {
            if (contents.getSlug().length() < 5) {
                throw new TipException("路径太短了");
            }
            if (!TaleUtils.isPath(contents.getSlug())) throw new TipException("您输入的路径不合法");
            ContentVoExample contentVoExample = new ContentVoExample();
            contentVoExample.createCriteria().andTypeEqualTo(contents.getType()).andStatusEqualTo(contents.getSlug());
            long count = contentDao.countByExample(contentVoExample);
            if (count > 0) throw new TipException("该路径已经存在，请重新输入");
        } else {
            contents.setSlug(null);
        }

        contents.setContent(EmojiParser.parseToAliases(contents.getContent()));

        int time = DateKit.getCurrentUnixTime();
        contents.setCreated(time);
        contents.setModified(time);
        contents.setHits(0);
        contents.setCommentsNum(0);

        String tags = contents.getTags();
        String categories = contents.getCategories();
        contentDao.insert(contents);
        Integer cid = contents.getCid();

        metasService.saveMetas(cid, tags, Types.TAG.getType());
        metasService.saveMetas(cid, categories, Types.CATEGORY.getType());
    }

    @Override
    public PageInfo<ContentVo> getContents(Integer p, Integer limit) {
        LOGGER.debug("Enter getContents method");
        ContentVoExample example = new ContentVoExample();
        example.setOrderByClause("rank desc, created desc");
        example.createCriteria().andTypeEqualTo(Types.ARTICLE.getType()).andStatusEqualTo(Types.PUBLISH.getType());
        PageHelper.startPage(p, limit);
        List<ContentVo> data = contentDao.selectByExampleWithBLOBs(example);
        PageInfo<ContentVo> pageInfo = new PageInfo<>(data);
        LOGGER.debug("Exit getContents method");
        return pageInfo;
    }

    @Override
    public ContentVo getContents(String id) {
        if (StringUtils.isNotBlank(id)) {
            ContentVo contentVo = null;
            if (Tools.isNumber(id)) {
                contentVo = contentDao.selectByPrimaryKey(Integer.valueOf(id));
            } else {
                ContentVoExample contentVoExample = new ContentVoExample();
                contentVoExample.createCriteria().andSlugEqualTo(id);
                List<ContentVo> contentVos = contentDao.selectByExampleWithBLOBs(contentVoExample);
                if (contentVos.size() != 1) {
                    throw new TipException("query content by id and return is not one");
                }
                contentVo = contentVos.get(0);
            }

            if (contentVo != null) {
                setMetas(contentVo);
            }
            return contentVo;
        }
        return null;
    }

    private void setMetas(ContentVo contentVo) {
        List<MetaVo> metas = metasService.getMetas(contentVo.getCid());
        StringBuilder categories = new StringBuilder();
        StringBuilder tags = new StringBuilder();
        for (MetaVo meta : metas) {
            if (Types.TAG.getType().equals(meta.getType())) {
                tags.append(",").append(meta.getName());
            } else if (Types.CATEGORY.getType().equals(meta.getType())) {
                categories.append(",").append(meta.getName());
            }
        }
        if (categories.length() > 0) categories.delete(0, 1);
        if (tags.length() > 0) tags.delete(0, 1);
        contentVo.setTags(tags.toString());
        contentVo.setCategories(categories.toString());
    }

//    @Override
//    public void updateContentByCid(ContentVo contentVo) {
//        if (null != contentVo && null != contentVo.getCid()) {
//            contentDao.updateByPrimaryKeySelective(contentVo);
//        }
//    }

    @Override
    public PageInfo<ContentVo> getArticles(Integer mid, int page, int limit) {
        int total = metaDao.countWithSql(mid);
        PageHelper.startPage(page, limit);
        List<ContentVo> list = contentDao.findByCatalog(mid);
        PageInfo<ContentVo> paginator = new PageInfo<>(list);
        paginator.setTotal(total);
        return paginator;
    }

    @Override
    public PageInfo<ContentVo> getArticles(String keyword, Integer page, Integer limit) {
        PageHelper.startPage(page, limit);
        ContentVoExample contentVoExample = new ContentVoExample();
        ContentVoExample.Criteria criteria = contentVoExample.createCriteria();
        criteria.andTypeEqualTo(Types.ARTICLE.getType());
        criteria.andStatusEqualTo(Types.PUBLISH.getType());
        criteria.andTitleLike("%" + keyword + "%");
        contentVoExample.setOrderByClause("created desc");
        List<ContentVo> contentVos = contentDao.selectByExampleWithBLOBs(contentVoExample);
        return new PageInfo<>(contentVos);
    }

    @Override
    public PageInfo<ContentVo> getArticlesWithpage(ContentVoExample commentVoExample, Integer page, Integer limit) {
        PageHelper.startPage(page, limit);
        List<ContentVo> contentVos = contentDao.selectByExampleWithBLOBs(commentVoExample);
        for (ContentVo contentVo : contentVos) {
            setMetas(contentVo);
        }
        return new PageInfo<>(contentVos);
    }

    @Override
    public void deleteByCid(Integer cid) {
        ContentVo contents = this.getContents(cid + "");
        if (null != contents) {
            contentDao.deleteByPrimaryKey(cid);
            relationshipService.deleteById(cid, null);
        }
    }

    @Override
    public void updateCategory(String ordinal, String newCatefory) {
        ContentVo contentVo = new ContentVo();
        contentVo.setCategories(newCatefory);
        ContentVoExample example = new ContentVoExample();
        example.createCriteria().andCategoriesEqualTo(ordinal);
        contentDao.updateByExampleSelective(contentVo, example);
    }

    @Override
    public void updateArticle(ContentVo modifyForm) {
        if (null == modifyForm || null == modifyForm.getCid()) {
            throw new TipException("文章对象不能为空");
        }
        ContentVo contentVo = contentDao.selectByPrimaryKey(modifyForm.getCid());
        contentVo.setCid(modifyForm.getCid());
        contentVo.setTitle(modifyForm.getTitle());
        contentVo.setContent(modifyForm.getContent());
        contentVo.setStatus(modifyForm.getStatus());
        contentVo.setSlug(modifyForm.getSlug());
        contentVo.setAllowComment(modifyForm.getAllowComment());
        contentVo.setAllowPing(modifyForm.getAllowPing());
        contentVo.setAuthorId(modifyForm.getAuthorId());


        if (StringUtils.isBlank(contentVo.getTitle())) {
            throw new TipException("文章标题不能为空");
        }
        if (StringUtils.isBlank(contentVo.getContent())) {
            throw new TipException("文章内容不能为空");
        }
        if (contentVo.getTitle().length() > 200) {
            throw new TipException("文章标题过长");
        }
        if (contentVo.getContent().length() > 65000) {
            throw new TipException("文章内容过长");
        }
        if (null == contentVo.getAuthorId()) {
            throw new TipException("请登录后发布文章");
        }
        if (StringUtils.isBlank(contentVo.getSlug())) {
            contentVo.setSlug(null);
        }
        int time = DateKit.getCurrentUnixTime();
        contentVo.setModified(time);
        Integer cid = contentVo.getCid();
        contentVo.setContent(EmojiParser.parseToAliases(contentVo.getContent()));

        contentDao.updateByPrimaryKeySelective(contentVo);
        relationshipService.deleteById(cid, null);
        metasService.saveMetas(cid, modifyForm.getTags(), Types.TAG.getType());
        metasService.saveMetas(cid, modifyForm.getCategories(), Types.CATEGORY.getType());
    }
}
