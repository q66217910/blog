package com.my.blog.website.job;

import com.my.blog.website.constant.GithubConst;
import com.my.blog.website.constant.WebConst;
import com.my.blog.website.dao.ContentVoMapper;
import com.my.blog.website.dto.Types;
import com.my.blog.website.modal.Vo.ContentVo;
import com.my.blog.website.modal.Vo.ContentVoExample;
import com.my.blog.website.service.IMetaService;
import com.my.blog.website.utils.*;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GithubQuartzJob extends QuartzJobBean {

    private static final Logger LOG = LoggerFactory.getLogger(GithubQuartzJob.class);

    @Autowired
    ContentVoMapper contentVoMapper;
    @Autowired
    IMetaService metasService;

    @Value("${file.path}/git")
    String savePath;
    @Autowired
    GithubConst githubConst;

    String PROP_REGEX = "([a-zA-Z-]+)=\"(.*?)\"";
    String NAME_REGEX = "<a.*?>(.*)</a>";


    private File unzipToFolder(File zipFile, File targetFolder) throws IOException {
        File tempFolder = null;
        try {
            tempFolder = new File(savePath + "/" + randomName());
            ZipUtils.unZip(zipFile, tempFolder.getAbsolutePath());

            File[] listFiles = tempFolder.listFiles();
            if (listFiles == null || listFiles.length != 1) {
                LOG.error("some error occur on download zip file. {}", listFiles);
                return null;
            }
            FileTool.deleteFiles(targetFolder);

            FileTool.moveFiles(listFiles[0], targetFolder, true);

            return targetFolder;
        } finally {
            FileTool.deleteFiles(tempFolder);
        }
    }

    private String getDateStr () {
        Calendar c = Calendar.getInstance();
        return String.format("%04d%02d$02d-%02d%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    private String randomName () {
        return UUID.randomUUID().toString().substring(23) + "-" + getDateStr();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private synchronized File downloadGitProject() throws IOException {
        FileOutputStream fos = null;
        File file;
        try {
            HttpClientUtil.HttpResponseVo responseVo = HttpClientUtil.doGetLoad(githubConst.getUrl(), null, null);
            InputStream inputStream = responseVo.getInputStream();
            if (inputStream == null) throw new RuntimeException("DOWNLOAD FAILED");
            String zipFileName = randomName() + ".zip";
            file = new File(savePath + "/" + zipFileName);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            } else {
                file.delete();
            }
            file.createNewFile();

            fos = new FileOutputStream(file);
            FileTool.writeFromInputStream(fos, inputStream);
            LOG.debug("SUCCESS DOWNLOAD GITHUB PROJECT");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignore) {
                }
            }
        }
        return file;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        LOG.debug("EXECUTE GITHUB BLOG UPDATE");
        File zipFile = null;
        try {
            zipFile = downloadGitProject();

            File file = new File(savePath + "/" + githubConst.getName());
            unzipToFolder(zipFile, file);

            List<GithubArticle> githubArticles = readGithubArticleList(file);
            if (githubArticles == null) {
                LOG.warn("github article not found");
                return;
            }

            githubArticles = githubArticles.stream().filter(article -> !article.ignore && StringUtil.isNotBlank(article.blogId))
                    .collect(Collectors.toList());

            // changed id
            List<GithubArticle> changeList = new ArrayList<>(githubArticles).stream().filter(article -> StringUtil.isNotBlank(article.originId)).collect(Collectors.toList());
            if (changeList.size() != 0) {
                List<String> changeNumbers = changeList.stream().map(article -> article.originId).collect(Collectors.toList());
                List<ContentVo> contentVos = contentVoMapper.selectByBlogNumberIn(changeNumbers);
                for (ContentVo contentVo : contentVos) {
                    GithubArticle article = null;
                    for (GithubArticle ca : changeList) {
                        if (Objects.equals(contentVo.getBlogNumber(), ca.originId)) {
                            article = ca;
                            break;
                        }
                    }
                    contentVo.setBlogNumber(Objects.requireNonNull(article).blogId);
                    contentVoMapper.updateByPrimaryKey(contentVo);
                }
            }

            // delete not exist
            List<String> currentBlogIds = githubArticles.stream().map(article -> article.blogId).collect(Collectors.toList());
            contentVoMapper.deleteByBlogNumberNotIn(currentBlogIds);

            List<GithubArticle> cloneArticles = new ArrayList<>(githubArticles);
            githubArticles.forEach(article -> {
                try {
                    ContentVo contentVo = contentVoMapper.selectByBlogNumberWithBLOBs(article.blogId);
                    readFileContent(article, cloneArticles);

                    if (contentVo != null) {
                        updateContent(contentVo, article);
                    } else {
                        createContent(article);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });

            Set<String> blogNumbers = new HashSet<>();
            List<GithubArticle> articles = cloneArticles.stream().filter(article -> {
                blogNumbers.addAll(article.refIdSet);
                return article.refIdSet.size() > 0;
            }).collect(Collectors.toList());

            if (articles.size() > 0) {
                List<ContentVo> refBlogList = contentVoMapper.selectByBlogNumberIn(new ArrayList<>(blogNumbers));
                for (GithubArticle art : articles) {
                    replaceContentRef(art, refBlogList);
                }
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            FileTool.deleteFiles(zipFile);
        }
    }

    private void replaceContentRef (GithubArticle art, List<ContentVo> refBlogList) {
        try {
            ContentVo contentVo = contentVoMapper.selectByBlogNumberWithBLOBs(art.blogId);
            String content = contentVo.getContent();
            if (content == null) return;
            for (String refId : art.refIdSet) {
                String refSlug = null;
                for (ContentVo refContent : refBlogList) {
                    if (refContent.getBlogNumber().equals(refId)) {
                        refSlug = StringUtil.isNotBlank(contentVo.getSlug()) ? refContent.getSlug() : refContent.getCid() + "";
                        break;
                    }
                }
                content = content.replace(getMarkRefName(refId), "./" + refSlug);
            }
            contentVo.setContent(content);
            contentVoMapper.updateByPrimaryKeyWithBLOBs(contentVo);
        } catch (Exception ignore) {}
    }

    private void createContent(GithubArticle article) {
        ContentVo contents = new ContentVo();
        contents.setTitle(article.blogName);
        contents.setContent(article.content);
        contents.setAuthorId(githubConst.getAuthorId());
        contents.setSlug(UUID.randomUUID().toString());
        contents.setCommitType(ContentVo.COMMIT_TYPE_GITHUB);
        contents.setBlogNumber(article.blogId);
        contents.setType(Types.ARTICLE.getType());
        contents.setStatus(Types.PUBLISH.getType());
        contents.setCategories("默认分类");
        contents.setAllowComment(true);
        contents.setAllowPing(true);
        contents.setAllowFeed(true);

        if (null == contents.getAuthorId()) {
            throw new RuntimeException("请登录后发布文章");
        }
        int titleLength = contents.getTitle().length();
        if (titleLength > WebConst.MAX_TITLE_COUNT) {
            throw new RuntimeException("文章标题过长");
        }
        if (StringUtils.isBlank(contents.getContent())) {
            throw new RuntimeException("文章内容不能为空");
        }
        int contentLength = contents.getContent().length();
        if (contentLength > WebConst.MAX_TEXT_COUNT) {
            throw new RuntimeException("文章内容过长");
        }
        if (StringUtils.isNotBlank(contents.getSlug())) {
            if (contents.getSlug().length() < 5) {
                throw new RuntimeException("路径太短了");
            }
            if (!TaleUtils.isPath(contents.getSlug())) throw new RuntimeException("您输入的路径不合法");
            ContentVoExample contentVoExample = new ContentVoExample();
            contentVoExample.createCriteria().andTypeEqualTo(contents.getType()).andStatusEqualTo(contents.getSlug());
            long count = contentVoMapper.countByExample(contentVoExample);
            if (count > 0) throw new RuntimeException("该路径已经存在，请重新输入");
        } else {
            contents.setSlug(null);
        }

        contents.setContent(EmojiParser.parseToAliases(contents.getContent()));

        int time = DateKit.getCurrentUnixTime();
        contents.setCreated(time);
        contents.setModified(time);
        contents.setCommentsNum(0);
        contents.setHits(0);

        String tags = contents.getTags();
        String categories = contents.getCategories();
        contentVoMapper.insert(contents);
        Integer cid = contents.getCid();

        metasService.saveMetas(cid, categories, Types.CATEGORY.getType());
        metasService.saveMetas(cid, tags, Types.TAG.getType());
    }

    private String getMarkRefName (String refId) {
        return "<ref=" + refId + ">";
    }

    private void readFileContent(GithubArticle article, List<GithubArticle> possibleRefArticle) throws IOException {
        File file = new File(article.path);
        File parentFile = file.getParentFile();


        String s1 = FileTool.readAsString(file);
        StringBuilder sb = new StringBuilder().append(s1);

        Pattern linkPatter = Pattern.compile("\\[(.*?)\\]\\([<]?(.*?)[>]?\\)");
        Matcher matcher = linkPatter.matcher(sb.toString());
        Map<String, String> attachMap = new HashMap<>();
        Map<String, String> refMap = new HashMap<>();

        while (matcher.find()) {
            String link = matcher.group(2);
            if (StringUtils.isBlank(link)) continue;
            if (StringUtil.isUrl(link)) continue;
            File linkFile = new File(parentFile, link);
            String refId = null;
            for (GithubArticle ref : possibleRefArticle) {
                File file1 = new File(ref.path);
                if (FileTool.fileEquals(linkFile, file1)) {
                    refId = ref.blogId;
                    break;
                }
            }
            if (refId != null) {
                refMap.put(link, refId);
                continue;
            }
            attachMap.put(link, "/" + githubConst.getAttachPath() + link);
        }

        refMap.forEach((key, value) -> {
            String s = sb.toString();
            sb.delete(0, sb.length());
            article.refIdSet.add(value);
            sb.append(s.replace(key, getMarkRefName(value)));
        });
        attachMap.forEach((key, value) -> {
            String s = sb.toString();
            sb.delete(0, sb.length());
            File attach = new File(parentFile, key);
            try {
                FileTool.copyFiles(attach, new File(TaleUtils.getFilePath() + value), true);
            } catch (IOException e) {
                LOG.warn(e.getMessage(), e);
            }
            sb.append(s.replace(key, value));
        });
        s1 = sb.toString();
        String[] split = s1.split("\n");
        sb.delete(0, sb.length());
        int lineNum = 0;
        for (; lineNum < split.length; lineNum++) {
            if (StringUtil.isNotBlank(split[lineNum]) && !split[lineNum].trim().startsWith("# ")) {
                break;
            }
        }
        for (; lineNum < split.length; lineNum++) {
            sb.append("\n").append(split[lineNum]);
        }
        if (sb.length() > 0) sb.delete(0, 1);
        article.content = sb.toString();
    }


    private void updateContent(ContentVo contentVo, GithubArticle article) {
        contentVo.setTitle(article.blogName);
        contentVo.setContent(article.content);
        contentVoMapper.updateByPrimaryKeyWithBLOBs(contentVo);
    }

    private List<GithubArticle> readGithubArticleList(File blogFolder) throws IOException {
        File[] files = blogFolder.listFiles();
        if (files == null) return null;
        File indexFile = null;
        for (File f : files) {
            if (Objects.equals(f.getName(), githubConst.getIndexName())) {
                indexFile = f;
                break;
            }
        }
        if (indexFile == null) return null;

        List<GithubArticle> list = new ArrayList<>();

        String string = FileTool.readAsString(indexFile);
        Pattern pp = Pattern.compile(PROP_REGEX);
        Pattern bp = Pattern.compile(NAME_REGEX);
        String folder = blogFolder.getAbsolutePath();
        HtmlUtil.tagReplace(string, "a", (fullTag, headTag) -> {
            Matcher matcher = pp.matcher(fullTag);
            GithubArticle article = new GithubArticle();
            while (matcher.find()) {
                String name = matcher.group(1);
                String value = matcher.group(2);
                if (name == null) continue;
                if (Objects.equals(name, githubConst.getArticleIdProp())) {
                    article.blogId = value;
                } else if (Objects.equals(name, githubConst.getOriginIdProp())) {
                    article.originId = value;
                } else if (Objects.equals(name, githubConst.getPathProp())) {
                    article.path = folder + "/" + value;
                }
            }
            Matcher matcher2 = bp.matcher(fullTag);
            if (matcher2.find()) {
                article.blogName = matcher2.group(1);
            }
            article.ignore = headTag.contains(githubConst.getIgnoreProp());
            list.add(article);
            return fullTag;
        });
        return list;
    }

    class GithubArticle {
        String blogName;
        String blogId;
        String originId;
        String path;
        Boolean ignore;
        String content;
        Set<String> refIdSet = new HashSet<>();
    }
}
