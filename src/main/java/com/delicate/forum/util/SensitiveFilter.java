package com.delicate.forum.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    private TrieNode rootNode = new TrieNode();

    public String filterString(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        TrieNode tempNode = rootNode;
        int begin = 0, end = 0;
        StringBuilder sb = new StringBuilder();
        while (end < text.length()) {
            char c = text.charAt(end);
            if (isSymbol(c)) {
                if (begin == end) {
                    sb.append(c);
                    begin++;
                }
                end++;
                continue;
            }

            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                sb.append(text.charAt(begin));
                end = ++begin;
                tempNode = rootNode;
            } else if (tempNode.isWordEnd()) {
                sb.append(REPLACEMENT);
                begin = ++end;
                tempNode = rootNode;
            } else {
                end++;
            }
        }

        return sb.append(text.substring(begin)).toString();
    }

    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiNumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    @PostConstruct
    private void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(is))
        ) {
            String keyword;
            while ((keyword = br.readLine()) != null) {
                this.addSensitiveWord(keyword);
            }
        } catch (IOException e) {
            logger.error("Load sensitive words file failed");
        }

    }

    private void addSensitiveWord(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode currNode = tempNode.getSubNode(c);
            if (currNode == null) {
                currNode = new TrieNode();
                tempNode.addSubNode(c, currNode);
            }

            tempNode = currNode;
            if (i == keyword.length() - 1) {
                tempNode.setWordEnd(true);
            }
        }
    }

    private class TrieNode {

        private boolean isWordEnd = false;
        private Map<Character, TrieNode> subNodes = new HashMap<>();


        public boolean isWordEnd() {
            return isWordEnd;
        }

        public void setWordEnd(boolean wordEnd) {
            isWordEnd = wordEnd;
        }

        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
