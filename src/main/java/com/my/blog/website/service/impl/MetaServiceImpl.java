package com.my.blog.website.service.impl;

import com.my.blog.website.constant.WebConst;
import com.my.blog.website.dao.ContentVoMapper;
import com.my.blog.website.dto.MetaDto;
import com.my.blog.website.dto.Types;
import com.my.blog.website.exception.TipException;
import com.my.blog.website.modal.Vo.*;
import com.my.blog.website.service.IMetaService;
import com.my.blog.website.service.IRelationshipService;
import com.my.blog.website.dao.MetaVoMapper;
import com.my.blog.website.service.IContentService;
import com.my.blog.website.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by BlueT on 2017/3/17.
 */
@Service
public class MetaServiceImpl implements IMetaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaServiceImpl.class);

    @Resource
    private MetaVoMapper metaDao;

    @Resource
    private IRelationshipService relationshipService;

    @Resource
    private IContentService contentService;

    @Resource
    private ContentVoMapper contentVoMapper;

    @Override
    public MetaDto getMeta(String type, String name) {
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(name)) {
            return metaDao.selectDtoByNameAndType(name, type);
        }
        return null;
    }

    @Override
    public List<MetaVo> getMetas(String types) {
        if (StringUtils.isNotBlank(types)) {
            MetaVoExample metaVoExample = new MetaVoExample();
            metaVoExample.setOrderByClause("sort desc, mid desc");
            metaVoExample.createCriteria().andTypeEqualTo(types);
            return metaDao.selectByExample(metaVoExample);
        }
        return null;
    }

    @Override
    public List<MetaDto> getMetaList(String type, String orderby, int limit) {
        if (StringUtils.isNotBlank(type)) {
            if (StringUtils.isBlank(orderby)) {
                orderby = "count desc, a.mid desc";
            }
            if (limit < 1 || limit > WebConst.MAX_POSTS) {
                limit = 10;
            }
            Map<String, Object> paraMap = new HashMap<>();
            paraMap.put("type", type);
            paraMap.put("order", orderby);
            paraMap.put("limit", limit);
            return metaDao.selectFromSql(paraMap);
        }
        return null;
    }

    @Override
    public void delete(int mid) {
        MetaVo metas = metaDao.selectByPrimaryKey(mid);
        if (null != metas) {
            String type = metas.getType();
            String name = metas.getName();

            metaDao.deleteByPrimaryKey(mid);

            List<RelationshipVoKey> rlist = relationshipService.getRelationshipById(null, mid);
            if (null != rlist) {
                for (RelationshipVoKey r : rlist) {
                    ContentVo contents = contentService.getContents(String.valueOf(r.getCid()));
                    if (null != contents) {
                        if (type.equals(Types.CATEGORY.getType())) {
                            contents.setCategories(reMeta(name, contents.getCategories()));
                        }
                        if (type.equals(Types.TAG.getType())) {
                            contents.setTags(reMeta(name, contents.getTags()));
                        }
                        contentVoMapper.updateByPrimaryKey(contents);
                    }
                }
            }
            relationshipService.deleteById(null, mid);
        }
    }

    @Override
    public void saveMeta(String type, String name, Integer mid) {
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(name)) {

            MetaVoExample metaVoExample = new MetaVoExample();
            metaVoExample.createCriteria().andTypeEqualTo(type).andNameEqualTo(name);
            List<MetaVo> metaVos = metaDao.selectByExample(metaVoExample);
            MetaVo metas;
            if (metaVos.size() != 0) {
                throw new TipException("已经存在该项");
            } else {
                metas = new MetaVo();
                metas.setName(name);
                if (null != mid) {
                    MetaVo original = metaDao.selectByPrimaryKey(mid);
                    metas.setMid(mid);
                    metaDao.updateByPrimaryKeySelective(metas);
//                    更新原有文章的categories
                    contentService.updateCategory(original.getName(),name);
                } else {
                    metas.setType(type);
                    metaDao.insertSelective(metas);
                }
            }
        }
    }

    @Override
    public void saveMetas(Integer cid, String names, String type) {
        if (null == cid) {
            throw new TipException("项目关联id不能为空");
        }
        if (StringUtils.isNotBlank(names) && StringUtils.isNotBlank(type)) {
            String[] nameArr = StringUtils.split(names, ",");
            List<String> nameList = new ArrayList<>(Arrays.asList(nameArr));
            updateTag(cid, nameList, type);
        }
    }

    private void updateTag(Integer cid, List<String> names, String type) {
        List<MetaVo> metas = getMetas(cid);
        metas = metas.stream().filter(meta -> Objects.equals(type, meta.getType())).collect(Collectors.toList());
        LOGGER.debug("cid: {}, type: {}, names: {}", cid, type, names);
        LOGGER.debug("original metas: {}", metas.stream().map(MetaVo::getName).collect(Collectors.toList()));

        if (Types.CATEGORY.getType().equals(type)) {
            if (names.size() > 1) {
                String category = names.get(0);
                names = new ArrayList<>();
                names.add(category);
            }
        }
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            for (int j = 0; j < metas.size(); j++) {
                if (Objects.equals(metas.get(j).getName(), name)) {
                    names.remove(i--);
                    metas.remove(j);
                    break;
                }
            }
        }

        for (MetaVo meta : metas) {
            LOGGER.debug("remove meta, mid: {}, name: {}", meta.getMid(), meta.getName());
            relationshipService.deleteById(cid, meta.getMid());
        }
        for (String name : names) {
            LOGGER.debug("save or update meta: {}", name);
            saveOrUpdate(cid, name, type);
        }
    }

    private void saveOrUpdate(Integer cid, String name, String type) {
        MetaVoExample metaVoExample = new MetaVoExample();
        metaVoExample.createCriteria().andTypeEqualTo(type).andNameEqualTo(name);
        List<MetaVo> metaVos = metaDao.selectByExample(metaVoExample);

        int mid;
        MetaVo metas;
        if (metaVos.size() > 1) {
            throw new TipException("查询到多条数据");
        } else if (metaVos.size() == 1) {
            metas = metaVos.get(0);
            mid = metas.getMid();
        } else {
            metas = new MetaVo();
            metas.setSlug(name);
            metas.setName(name);
            metas.setType(type);
            metaDao.insertSelective(metas);
            mid = metas.getMid();
        }
        if (mid != 0) {
            Long count = relationshipService.countById(cid, mid);
            if (count == 0) {
                RelationshipVoKey relationships = new RelationshipVoKey();
                relationships.setCid(cid);
                relationships.setMid(mid);
                relationshipService.insertVo(relationships);
            }
        }
    }

    private String reMeta(String name, String metas) {
        String[] ms = StringUtils.split(metas, ",");
        StringBuilder sbuf = new StringBuilder();
        for (String m : ms) {
            if (!name.equals(m)) {
                sbuf.append(",").append(m);
            }
        }
        if (sbuf.length() > 0) {
            return sbuf.substring(1);
        }
        return "";
    }

    @Override
    public void saveMeta(MetaVo metas) {
        if (null != metas) {
            metaDao.insertSelective(metas);
        }
    }

    @Override
    public void update(MetaVo metas) {
        if (null != metas && null != metas.getMid()) {
            metaDao.updateByPrimaryKeySelective(metas);
        }
    }

    @Override
    public List<MetaVo> getMetas(Integer cid) {
        List<RelationshipVoKey> relationships = relationshipService.getRelationshipById(cid, null);
        List<Integer> mids = relationships.stream().map(RelationshipVoKey::getMid).collect(Collectors.toList());

        List<MetaVo> metaVos = new ArrayList<>();
        if (mids.size() > 0) {
            MetaVoExample metaVoExample = new MetaVoExample();
            metaVoExample.createCriteria().andMidIn(mids);
            metaVoExample.setOrderByClause("type desc");
            metaVos = metaDao.selectByExample(metaVoExample);
        }
        return metaVos;
    }
}
