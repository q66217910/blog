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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class GithubQuartzJob extends QuartzJobBean {

    private static final Logger LOG = LoggerFactory.getLogger(GithubQuartzJob.class);

    @Autowired
    private ContentVoMapper contentVoMapper;
    @Autowired
    private IMetaService metasService;

    @Value("${file.path}/git")
    private String savePath;
    @Autowired
    private GithubConst githubConst;

    private long lastExecuted = 0L;
    private boolean isExecuting = false;
    private static final Object EXECUTE_INFO_LOCK = new Object();
    private static final Object DOWNLOAD_LOCK = new Object();
    private SimpleDateFormat readDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat slugDateFormat = new SimpleDateFormat("yyyy-MM-dd");


    @SuppressWarnings("UnusedReturnValue")
    private File unzipToFolder(File zipFile, File targetFolder) throws IOException {
        File tempFolder = null;
        try {
            tempFolder = new File(savePath + "/" + randomName());
            ZipUtils.unZip(zipFile, tempFolder.getAbsolutePath());

            File[] listFiles = tempFolder.listFiles();
            if (listFiles == null || listFiles.length != 1) {
                LOG.error("some error occur on download zip file. child files is {}", listFiles == null ? "null" : "[]");
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
        return String.format("%04d%02d%02d-%02d%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    private String randomName () {
        return UUID.randomUUID().toString().substring(24) + "-" + getDateStr();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File downloadGitProject() throws IOException {
        FileOutputStream fos = null;
        File file;
        synchronized (DOWNLOAD_LOCK) {
            try {
                try {
                    LOG.info("preheat git download url");
                    HttpClientUtil.doGetLoad(githubConst.getPreUrl(), null, null);
                } catch (Exception ignore) {
                }

                LOG.info("download git repository");
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
        }
        return file;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        synchronized (EXECUTE_INFO_LOCK) {
            if (isExecuting && System.currentTimeMillis() - lastExecuted < 6 * 60 * 1000) {
                return;
            }
            lastExecuted = System.currentTimeMillis();
            isExecuting = true;
        }

        LOG.debug("EXECUTE GITHUB BLOG UPDATE");
        File zipFile = null;
        try {
            zipFile = downloadGitProject();

            File file = new File(savePath + "/" + githubConst.getName());
            unzipToFolder(zipFile, file);

            List<GithubArticle> githubArticles = readGithubArticles(file);
            if (githubArticles == null) {
                LOG.warn("github article not found");
                return;
            }

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
                    contentVo.setBlogNumber(Objects.requireNonNull(article).rid);
                    contentVoMapper.updateByPrimaryKey(contentVo);
                }
            }

            // delete not exist
            List<String> currentBlogIds = githubArticles.stream().map(article -> article.rid).collect(Collectors.toList());
            if (currentBlogIds.size() > 0) {
                contentVoMapper.deleteByBlogNumberNotIn(currentBlogIds);
            }

            List<GithubArticle> cloneArticles = new ArrayList<>(githubArticles);
            githubArticles.forEach(article -> {
                try {
                    ContentVo contentVo = contentVoMapper.selectByBlogNumberWithBLOBs(article.rid);
                    resolveFileContent(article, cloneArticles, file);

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
            isExecuting = false;
        }
    }

    private void replaceContentRef (GithubArticle art, List<ContentVo> refBlogList) {
        try {
            ContentVo contentVo = contentVoMapper.selectByBlogNumberWithBLOBs(art.rid);
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
        contents.setTitle(article.title);
        contents.setContent(article.content);
        contents.setAuthorId(githubConst.getAuthorId());
        contents.setSlug(article.permalink);
        contents.setIsShadow(article.isShadow);
        contents.setTags(String.join(",", article.keywords.toArray(new String[0])));
        if (article.tags != null) {
            contents.setCategories(article.tags);
        } else {
            contents.setCategories("默认分类");
        }
        contents.setCreated(DateKit.getUnixTimeByDate(article.createTime));
        contents.setBlogNumber(article.rid);
        contents.setCommitType(ContentVo.COMMIT_TYPE_GITHUB);
        contents.setType(Types.ARTICLE.getType());
        contents.setStatus(Types.PUBLISH.getType());
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

    private void resolveFileContent(GithubArticle article, List<GithubArticle> possibleRefArticle, File blogGitSourceFolder) throws IOException {
        File file = new File(article.path);
        File postFolder = new File(blogGitSourceFolder, githubConst.getPostFolderName());
        File attachFolder = new File(blogGitSourceFolder, githubConst.getAttachFolderName());
        String realAttachFolderPath = attachFolder.getCanonicalPath();


        String s1 = article.content;
        StringBuilder sb = new StringBuilder().append(s1);

        Pattern linkPatter = Pattern.compile("\\[(.*?)]\\([<]?(.*?)[>]?\\)");
        Matcher matcher = linkPatter.matcher(sb.toString());
        Map<String, String> attachMap = new HashMap<>();
        Map<String, String> refMap = new HashMap<>();

        while (matcher.find()) {
            String link = matcher.group(2);
            if (StringUtils.isBlank(link)) continue;
            if (StringUtil.isUrl(link)) continue;
            File linkFile = new File(postFolder, link);
            linkFile = linkFile.getCanonicalFile();

            String refId = null;
            for (GithubArticle ref : possibleRefArticle) {
                File file1 = new File(ref.path);
                if (FileTool.fileEquals(linkFile, file1)) {
                    refId = ref.rid;
                    break;
                }
            }
            if (refId != null) {
                refMap.put(link, refId);
                continue;
            }

            String realLink = linkFile.getAbsolutePath();
            if (realLink.startsWith(realAttachFolderPath)) {
                String relatePath = realLink.substring(realAttachFolderPath.length());
                attachMap.put(link, "/" + githubConst.getAttachPath() + relatePath);
            }
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
            File attach = new File(postFolder, key);
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
        contentVo.setTitle(article.title);
        contentVo.setContent(article.content);
        contentVo.setAuthorId(githubConst.getAuthorId());
        contentVo.setSlug(article.permalink);
        contentVo.setIsShadow(article.isShadow);
        contentVo.setCreated(DateKit.getUnixTimeByDate(article.createTime));

        String tags = String.join(",", article.keywords.toArray(new String[0]));
        boolean updateTags = !Objects.equals(tags, contentVo.getTags());
        contentVo.setTags(tags);

        String categories;
        if (article.tags != null) {
            categories = article.tags;
        } else {
            categories = "默认分类";
        }
        boolean updateCategories = !Objects.equals(categories, contentVo.getCategories());
        contentVo.setCategories(categories);

        contentVoMapper.updateByPrimaryKeyWithBLOBs(contentVo);

        if (updateTags) {
            metasService.saveMetas(contentVo.getCid(), tags, Types.TAG.getType());
        }
        if (updateCategories) {
            metasService.saveMetas(contentVo.getCid(), categories, Types.CATEGORY.getType());
        }
    }

    private List<GithubArticle> readGithubArticles(File blogGitSourceFolder) throws IOException, ParseException {
        List<GithubArticle> list = new ArrayList<>();

        File postFolder = new File(blogGitSourceFolder, githubConst.getPostFolderName());
        if (!postFolder.exists()) {
            throw new RuntimeException("post folder not found");
        }

        File[] files = postFolder.listFiles();
        if (files == null) {
            return list;
        }

        for (File child : files) {
            GithubArticle article = getArticle(child);
            if (article != null) {
                article.path = child.getAbsolutePath();
                list.add(article);
            }
        }

        return list;
    }

    private GithubArticle getArticle(File articleFile) throws IOException, ParseException {
        String string = FileTool.readAsString(articleFile);
        string = StringUtil.trimStart(string);

        return analyzeContent(string);
    }

    @SuppressWarnings("SameParameterValue")
    private String[] splitFirst(String string, String separator) {
        int i = string.indexOf(separator);
        if (i != -1) {
            String[] arr = new String[2];
            arr[0] = string.substring(0, i);
            arr[1] = string.substring(i + separator.length());
            return arr;
        } else {
            String[] arr = new String[1];
            arr[0] = string;
            return arr;
        }
    }

    private GithubArticle analyzeContent(String string) throws ParseException {
        GithubArticle article = new GithubArticle();

        int i = lookLineEnd(string);
        if (i == -1) {
            throw new RuntimeException("analyze content failed");
        }
        String line = string.substring(0, i);
        if (!isDescriptSeparatorLine(line)) {
            throw new RuntimeException("analyze content failed");
        }
        string = string.substring(i + 1);
        List<String> descriptLines = new ArrayList<>();
        while (string.length() > 0) {
            i = lookLineEnd(string);
            if (i == -1) {
                break;
            }
            line = string.substring(0, i);
            string = string.substring(i + 1);

            if (isDescriptSeparatorLine(line)) {
                article.content  = StringUtil.trimStart(string);
                break;
            }
            descriptLines.add(line);
        }

        if (article.content == null) {
            throw new RuntimeException("analyze content failed");
        }

        for (String desLine : descriptLines) {
            String[] split = splitFirst(desLine, ":");
            if (split.length == 2) {
                String key = split[0].trim();
                String value = split[1].trim();
                if (Objects.equals(key, githubConst.getAttrTitle())) {
                    value = value.replace("\\ ", " ");
                    article.title = value;
                } else if (Objects.equals(key, githubConst.getAttrDate())) {
                    article.createTime = readDateFormat.parse(value);
                } else if (Objects.equals(key, githubConst.getAttrTag())) {
                    article.tags = value;
                } else if (Objects.equals(key, githubConst.getAttrPermalink())) {
                    article.permalink = value;
                } else if (Objects.equals(key, githubConst.getAttrKeywords())) {
                    String[] keywords = value.split(",");
                    for (String keyword : keywords) {
                        if (StringUtil.isNotBlank(keyword)) {
                            article.keywords.add(keyword.trim());
                        }
                    }
                } else if (Objects.equals(key, githubConst.getAttrRid())) {
                    article.rid = value;
                } else if (Objects.equals(key, githubConst.getAttrOriginId())) {
                    article.originId = value;
                } else if (Objects.equals(key, githubConst.getAttrShadow())) {
                    article.isShadow = Objects.equals("true", value);
                }
            }
        }

		System.out.println("----------");
        System.out.println("title: " + article.title);
        System.out.println("createTime: " + article.createTime);
        System.out.println("rid: " + article.rid);
		System.out.println("isShadow: " + article.isShadow);
        if (StringUtil.isBlank(article.title)
                || article.createTime == null || StringUtil.isBlank(article.rid)) {
            LOG.warn("article info incomplete");
            return null;
        }
        if (article.permalink != null) {
            article.permalink = slugDateFormat.format(article.createTime) + "-" + article.permalink;
        }
        return article;
    }

    private int lookLineEnd(String string) {
        return string.indexOf('\n');
    }

    private boolean isDescriptSeparatorLine(String line) {
        if (line.length() < 3) {
            return false;
        }
        line = StringUtil.trimEnd(line);
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) != '-') {
                return false;
            }
        }
        return true;
    }
//    private List<GithubArticle> readGithubArticleList(File blogFolder) throws IOException {
//        File[] files = blogFolder.listFiles();
//        if (files == null) return null;
//        File postFolder = null;
//        for (File f : files) {
//            if (Objects.equals(f.getName(), githubConst.getPostFolderName())) {
//                postFolder = f;
//                break;
//            }
//        }
//        if (postFolder == null) {
//            throw new RuntimeException("post folder not found");
//        }
//
//        List<GithubArticle> list = new ArrayList<>();
//
//
//
//        String string = FileTool.readAsString(postFolder);
//        Pattern pp = Pattern.compile(PROP_REGEX);
//        Pattern bp = Pattern.compile(NAME_REGEX);
//        String folder = blogFolder.getAbsolutePath();
//        HtmlUtil.tagReplace(string, "a", (fullTag, headTag) -> {
//            Matcher matcher = pp.matcher(fullTag);
//            GithubArticle article = new GithubArticle();
//            while (matcher.find()) {
//                String name = matcher.group(1);
//                String value = matcher.group(2);
//                if (name == null) continue;
//                if (Objects.equals(name, githubConst.getArticleIdProp())) {
//                    article.blogId = value;
//                } else if (Objects.equals(name, githubConst.getOriginIdProp())) {
//                    article.originId = value;
//                } else if (Objects.equals(name, githubConst.getPathProp())) {
//                    article.path = folder + "/" + value;
//                }
//            }
//            Matcher matcher2 = bp.matcher(fullTag);
//            if (matcher2.find()) {
//                article.blogName = matcher2.group(1);
//            }
//            article.ignore = headTag.contains(githubConst.getIgnoreProp());
//            list.add(article);
//            return fullTag;
//        });
//        return list;
//    }

    class GithubArticle {
        String title;
        Date createTime;
        String tags;
        String permalink;
        List<String> keywords = new ArrayList<>();
        String rid;
        String originId;
        String topic;
        Boolean isShadow;

        String content;
        Set<String> refIdSet = new HashSet<>();

        String path;

    }
}
