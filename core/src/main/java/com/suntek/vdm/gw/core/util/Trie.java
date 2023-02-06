package com.suntek.vdm.gw.core.util;

import com.suntek.vdm.gw.common.pojo.GwId;

import java.util.HashMap;
import java.util.Map;

public class Trie {

    private final Map<Character, Trie> nextNodes = new HashMap<>();
    private boolean isWordEnd = false;
    private GwId gwId;

    public Trie() {
    }

    /**
     * Inserts a word into the trie.
     */
    public void insert(String word, GwId gwId) {
        Trie current = this;
        Trie next;
        for (char c : word.toCharArray()) {
            next = current.nextNodes.getOrDefault(c, new Trie());
            current.nextNodes.put(c, next);
            current = next;
        }
        current.isWordEnd = true;
        current.gwId = gwId;
    }

    /**
     * Returns if the word is in the trie.
     */
    public GwId search(String word) {
        Trie current = this;
        for (char c : word.toCharArray()) {
            if (!current.nextNodes.containsKey(c)) {
                break;
            }
            current = current.nextNodes.get(c);
        }
        return current.gwId;
    }

    /**
     * Returns if there is any word in the trie that starts with the given prefix.
     */
    public boolean startsWith(String prefix) {
        Trie current = this;
        for (char c : prefix.toCharArray()) {
            if (!current.nextNodes.containsKey(c)) {
                return false;
            }
            current = current.nextNodes.get(c);
        }
        return true;
    }
}