package org.a8043.simpleCode.util;

import java.util.ArrayList;
import java.util.List;

public class DiffCalculator {
    private static int[][] computeLCSMatrix(List<String> oldLines, List<String> newLines) {
        int m = oldLines.size();
        int n = newLines.size();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (oldLines.get(i - 1).equals(newLines.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp;
    }

    private static List<String> backtrackLCS(List<String> oldLines, List<String> newLines, int[][] dp) {
        List<String> lcs = new ArrayList<>();
        int i = oldLines.size(), j = newLines.size();

        while (i > 0 && j > 0) {
            if (oldLines.get(i - 1).equals(newLines.get(j - 1))) {
                lcs.addFirst(oldLines.get(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return lcs;
    }

    public static List<LineChange> computeDiff(List<String> oldLines, List<String> newLines) {
        int[][] dp = computeLCSMatrix(oldLines, newLines);
        List<String> lcs = backtrackLCS(oldLines, newLines, dp);

        List<LineChange> changes = new ArrayList<>();
        int i = 0, j = 0;
        int lcsIdx = 0;

        int oldLineNum = 1;
        int newLineNum = 1;

        while (i < oldLines.size() || j < newLines.size()) {
            if (lcsIdx < lcs.size() &&
                i < oldLines.size() && oldLines.get(i).equals(lcs.get(lcsIdx)) &&
                j < newLines.size() && newLines.get(j).equals(lcs.get(lcsIdx))) {

                changes.add(new LineChange(LineChange.Action.EQUAL, oldLineNum, newLineNum, oldLines.get(i)));
                i++;
                j++;
                oldLineNum++;
                newLineNum++;
                lcsIdx++;
            } else if (i < oldLines.size() && (lcsIdx >= lcs.size() || !oldLines.get(i).equals(lcs.get(lcsIdx)))) {
                changes.add(new LineChange(LineChange.Action.DELETE, oldLineNum, -1, oldLines.get(i)));
                i++;
                oldLineNum++;
            } else if (j < newLines.size() && (lcsIdx >= lcs.size() || !newLines.get(j).equals(lcs.get(lcsIdx)))) {
                changes.add(new LineChange(LineChange.Action.INSERT, -1, newLineNum, newLines.get(j)));
                j++;
                newLineNum++;
            }
        }

        return changes;
    }
}
