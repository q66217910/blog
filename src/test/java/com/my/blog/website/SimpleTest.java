package com.my.blog.website;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleTest {

    public static void main(String[] args) throws IOException {
        File file = new File("../uetty.github.io/./blog/../blog/2018-09-21_shadowsocks.md");
        File file1 = new File("/data/blog/git/uetty.github.io/./blog/2018-09-21_shadowsocks.md");

        System.out.println(file.exists());
        System.out.println(file1.exists());

        System.out.println(file.getCanonicalFile().getAbsolutePath());
        System.out.println(file1.getCanonicalFile().getAbsolutePath());

        System.out.println(file.equals(file1));


        Map<String, String> map = new LinkedHashMap<>();

        map.put("dfafd", "gwe");
        map.put("dfafd2", "gwe");
        map.put("dfafd3", "gwe");
        map.put("dfafd4", "gwe");
        map.put("dfafd5", "gwe");

    }
}
