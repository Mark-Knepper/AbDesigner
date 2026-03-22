/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nih.nhlbi.abdesigner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Pisitkun
 */
class AbDesigner {

    private static final double MAX_KD = 4.5;
    private static final double TAIL_BONUS = 1.5;
    private static final String[] STRUCTURE = {" ", "alpha helix", "beta sheet", "strong beta turn", "weak beta turn"};
    private static final int UNIQUENESS_NUMBER = 3;
    private static final int SPECIES_NUMBER = 3;

    static HeatMapArguments getHeatMapArguments(String inputType, String entryName, String swissProtContent, String segmaskerSequence, int spanSetting, int inputSequenceLength, int numberOfSpan, int matchSpan, ArrayList<ArrayList<String>> uniquenessDatabase, ArrayList<ArrayList<String>> conservationDatabase) {
        String inputSequence = segmaskerSequence.toUpperCase();
        int leftWingSpan = (spanSetting - 1) / 2;
        int rightWingSpan = spanSetting / 2;
        //Get KD
        double[] arrayKD = KyteDoolittle.getArrayKyteDoolittle(inputSequence, spanSetting, inputSequenceLength, numberOfSpan);
        //Get modified CF
        Double[] arrayCF = ChouFasman.getArrayModifiedChouFasman(inputSequence, spanSetting, inputSequenceLength, numberOfSpan);
        //Get low-complexity sequence
        char[] segmaskerChar = new char[inputSequenceLength];
        for (int i = 0; i < segmaskerChar.length; i++) {
            segmaskerChar[i] = segmaskerSequence.charAt(i);
        }
        //Get Region/Site
        ArrayList<String> listRegionSite = new ArrayList<String>();
        //Get region
        Object[] arrayAllRegion = SwissProtExtraction.getAllRegion(swissProtContent);
        StringBuilder regionHeader = new StringBuilder();
        //Check TM
        boolean hasTM = false;
        int arrayTailLoop = 0;
        for (int i = 0; i < arrayAllRegion.length; i++) {
            listRegionSite.addAll(SwissProtExtraction.getRegion(swissProtContent, arrayAllRegion[i], inputSequenceLength));
            regionHeader.append(arrayAllRegion[i]).append("\t");
            //If arrayAllRegion has Transmembrane region
            if (arrayAllRegion[i].equals("Transmembrane region")) {
                hasTM = true;
                arrayTailLoop = i;
            }
        }
        //Get site
        Object[] arrayAllSite = SwissProtExtraction.getAllSite(swissProtContent);
        StringBuilder siteHeader = new StringBuilder();
        for (int i = 0; i < arrayAllSite.length; i++) {
            listRegionSite.addAll(SwissProtExtraction.getSite(swissProtContent, arrayAllSite[i], inputSequenceLength));
            siteHeader.append(arrayAllSite[i]).append("\t");
        }
        String[] arrayRegionSite = listRegionSite.toArray(new String[0]);
        //Get tail bonus
        double[] tailBonusArray = new double[inputSequenceLength];
        Arrays.fill(tailBonusArray, 1);
        if (hasTM) {
            tailBonusArray = TailBonus.getTailBonus(arrayRegionSite, arrayTailLoop, TAIL_BONUS, spanSetting, inputSequenceLength, numberOfSpan);
        }
        //Initialize arrays for arrIgScore, arrIgScoreMap, peptide, peptideStartRange, peptideEndRange, and structureArray
        double[] arrIgScore = new double[inputSequenceLength];
        TreeMap<Integer, Double> arrIgScoreMap = new TreeMap<Integer, Double>();
        String[] peptide = new String[inputSequenceLength];
        int[] peptideStartRange = new int[inputSequenceLength]; //Start from 1 (not from 0)
        int[] peptideEndRange = new int[inputSequenceLength];
        String[] structureArray = new String[inputSequenceLength];

        //Print Table 1
        StringBuilder stringBuilder = new StringBuilder();
        String input = null;
        if (!inputType.equals("FASTA")) {
            input = entryName;
        } else {
            input = (entryName.isEmpty()) ? inputSequence : entryName;
        }
        stringBuilder.append("Input: ").append(input).append("\nAmino Acid Number: ").append(inputSequenceLength).append("\nPeptide Length: ").append(spanSetting).append("\nEpitope Length: ").append(matchSpan).append("\nTail Bonus: ").append(TAIL_BONUS).append("\nInput Sequence: ").append(inputSequence).append("\n");
        stringBuilder.append("Position\t" + "Residue\t" + "Ig-score\t" + "Average hydropathy index\t" + "<Pa>\t" + "<Pb>\t" + "<Pt>\t" + "<pt>\t" + "Prediction\t" + "Average<Pa>\t" + "Average<Pb>\t" + "Average<Pt>\t" + "Average<pt>\t" + "Low-complexity sequence\t" + "Tail bonus\t").append(regionHeader).append(siteHeader).append("\n");
        for (int i = 0; i < inputSequenceLength; i++) {
            char eachResidue = inputSequence.charAt(i);
            //Calculate IgScore
            double immunogenicityScore = ((arrayKD[i] * -1) + MAX_KD) * ((arrayCF[i + (7 * inputSequenceLength)]) / 100) * tailBonusArray[i];
            arrIgScore[i] = immunogenicityScore;
            arrIgScoreMap.put(i, immunogenicityScore);
            //Get peptide, peptideStartRange, and peptideEndRange
            if (i >= leftWingSpan && i < inputSequenceLength - rightWingSpan) {
                int startRange = i - leftWingSpan;
                int endRange = i + rightWingSpan + 1;
                peptide[i] = inputSequence.substring(startRange, endRange);
                peptideStartRange[i] = startRange + 1;
                peptideEndRange[i] = endRange;
            } else {
                peptide[i] = "";
            }
            int position = i + 1;
            //Print details per eachResidue
            stringBuilder.append(position).append("\t").append(eachResidue).append("\t").append(immunogenicityScore).append("\t").append(arrayKD[i]).append("\t");
            //Print arrayCF
            for (int j = 0; j < (arrayCF.length / inputSequenceLength); j++) {
                if (j == 4) {
                    structureArray[i] = STRUCTURE[arrayCF[i + (4 * inputSequenceLength)].intValue()];
                    stringBuilder.append(structureArray[i]).append("\t");
                } else {
                    stringBuilder.append(arrayCF[i + (j * inputSequenceLength)]).append("\t");
                }
            }
            //Print low-complexity sequence
            stringBuilder.append(segmaskerChar[i]).append("\t");
            //Print tail bonus
            stringBuilder.append(tailBonusArray[i]).append("\t");
            //Print arrayRegionSite
            for (int j = 0; j < (arrayRegionSite.length / inputSequenceLength); j++) {
                stringBuilder.append(arrayRegionSite[i + (j * inputSequenceLength)].replace("\t", ", ").replace("<br>", " ")).append("\t");
            }
            stringBuilder.append("\n");
        }
        //Prepare for heat map
        ArrayList<Map.Entry<Integer, Double>> arrIgScoreMapList = new ArrayList<Map.Entry<Integer, Double>>(arrIgScoreMap.entrySet());
        Comparator c1 = new Comparator<Map.Entry<Integer, Double>>() {

            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        };
        //Sort arrIgScoreMapList<index, IgScore> based on IgScore (Sort Ascending: smallest to largest)
        //Double.NaN is considered by this method to be equal to itself and greater than all other double values
        Collections.sort(arrIgScoreMapList, c1);
        double maxIgScore = arrIgScoreMapList.get(numberOfSpan - 1).getValue();
        double minIgScore = arrIgScoreMapList.get(0).getValue();
        //Get igScoreRank
        String[] igScoreRank = new String[inputSequenceLength];
        igScoreRank[arrIgScoreMapList.get(numberOfSpan - 1).getKey()] = Integer.toString(1);
        for (int i = numberOfSpan - 2; i >= 0; i--) {
            igScoreRank[arrIgScoreMapList.get(i).getKey()] =
                    (!arrIgScoreMapList.get(i).getValue().equals(arrIgScoreMapList.get(i + 1).getValue()))
                    ? Integer.toString(numberOfSpan - i)
                    : igScoreRank[arrIgScoreMapList.get(i + 1).getKey()];
        }
        double[] arrIgScoreHeatMap = new double[inputSequenceLength];
        double[] arrayKDHeatMap = new double[inputSequenceLength];
        //Transform values to 8-bit (0-255) scale
        for (int i = leftWingSpan; i < inputSequenceLength - rightWingSpan; i++) {
            arrIgScoreHeatMap[i] = (maxIgScore - minIgScore != 0)
                    ? (arrIgScore[i] - minIgScore) * 255 / (maxIgScore - minIgScore) : 255;
            arrayKDHeatMap[i] = (arrayKD[i] * 127.5 / MAX_KD) + 127.5;
        }
        //Assign the same value as the first one evaluated
        for (int i = 0; i < leftWingSpan; i++) {
            arrIgScoreHeatMap[i] = arrIgScoreHeatMap[leftWingSpan];
            arrayKDHeatMap[i] = arrayKDHeatMap[leftWingSpan];
        }
        //Assign the same value as the last one evaluated
        for (int i = inputSequenceLength - rightWingSpan; i < inputSequenceLength; i++) {
            arrIgScoreHeatMap[i] = arrIgScoreHeatMap[inputSequenceLength - rightWingSpan - 1];
            arrayKDHeatMap[i] = arrayKDHeatMap[inputSequenceLength - rightWingSpan - 1];
        }
        //Create arr2DIgScore (Sort Descending: largest to smallest)
        String[][] arr2DIgScore = new String[numberOfSpan][4];
        for (int i = 0; i < numberOfSpan; i++) {
            int sortedIndex = arrIgScoreMapList.get(numberOfSpan - 1 - i).getKey();
            arr2DIgScore[i][0] = igScoreRank[sortedIndex];
            arr2DIgScore[i][1] = peptide[sortedIndex];
            arr2DIgScore[i][2] = Integer.toString(peptideStartRange[sortedIndex]);
            arr2DIgScore[i][3] = Integer.toString(peptideEndRange[sortedIndex]);
//            System.out.println("igScoreRank = " + arr2DIgScore[i][0]
//                    + " peptide = " + arr2DIgScore[i][1]
//                    + " " + arr2DIgScore[i][2]
//                    + " " + arr2DIgScore[i][3]);
        }
        //Print Table 2
        /**
         *  Sort peptide, peptideStartRange, and peptideEndRange
         *  using sortedIndex based on IgScore (Sort Ascending: smallest to largest)
         *  Example: sortedArrays
         */
        String[] sortedPeptide = new String[inputSequenceLength];
        int[] sortedPeptideStartRange = new int[inputSequenceLength];
        int[] sortedPeptideEndRange = new int[inputSequenceLength];
        int[] sortedIndexArray = new int[inputSequenceLength];
        for (int i = 0; i < inputSequenceLength; i++) {
            int sortedIndex = arrIgScoreMapList.get(i).getKey();
            sortedPeptide[i] = peptide[sortedIndex];
            sortedPeptideStartRange[i] = peptideStartRange[sortedIndex];
            sortedPeptideEndRange[i] = peptideEndRange[sortedIndex];
            sortedIndexArray[i] = sortedIndex;
        }
        //Prepare an array containing matching peptides each located at position corresponding to its startRange
        int matchPerPeptide = spanSetting - matchSpan + 1;
        String[] arrMatch = new String[inputSequenceLength - matchSpan + 2]; //Example: arrMatch
        for (int i = 1; i < arrMatch.length; i++) {
            arrMatch[i] = inputSequence.substring(i - 1, i + matchSpan - 1).toUpperCase();
        }
        //Match
        String[][] arrUniquenessResults = new String[arrMatch.length][]; //Example: arrUniquenessResults
        String[][] arrConservationResults = new String[arrMatch.length][]; //Example: arrConservationResults
        for (int i = 1; i < arrMatch.length; i++) {
            ArrayList<String> uniquenessMatchList = new ArrayList<String>();
            for (ArrayList<String> arrayList : uniquenessDatabase) {
                String locus = arrayList.get(0); //get(0) = locus
                if (!locus.equals(entryName)) { //Omit inputID
                    Pattern pattern = Pattern.compile(arrMatch[i]);
                    Matcher matcher = pattern.matcher(arrayList.get(1)); //get(1) = sequence
                    int matcherStart = 0;
                    /**
                     *  Resets this matcher and then attempts to find the next subsequence of the input sequence
                     *  that matches the pattern, starting at the next index from the start index of the previous match
                     */
                    while (matcher.find(matcherStart)) {
                        matcherStart = matcher.start() + 1;
                        uniquenessMatchList.add(locus + "\t" + i + "\t" + matcherStart + "\t" + matcher.group());
                    }
                }
            }
            arrUniquenessResults[i] = uniquenessMatchList.toArray(new String[0]);
            ArrayList<String> conservationMatchList = new ArrayList<String>();
            for (ArrayList<String> arrayList : conservationDatabase) {
                String locus = arrayList.get(0); //get(0) = locus
                if (!locus.equals(entryName)) { //Omit inputID
                    Pattern pattern = Pattern.compile(arrMatch[i]);
                    Matcher matcher = pattern.matcher(arrayList.get(1)); //get(1) = sequence
                    int matcherStart = 0;
                    /**
                     *  Resets this matcher and then attempts to find the next subsequence of the input sequence
                     *  that matches the pattern, starting at the next index from the start index of the previous match
                     */
                    while (matcher.find(matcherStart)) {
                        matcherStart = matcher.start() + 1;
                        conservationMatchList.add(locus + "\t" + i + "\t" + matcherStart + "\t" + matcher.group());
                    }
                }
            }
            arrConservationResults[i] = conservationMatchList.toArray(new String[0]);
        }
        //Initialize arrays for arrUniquenessNumber, allUniquenessMatchList, arrConservationNumber, and allConservationMatchList
        double[] arrUniquenessNumber = new double[inputSequenceLength];
        LinkedHashMap<Integer, Double> arrUniquenessNumberMap = new LinkedHashMap<Integer, Double>();
        String[] allUniquenessMatchList = new String[inputSequenceLength];
        double[] arrConservationNumber = new double[inputSequenceLength];
        LinkedHashMap<Integer, Double> arrConservationNumberMap = new LinkedHashMap<Integer, Double>();
        String[] allConservationMatchList = new String[inputSequenceLength];
        //Prepare blank peptide
        String blankPeptide = new String();
        for (int i = 0; i < spanSetting; i++) {
            blankPeptide += "-";
        }
        //Prepare entryName (gene_species)
        String species = (!entryName.isEmpty()) ? entryName.split("_")[1] : "input";
        String gene = (!entryName.isEmpty()) ? entryName.split("_")[0] : "input";
        //Print details per each peptide
        stringBuilder.append("Range\t" + "Peptide\t" + "Ig-score\t" + "Uniqueness match number\t" + "Uniqueness match list\t" + "Conservation match number\t" + "Conservation match list\t").append(regionHeader).append(siteHeader).append("\n");
        for (int i = inputSequenceLength - 1; i >= 0; i--) {
            if (!sortedPeptide[i].isEmpty()) {
                ArrayList<String> uniquenessArrayList = new ArrayList<String>(); //Example: uniquenessArrayList
                ArrayList<String> conservationArrayList = new ArrayList<String>(); //Example: conservationArrayList
                for (int j = 0; j < matchPerPeptide; j++) { //For each matchSpan
                    String[] uniquenessResults = arrUniquenessResults[sortedPeptideStartRange[i] + j];
                    uniquenessArrayList.addAll(Arrays.asList(uniquenessResults));
                    String[] conservationResults = arrConservationResults[sortedPeptideStartRange[i] + j];
                    conservationArrayList.addAll(Arrays.asList(conservationResults));
                }
                LinkedHashSet<String> uniquenessMatchSet = new LinkedHashSet<String>(); //Example: uniquenessMatchSet
                TreeMap<String, Integer> uniquenessMatchMap = new TreeMap<String, Integer>();
                for (int j = 0; j < uniquenessArrayList.size(); j++) {
                    String[] strings = uniquenessArrayList.get(j).split("\t");
                    /**
                     * strings[0] = locus
                     * strings[1] = arrMatch index
                     * strings[2] = matched peptide start position
                     * strings[3] = matched peptide sequence
                     */
                    int relativeStartPosition = Integer.parseInt(strings[1]) - sortedPeptideStartRange[i];
                    /**
                     * Merge the results with the same locus, sequence, and relativeStartPosition
                     * Ignore matched peptide start position to avoid redundant count
                     */
                    uniquenessMatchSet.add(strings[0] + "_" + strings[3] + "_" + relativeStartPosition);
                    /**
                     * Tag the results with the same locus matched within the same peptide range with
                     * the same number (its start position - its relative distance from peptideStartRange)
                     */
                    String string = strings[0] + "_" + (Integer.parseInt(strings[2]) - relativeStartPosition);
                    uniquenessArrayList.set(j, uniquenessArrayList.get(j) + "\t" + string);
                    Integer freq = uniquenessMatchMap.get(string);
                    uniquenessMatchMap.put(string, (freq == null) ? 1 : freq + 1);
                }
                arrUniquenessNumber[i] = uniquenessMatchSet.size();
                //Create TreeMap<gene, number of non-redundant matched peptides> from uniquenessMatchSet
                TreeMap<String, Integer> uniquenessMatchNRMap = new TreeMap<String, Integer>(); //Example: uniquenessMatchNRMap
                for (String string : uniquenessMatchSet) {
                    String[] strings = string.split("_");
                    Integer freq = uniquenessMatchNRMap.get(strings[0]);
                    uniquenessMatchNRMap.put(strings[0], (freq == null) ? 1 : freq + 1);
                }
                LinkedHashSet<String> conservationMatchSet = new LinkedHashSet<String>(); //Example: conservationMatchSet
                TreeMap<String, Integer> conservationMatchMap = new TreeMap<String, Integer>();
                for (int j = 0; j < conservationArrayList.size(); j++) {
                    String[] strings = conservationArrayList.get(j).split("\t");
                    /**
                     * strings[0] = locus
                     * strings[1] = arrMatch index
                     * strings[2] = matched peptide start position
                     * strings[3] = matched peptide sequence
                     */
                    int relativeStartPosition = Integer.parseInt(strings[1]) - sortedPeptideStartRange[i];
                    /**
                     * Merge the results with the same locus, sequence, and relativeStartPosition
                     * Ignore matched peptide start position to avoid redundant count
                     */
                    conservationMatchSet.add(strings[0] + "_" + strings[3] + "_" + relativeStartPosition);
                    /**
                     * Tag the results with the same locus matched within the same peptide range with
                     * the same number (its start position - its relative distance from peptideStartRange)
                     */
                    String string = strings[0] + "_" + (Integer.parseInt(strings[2]) - relativeStartPosition);
                    conservationArrayList.set(j, conservationArrayList.get(j) + "\t" + string);
                    Integer freq = conservationMatchMap.get(string);
                    conservationMatchMap.put(string, (freq == null) ? 1 : freq + 1);
                }
                arrConservationNumber[i] = conservationMatchSet.size();
                //Create TreeMap<species, number of non-redundant matched peptides> from conservationMatchSet
                TreeMap<String, Integer> conservationMatchNRMap = new TreeMap<String, Integer>(); //Example: conservationMatchNRMap
                for (String string : conservationMatchSet) {
                    String[] strings = string.split("_");
                    Integer freq = conservationMatchNRMap.get(strings[1]);
                    conservationMatchNRMap.put(strings[1], (freq == null) ? 1 : freq + 1);
                }
                //Sort Map.Entry<String, Integer> based on Integer freq (Sort Descending: largest to smallest)
                ArrayList<Map.Entry<String, Integer>> uniquenessMatchMapList = new ArrayList<Map.Entry<String, Integer>>(uniquenessMatchMap.entrySet()); //Example: uniquenessMatchMapList
                ArrayList<Map.Entry<String, Integer>> conservationMatchMapList = new ArrayList<Map.Entry<String, Integer>>(conservationMatchMap.entrySet()); //Example: conservationMatchMapList
                ArrayList<Map.Entry<String, Integer>> uniquenessMatchNRMapList = new ArrayList<Map.Entry<String, Integer>>(uniquenessMatchNRMap.entrySet());
                ArrayList<Map.Entry<String, Integer>> conservationMatchNRMapList = new ArrayList<Map.Entry<String, Integer>>(conservationMatchNRMap.entrySet());
                Comparator c2 = new Comparator<Map.Entry<String, Integer>>() {

                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                };
                Collections.sort(uniquenessMatchMapList, c2);
                Collections.sort(conservationMatchMapList, c2);
                Collections.sort(uniquenessMatchNRMapList, c2);
                Collections.sort(conservationMatchNRMapList, c2);
                //Create uniquenessMatchList
                StringBuilder uniquenessMatchList = new StringBuilder();
                for (Map.Entry<String, Integer> entry : uniquenessMatchNRMapList) {
                    int percentMatch = entry.getValue() * 100 / matchPerPeptide;
                    uniquenessMatchList.append(entry.getKey()).append(" [").append(percentMatch).append("%]; ");
                }
                //Create uniquenessMatchListToolTip
                StringBuilder uniquenessMatchListToolTip = new StringBuilder();
                uniquenessMatchListToolTip.append(sortedPeptide[i]).append(" ").append(sortedPeptideStartRange[i]).append("-").append(sortedPeptideEndRange[i]).append(" ").append(gene).append("<br>");
                for (int j = 0; j < uniquenessMatchMapList.size(); j++) {
                    String string = uniquenessMatchMapList.get(j).getKey();
                    StringBuilder mergedPeptide = new StringBuilder(blankPeptide);
                    TreeSet<Integer> treeSet = new TreeSet<Integer>();
                    for (int k = 0; k < matchPerPeptide; k++) {
                        for (int l = 0; l < uniquenessArrayList.size(); l++) {
                            String[] strings = uniquenessArrayList.get(l).split("\t");
                            if (strings[4].equals(string) && strings[1].equals(Integer.toString(sortedPeptideStartRange[i] + k))) {
                                mergedPeptide.replace(k, k + matchSpan, strings[3]);
                                treeSet.add(Integer.parseInt(strings[2]));
                                break;
                            }
                        }
                    }
                    if (j <= 10) {
                        uniquenessMatchListToolTip.append(mergedPeptide).append(" ").append(treeSet.first()).append("-").append(treeSet.last() + matchSpan - 1).append(" ").append(string.split("_")[0]).append("<br>");
                    } else {
                        uniquenessMatchListToolTip.append("...").append(uniquenessMatchMapList.size() - 10).append(" more matches");
                        break;
                    }
                }
                //Create conservationMatchList
                StringBuilder conservationMatchList = new StringBuilder();
                for (Map.Entry<String, Integer> entry : conservationMatchNRMapList) {
                    int percentMatch = entry.getValue() * 100 / matchPerPeptide;
                    conservationMatchList.append(entry.getKey()).append(" [").append(percentMatch).append("%]; ");
                }
                //Create conservationMatchListToolTip
                StringBuilder conservationMatchListToolTip = new StringBuilder();
                conservationMatchListToolTip.append(sortedPeptide[i]).append(" ").append(sortedPeptideStartRange[i]).append("-").append(sortedPeptideEndRange[i]).append(" ").append(species).append("<br>");
                for (int j = 0; j < conservationMatchMapList.size(); j++) {
                    String string = conservationMatchMapList.get(j).getKey();
                    StringBuilder mergedPeptide = new StringBuilder(blankPeptide);
                    TreeSet<Integer> treeSet = new TreeSet<Integer>();
                    for (int k = 0; k < matchPerPeptide; k++) {
                        for (int l = 0; l < conservationArrayList.size(); l++) {
                            String[] strings = conservationArrayList.get(l).split("\t");
                            if (strings[4].equals(string) && strings[1].equals(Integer.toString(sortedPeptideStartRange[i] + k))) {
                                mergedPeptide.replace(k, k + matchSpan, strings[3]);
                                treeSet.add(Integer.parseInt(strings[2]));
                                break;
                            }
                        }
                    }
                    if (j <= 10) {
                        conservationMatchListToolTip.append(mergedPeptide).append(" ").append(treeSet.first()).append("-").append(treeSet.last() + matchSpan - 1).append(" ").append(string.split("_")[1]).append("<br>");
                    } else {
                        conservationMatchListToolTip.append("...").append(conservationMatchMapList.size() - 10).append(" more matches");
                        break;
                    }
                }
                //Start actual reporting
                stringBuilder.append(sortedPeptideStartRange[i]).append("..").append(sortedPeptideEndRange[i]).append("\t").append(sortedPeptide[i]).append("\t").append(arrIgScoreMapList.get(i).getValue()).append("\t").append(arrUniquenessNumber[i]).append("\t").append(uniquenessMatchList).append("\t").append(arrConservationNumber[i]).append("\t").append(conservationMatchList).append("\t");
                arrUniquenessNumberMap.put(sortedIndexArray[i], arrUniquenessNumber[i]);
                allUniquenessMatchList[i] = uniquenessMatchListToolTip.toString();
                arrConservationNumberMap.put(sortedIndexArray[i], arrConservationNumber[i]);
                allConservationMatchList[i] = conservationMatchListToolTip.toString();
                for (int j = 0; j < (arrayRegionSite.length / inputSequenceLength); j++) { //For each region/site
                    StringBuilder allRegionSite = new StringBuilder();
                    for (int k = sortedPeptideStartRange[i] - 1; k < sortedPeptideEndRange[i]; k++) { //Get allRegionSite of each peptide
                        allRegionSite.append(arrayRegionSite[k + (j * inputSequenceLength)]).append("\t");
                    }
                    //Eliminate redundancy and Put overlapping notes together separated by ,
                    LinkedHashSet<String> setRegionSite = new LinkedHashSet<String>();
                    String[] arrayAllRegionSite = allRegionSite.toString().split("\t");
                    for (String eachRegionSite : arrayAllRegionSite) {
                        if (!eachRegionSite.equals(" ")) {
                            setRegionSite.add(eachRegionSite);
                        }
                    }
                    if (setRegionSite.isEmpty()) {
                        stringBuilder.append(" \t");
                    } else {
                        StringBuilder cleanRegionSite = new StringBuilder();
                        for (Object eachRegionSite : setRegionSite) {
                            cleanRegionSite.append(eachRegionSite).append(", ");
                        }
                        stringBuilder.append(cleanRegionSite.substring(0, cleanRegionSite.length() - 2).replace("<br>", " ")).append("\t");
                    }
                }
                stringBuilder.append("\n");
            }
        }
        //Sort arrUniquenessNumberMapList based on UniquenessNumber (Sort Ascending: smallest to largest)
        ArrayList<Map.Entry<Integer, Double>> arrUniquenessNumberMapList = new ArrayList<Map.Entry<Integer, Double>>(arrUniquenessNumberMap.entrySet());
        Collections.sort(arrUniquenessNumberMapList, c1);
        String[][] arr2DUniqueness = new String[numberOfSpan][4];
        for (int i = 0; i < numberOfSpan; i++) {
            int sortedIndex = arrUniquenessNumberMapList.get(i).getKey();
            //Uniqueness Rank
            if (i != 0) {
                arr2DUniqueness[i][0] =
                        (arrUniquenessNumberMapList.get(i).getValue().equals(arrUniquenessNumberMapList.get(i - 1).getValue())
                        && arrIgScore[sortedIndex] == arrIgScore[arrUniquenessNumberMapList.get(i - 1).getKey()])
                        ? arr2DUniqueness[i - 1][0]
                        : Integer.toString(i + 1);
            } else {
                arr2DUniqueness[i][0] = Integer.toString(1);
            }
            arr2DUniqueness[i][1] = peptide[sortedIndex];
            arr2DUniqueness[i][2] = Integer.toString(peptideStartRange[sortedIndex]);
            arr2DUniqueness[i][3] = Integer.toString(peptideEndRange[sortedIndex]);
//            System.out.println("Uniqueness Rank = " + arr2DUniqueness[i][0]
//                    + " peptide = " + arr2DUniqueness[i][1]
//                    + " " + arr2DUniqueness[i][2]
//                    + " " + arr2DUniqueness[i][3]);
        }
        double maxUniquenessNumber = UNIQUENESS_NUMBER * matchPerPeptide; //Real max = arrUniquenessNumberMapList.get(numberOfSpan - 1).getValue();
        double minUniquenessNumber = 0;
//        System.out.println("maxUniquenessNumber: " + maxUniquenessNumber + " minUniquenessNumber: " + minUniquenessNumber);
        //Sort arrConservationNumberMapList based on ConservationNumber (Sort Descending: largest to smallest)
        ArrayList<Map.Entry<Integer, Double>> arrConservationNumberMapList = new ArrayList<Map.Entry<Integer, Double>>(arrConservationNumberMap.entrySet());
        Comparator c3 = new Comparator<Map.Entry<Integer, Double>>() {

            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        };
        Collections.sort(arrConservationNumberMapList, c3);
        String[][] arr2DConservation = new String[numberOfSpan][4];
        for (int i = 0; i < numberOfSpan; i++) {
            int sortedIndex = arrConservationNumberMapList.get(i).getKey();
            //Conservation Rank
            if (i != 0) {
                arr2DConservation[i][0] =
                        (arrConservationNumberMapList.get(i).getValue().equals(arrConservationNumberMapList.get(i - 1).getValue())
                        && arrIgScore[sortedIndex] == arrIgScore[arrConservationNumberMapList.get(i - 1).getKey()])
                        ? arr2DConservation[i - 1][0]
                        : Integer.toString(i + 1);
            } else {
                arr2DConservation[i][0] = Integer.toString(1);
            }
            arr2DConservation[i][1] = peptide[sortedIndex];
            arr2DConservation[i][2] = Integer.toString(peptideStartRange[sortedIndex]);
            arr2DConservation[i][3] = Integer.toString(peptideEndRange[sortedIndex]);
//            System.out.println("Conservation Rank = " + arr2DConservation[i][0]
//                    + " peptide = " + arr2DConservation[i][1]
//                    + " " + arr2DConservation[i][2]
//                    + " " + arr2DConservation[i][3]);
        }
        double maxConservationNumber = (species.equals("HUMAN") || species.equals("RAT") || species.equals("MOUSE"))
                ? (SPECIES_NUMBER - 1) * matchPerPeptide : SPECIES_NUMBER * matchPerPeptide;
        double minConservationNumber = 0;
//        System.out.println("maxConservationNumber: " + maxConservationNumber + " minConservationNumber: " + minConservationNumber);
        //Sort arrays back in ascending index order (based on sortedIndex)
        double[] sortedArrUniquenessNumber = new double[inputSequenceLength];
        String[] sortedAllUniquenessMatchList = new String[inputSequenceLength];
        double[] sortedArrConservationNumber = new double[inputSequenceLength];
        String[] sortedAllConservationMatchList = new String[inputSequenceLength];
        for (int i = 0; i < inputSequenceLength; i++) {
            int sortedIndex = sortedIndexArray[i];
            sortedArrUniquenessNumber[sortedIndex] = arrUniquenessNumber[i];
            sortedAllUniquenessMatchList[sortedIndex] = allUniquenessMatchList[i];
            sortedArrConservationNumber[sortedIndex] = arrConservationNumber[i];
            sortedAllConservationMatchList[sortedIndex] = allConservationMatchList[i];
        }
        //Transform values to 8-bit (0-255) scale
        double[] arrUniquenessHeatMap = new double[inputSequenceLength];
        double[] arrConservationHeatMap = new double[inputSequenceLength];
        for (int i = leftWingSpan; i < inputSequenceLength - rightWingSpan; i++) {
            arrUniquenessHeatMap[i] = (sortedArrUniquenessNumber[i] > maxUniquenessNumber)
                    ? 0 : 255 - ((sortedArrUniquenessNumber[i] * 255) / maxUniquenessNumber);
            arrConservationHeatMap[i] = (sortedArrConservationNumber[i] * 255) / maxConservationNumber;
        }
        for (int i = 0; i < leftWingSpan; i++) {
            arrUniquenessHeatMap[i] = arrUniquenessHeatMap[leftWingSpan];
            arrConservationHeatMap[i] = arrConservationHeatMap[leftWingSpan];
        }
        for (int i = inputSequenceLength - rightWingSpan; i < inputSequenceLength; i++) {
            arrUniquenessHeatMap[i] = arrUniquenessHeatMap[inputSequenceLength - rightWingSpan - 1];
            arrConservationHeatMap[i] = arrConservationHeatMap[inputSequenceLength - rightWingSpan - 1];
        }
        //Prepare final components of HeatMapArguments
        boolean[] arrayAllRegionSelectedState = null;
        boolean[] arrayAllSiteSelectedState = null;
        if (arrayAllRegion.length != 0) {
            arrayAllRegionSelectedState = new boolean[arrayAllRegion.length];
            Arrays.fill(arrayAllRegionSelectedState, true); //Show all regions by default
        }
        if (arrayAllSite.length != 0) {
            arrayAllSiteSelectedState = new boolean[arrayAllSite.length];
            Arrays.fill(arrayAllSiteSelectedState, true); //Show all sites by default
        }
        String outputText = stringBuilder.toString();
        //HeatMapArguments
        HeatMapArguments heatMapArguments =
                new HeatMapArguments(
                inputType,
                entryName,
                inputSequence,
                inputSequenceLength,
                spanSetting,
                segmaskerChar,
                arrIgScoreHeatMap,
                arrIgScore,
                igScoreRank,
                arrUniquenessHeatMap,
                sortedAllUniquenessMatchList,
                arrConservationHeatMap,
                sortedAllConservationMatchList,
                peptide,
                arrayAllRegion,
                arrayAllSite,
                arrayRegionSite,
                arrayAllRegionSelectedState,
                arrayAllSiteSelectedState,
                hasTM,
                arrayTailLoop,
                arrayKDHeatMap,
                arrayKD,
                structureArray,
                outputText,
                arr2DIgScore,
                arr2DUniqueness,
                arr2DConservation);
        return heatMapArguments;
    }
}
