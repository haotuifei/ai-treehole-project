package com.zxw.treehole.ai.risk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentResult {

    /** LOW / MEDIUM / HIGH */
    private String level;
    private List<String> matchedKeywords = new ArrayList<>();
}
