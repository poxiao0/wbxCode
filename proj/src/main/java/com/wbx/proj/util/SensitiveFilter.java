package com.wbx.proj.util;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    private static final String REPLACEMENT = "***";
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词失败：" + e.getMessage());
        }
    }

    public void addKeyword(String s) {
        TrieNode cur = rootNode;
        for (char c : s.toCharArray()) {
            TrieNode sub = cur.getSubNode(c);
            if (sub == null) {
                sub = new TrieNode();
                cur.setSubNode(c, sub);
            }
            cur = sub;
        }
        cur.setIsEnd(true);
    }

    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        TrieNode cur = rootNode;
        int p = 0;
        int pos = 0;
        StringBuilder sb = new StringBuilder();

        while (pos < text.length()) {
            char c = text.charAt(pos);

            // 跳过符号
            if (isSymbol(c)) {
                if (cur == rootNode) {
                    sb.append(c);
                    p++;
                }
                pos++;
                continue;
            }

            // 下级检查
            cur = cur.getSubNode(c);
            if (cur == null) {
                sb.append(text.charAt(p));
                pos = ++p;
                cur = rootNode;
            } else if (cur.getIsEnd()) {
                sb.append(REPLACEMENT);
                p = ++pos;
                cur = rootNode;
            } else {
                pos++;
            }
        }

        sb.append(text.substring(p));
        return sb.toString();
    }

    public boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    private class TrieNode {

        private boolean isEnd = false;
        private Map<Character, TrieNode> subNode = new HashMap<>();

        public boolean getIsEnd() {
            return isEnd;
        }

        public void setIsEnd(boolean end) {
            isEnd = end;
        }

        public TrieNode getSubNode(Character k) {
            return subNode.get(k);
        }

        public void setSubNode(Character k, TrieNode v) {
            subNode.put(k, v);
        }


    }
}
