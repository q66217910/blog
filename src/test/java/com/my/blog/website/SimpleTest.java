package com.my.blog.website;

import com.my.blog.website.utils.FileTool;
import com.my.blog.website.utils.HttpClientUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimpleTest {

    public static void main(String[] args) throws IOException {
        Map<String, Object> header = new HashMap<>();
        header.put("User-Agent", "Wget/1.17.1 (linux-gnu)");
        header.put("Accept", "*/*");
        header.put("Connection", "Keep-Alive");
        String preUrl = "https://github.com/Uetty/uetty.github.io";
//        HttpClientUtil.HttpResponseVo responseVo0 = HttpClientUtil.doGet(preUrl, header, null);
//        Map<String, List<String>> headers = responseVo0.getHeaders();
//
//        System.out.println(headers);

        String url = "https://codeload.github.com/Uetty/uetty.github.io/zip/master";
        header.put("Host", "codeload.github.com");
        HttpClientUtil.HttpResponseVo responseVo = HttpClientUtil.doGetLoad(url, header, null);
        InputStream inputStream = responseVo.getInputStream();
        if (inputStream == null) throw new RuntimeException("DOWNLOAD FAILED");
        String zipFileName = UUID.randomUUID() + ".zip";
        File file = new File("/data/" + zipFileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        } else {
            file.delete();
        }
        file.createNewFile();
        FileTool.writeFromInputStream(new FileOutputStream(file), inputStream);
    }
}
