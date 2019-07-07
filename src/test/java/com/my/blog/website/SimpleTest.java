package com.my.blog.website;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

public class SimpleTest {

    public static void main(String[] args) throws IOException {
        String filePath = "/data/blog/git/uetty.github.io/./blog/./bog/Vince Style.xml";
        File file = new File(filePath);
        System.out.println(file.exists());
        filePath = URLDecoder.decode(filePath,"UTF-8");
        System.out.println((file = new File(filePath)).exists());
    }
}
