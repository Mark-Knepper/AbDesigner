/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nih.nhlbi.abdesigner;

/**
 *
 * @author Pisitkun
 */
public class HeatMapArguments {

    private final String inputType;
    private final String entryName;
    private final String inputSequence;
    private final int inputSequenceLength;
    private final int spanSetting;
    private final char[] segmaskerChar;
    private final double[] arrIgScoreHeatMap;
    private final double[] arrIgScore;
    private final String[] igScoreRank;
    private final double[] arrUniquenessHeatMap;
    private final String[] sortedAllUniquenessMatchList;
    private final double[] arrConservationHeatMap;
    private final String[] sortedAllConservationMatchList;
    private final String[] peptide;
    private final Object[] arrayAllRegion;
    private final Object[] arrayAllSite;
    private final String[] arrayRegionSite;
    private final boolean[] arrayAllRegionSelectedState;
    private final boolean[] arrayAllSiteSelectedState;
    private final boolean hasTM;
    private final int arrayTailLoop;
    private final double[] arrayKDHeatMap;
    private final double[] arrayKD;
    private final String[] structureArray;
    private final String outputText;
    private final String[][] arr2DIgScore;
    private final String[][] arr2DUniqueness;
    private final String[][] arr2DConservation;

    HeatMapArguments(
            String inputType,
            String entryName,
            String inputSequence,
            int inputSequenceLength,
            int spanSetting,
            char[] segmaskerChar,
            double[] arrIgScoreHeatMap,
            double[] arrIgScore,
            String[] igScoreRank,
            double[] arrUniquenessHeatMap,
            String[] sortedAllUniquenessMatchList,
            double[] arrConservationHeatMap,
            String[] sortedAllConservationMatchList,
            String[] peptide,
            Object[] arrayAllRegion,
            Object[] arrayAllSite,
            String[] arrayRegionSite,
            boolean[] arrayAllRegionSelectedState,
            boolean[] arrayAllSiteSelectedState,
            boolean hasTM,
            int arrayTailLoop,
            double[] arrayKDHeatMap,
            double[] arrayKD,
            String[] structureArray,
            String outputText,
            String[][] arr2DIgScore,
            String[][] arr2DUniqueness,
            String[][] arr2DConservation) {
        this.inputType = inputType;
        this.entryName = entryName;
        this.inputSequence = inputSequence;
        this.inputSequenceLength = inputSequenceLength;
        this.spanSetting = spanSetting;
        this.segmaskerChar = segmaskerChar;
        this.arrIgScoreHeatMap = arrIgScoreHeatMap;
        this.arrIgScore = arrIgScore;
        this.igScoreRank = igScoreRank;
        this.arrUniquenessHeatMap = arrUniquenessHeatMap;
        this.sortedAllUniquenessMatchList = sortedAllUniquenessMatchList;
        this.arrConservationHeatMap = arrConservationHeatMap;
        this.sortedAllConservationMatchList = sortedAllConservationMatchList;
        this.peptide = peptide;
        this.arrayAllRegion = arrayAllRegion;
        this.arrayAllSite = arrayAllSite;
        this.arrayRegionSite = arrayRegionSite;
        this.arrayAllRegionSelectedState = arrayAllRegionSelectedState;
        this.arrayAllSiteSelectedState = arrayAllSiteSelectedState;
        this.hasTM = hasTM;
        this.arrayTailLoop = arrayTailLoop;
        this.arrayKDHeatMap = arrayKDHeatMap;
        this.arrayKD = arrayKD;
        this.structureArray = structureArray;
        this.outputText = outputText;
        this.arr2DIgScore = arr2DIgScore;
        this.arr2DUniqueness = arr2DUniqueness;
        this.arr2DConservation = arr2DConservation;
    }

    public String[][] getArr2DConservation() {
        return arr2DConservation;
    }

    public String[][] getArr2DIgScore() {
        return arr2DIgScore;
    }

    public String[][] getArr2DUniqueness() {
        return arr2DUniqueness;
    }

    public double[] getArrConservationHeatMap() {
        return arrConservationHeatMap;
    }

    public double[] getArrIgScore() {
        return arrIgScore;
    }

    public double[] getArrIgScoreHeatMap() {
        return arrIgScoreHeatMap;
    }

    public double[] getArrUniquenessHeatMap() {
        return arrUniquenessHeatMap;
    }

    public Object[] getArrayAllRegion() {
        return arrayAllRegion;
    }

    public boolean[] getArrayAllRegionSelectedState() {
        return arrayAllRegionSelectedState;
    }

    public Object[] getArrayAllSite() {
        return arrayAllSite;
    }

    public boolean[] getArrayAllSiteSelectedState() {
        return arrayAllSiteSelectedState;
    }

    public double[] getArrayKD() {
        return arrayKD;
    }

    public double[] getArrayKDHeatMap() {
        return arrayKDHeatMap;
    }

    public String[] getArrayRegionSite() {
        return arrayRegionSite;
    }

    public int getArrayTailLoop() {
        return arrayTailLoop;
    }

    public String getEntryName() {
        return entryName;
    }

    public boolean isHasTM() {
        return hasTM;
    }

    public String[] getIgScoreRank() {
        return igScoreRank;
    }

    public String getInputSequence() {
        return inputSequence;
    }

    public int getInputSequenceLength() {
        return inputSequenceLength;
    }

    public String getInputType() {
        return inputType;
    }

    public String getOutputText() {
        return outputText;
    }

    public String[] getPeptide() {
        return peptide;
    }

    public char[] getSegmaskerChar() {
        return segmaskerChar;
    }

    public String[] getSortedAllConservationMatchList() {
        return sortedAllConservationMatchList;
    }

    public String[] getSortedAllUniquenessMatchList() {
        return sortedAllUniquenessMatchList;
    }

    public int getSpanSetting() {
        return spanSetting;
    }

    public String[] getStructureArray() {
        return structureArray;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        appendString(sb, "inputType", inputType); sb.append(",");
        appendString(sb, "entryName", entryName); sb.append(",");
        appendString(sb, "inputSequence", inputSequence); sb.append(",");
        sb.append("\"inputSequenceLength\":").append(inputSequenceLength).append(",");
        sb.append("\"spanSetting\":").append(spanSetting).append(",");
        // segmaskerChar as string
        sb.append("\"segmaskerChar\":\"");
        for (char c : segmaskerChar) {
            sb.append(escapeJsonChar(c));
        }
        sb.append("\",");
        appendDoubleArray(sb, "arrIgScoreHeatMap", arrIgScoreHeatMap); sb.append(",");
        appendDoubleArray(sb, "arrIgScore", arrIgScore); sb.append(",");
        appendStringArray(sb, "igScoreRank", igScoreRank); sb.append(",");
        appendDoubleArray(sb, "arrUniquenessHeatMap", arrUniquenessHeatMap); sb.append(",");
        appendStringArray(sb, "sortedAllUniquenessMatchList", sortedAllUniquenessMatchList); sb.append(",");
        appendDoubleArray(sb, "arrConservationHeatMap", arrConservationHeatMap); sb.append(",");
        appendStringArray(sb, "sortedAllConservationMatchList", sortedAllConservationMatchList); sb.append(",");
        appendStringArray(sb, "peptide", peptide); sb.append(",");
        // arrayAllRegion and arrayAllSite as string arrays
        sb.append("\"arrayAllRegion\":[");
        for (int i = 0; i < arrayAllRegion.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(arrayAllRegion[i].toString())).append("\"");
        }
        sb.append("],");
        sb.append("\"arrayAllSite\":[");
        for (int i = 0; i < arrayAllSite.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(arrayAllSite[i].toString())).append("\"");
        }
        sb.append("],");
        appendStringArray(sb, "arrayRegionSite", arrayRegionSite); sb.append(",");
        appendBooleanArray(sb, "arrayAllRegionSelectedState", arrayAllRegionSelectedState); sb.append(",");
        appendBooleanArray(sb, "arrayAllSiteSelectedState", arrayAllSiteSelectedState); sb.append(",");
        sb.append("\"hasTM\":").append(hasTM).append(",");
        sb.append("\"arrayTailLoop\":").append(arrayTailLoop).append(",");
        appendDoubleArray(sb, "arrayKDHeatMap", arrayKDHeatMap); sb.append(",");
        appendDoubleArray(sb, "arrayKD", arrayKD); sb.append(",");
        appendStringArray(sb, "structureArray", structureArray); sb.append(",");
        appendString(sb, "outputText", outputText);
        sb.append("}");
        return sb.toString();
    }

    private static void appendString(StringBuilder sb, String key, String value) {
        sb.append("\"").append(key).append("\":\"").append(escapeJson(value)).append("\"");
    }

    private static void appendDoubleArray(StringBuilder sb, String key, double[] arr) {
        sb.append("\"").append(key).append("\":[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        sb.append("]");
    }

    private static void appendStringArray(StringBuilder sb, String key, String[] arr) {
        sb.append("\"").append(key).append("\":[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(arr[i])).append("\"");
        }
        sb.append("]");
    }

    private static void appendBooleanArray(StringBuilder sb, String key, boolean[] arr) {
        sb.append("\"").append(key).append("\":[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        sb.append("]");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            sb.append(escapeJsonChar(s.charAt(i)));
        }
        return sb.toString();
    }

    private static String escapeJsonChar(char c) {
        switch (c) {
            case '\\': return "\\\\";
            case '"': return "\\\"";
            case '\n': return "\\n";
            case '\r': return "\\r";
            case '\t': return "\\t";
            case '<': return "\\u003c";
            default: return Character.toString(c);
        }
    }
}
