package com.zxw.treehole.emotion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultEmotionAnalysisEngine implements EmotionAnalysisEngine {

    private final LexiconEmotionAnalyzer lexiconEmotionAnalyzer;

    @Override
    public LexiconEmotionResult analyze(String text) {
        return lexiconEmotionAnalyzer.analyze(text);
    }
}
