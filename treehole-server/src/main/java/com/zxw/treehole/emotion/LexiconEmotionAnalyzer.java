package com.zxw.treehole.emotion;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于中文情感词典的粗粒度分析（MVP，可替换为深度学习模型）
 */
@Component
public class LexiconEmotionAnalyzer {

    private static final String[] POSITIVE = {"开心", "高兴", "棒", "不错", "谢谢", "喜欢", "轻松", "有希望", "充实", "顺利", "加油", "好耶", "满足", "温暖"};
    private static final String[] NEGATIVE = {"难过", "糟糕", "失败", "没用", "废物", "讨厌", "烦", "累死了", "崩溃", "绝望", "孤独", "害怕", "担心"};
    private static final String[] ANXIOUS = {"焦虑", "紧张", "慌", "睡不着", "考研", "考不上", "来不及", "压力", "内耗", "发慌", "心悸"};
    private static final String[] SAD = {"悲伤", "想哭", "失落", "空虚", "没意思", "抑郁", "低落", "心痛"};
    private static final String[] ANGRY = {"生气", "愤怒", "恨", "烦死了", "受不了", "恼火", "暴躁"};
    private static final String[] SUPPRESSED = {"压抑", "憋", "说不出", "忍着", "硬撑", "麻木", "不想说"};

    public LexiconEmotionResult analyze(String text) {
        LexiconEmotionResult r = new LexiconEmotionResult();
        if (text == null || text.isBlank()) {
            r.setSentimentScore(BigDecimal.ZERO);
            r.setEmotionLabel("NEUTRAL");
            return r;
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        int pos = countHits(text, POSITIVE);
        int neg = countHits(text, NEGATIVE);
        int anx = countHits(text, ANXIOUS);
        int sad = countHits(text, SAD);
        int ang = countHits(text, ANGRY);
        int sup = countHits(text, SUPPRESSED);
        counts.put("positive", pos);
        counts.put("negative", neg);
        counts.put("anxious", anx);
        counts.put("sad", sad);
        counts.put("angry", ang);
        counts.put("suppressed", sup);
        r.setHitCounts(counts);

        double raw = pos * 0.12 - neg * 0.14 - anx * 0.16 - sad * 0.15 - ang * 0.13 - sup * 0.14;
        raw = Math.max(-1.0, Math.min(1.0, raw));
        r.setSentimentScore(BigDecimal.valueOf(raw).setScale(3, RoundingMode.HALF_UP));

        String label = pickLabel(pos, neg, anx, sad, ang, sup, raw);
        r.setEmotionLabel(label);
        return r;
    }

    private static int countHits(String text, String[] words) {
        int n = 0;
        for (String w : words) {
            if (w.isEmpty()) {
                continue;
            }
            int from = 0;
            while (true) {
                int i = text.indexOf(w, from);
                if (i < 0) {
                    break;
                }
                n++;
                from = i + w.length();
            }
        }
        return n;
    }

    private static String pickLabel(int pos, int neg, int anx, int sad, int ang, int sup, double raw) {
        if (pos > 0 && raw > 0.15 && neg + anx + sad + ang + sup == 0) {
            return "POSITIVE";
        }
        int maxEmo = Math.max(Math.max(anx, sad), Math.max(ang, sup));
        if (maxEmo == 0 && Math.abs(raw) < 0.08) {
            return "NEUTRAL";
        }
        if (anx >= maxEmo && anx > 0) {
            return "ANXIOUS";
        }
        if (ang >= maxEmo && ang > 0) {
            return "ANGRY";
        }
        if (sup >= maxEmo && sup > 0) {
            return "SUPPRESSED";
        }
        if (sad >= maxEmo && sad > 0) {
            return "SAD";
        }
        if (raw > 0.08) {
            return "POSITIVE";
        }
        if (raw < -0.08) {
            return "SAD";
        }
        return "NEUTRAL";
    }
}
