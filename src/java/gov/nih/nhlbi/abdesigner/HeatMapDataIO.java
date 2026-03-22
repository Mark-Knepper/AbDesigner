/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nih.nhlbi.abdesigner;

import java.io.*;

/**
 *
 * @author Pisitkun
 */
class HeatMapDataIO {

    static void add(HeatMapArguments heatMapArguments, String pathname) throws IOException {
        ObjectOutputStream outputStream = null;
        try {
            outputStream =
                    new ObjectOutputStream(
                    new BufferedOutputStream(
                    new FileOutputStream(pathname, false)));
            //Write data to the outputStream
            outputStream.writeObject(heatMapArguments.getInputType());
            outputStream.writeObject(heatMapArguments.getEntryName());
            outputStream.writeObject(heatMapArguments.getInputSequence());
            outputStream.writeInt(heatMapArguments.getInputSequenceLength());
            outputStream.writeInt(heatMapArguments.getSpanSetting());
            outputStream.writeObject(heatMapArguments.getSegmaskerChar());
            outputStream.writeObject(heatMapArguments.getArrIgScoreHeatMap());
            outputStream.writeObject(heatMapArguments.getArrIgScore());
            outputStream.writeObject(heatMapArguments.getIgScoreRank());
            outputStream.writeObject(heatMapArguments.getArrUniquenessHeatMap());
            outputStream.writeObject(heatMapArguments.getSortedAllUniquenessMatchList());
            outputStream.writeObject(heatMapArguments.getArrConservationHeatMap());
            outputStream.writeObject(heatMapArguments.getSortedAllConservationMatchList());
            outputStream.writeObject(heatMapArguments.getPeptide());
            outputStream.writeObject(heatMapArguments.getArrayAllRegion());
            outputStream.writeObject(heatMapArguments.getArrayAllSite());
            outputStream.writeObject(heatMapArguments.getArrayRegionSite());
            outputStream.writeObject(heatMapArguments.getArrayAllRegionSelectedState());
            outputStream.writeObject(heatMapArguments.getArrayAllSiteSelectedState());
            outputStream.writeBoolean(heatMapArguments.isHasTM());
            outputStream.writeInt(heatMapArguments.getArrayTailLoop());
            outputStream.writeObject(heatMapArguments.getArrayKDHeatMap());
            outputStream.writeObject(heatMapArguments.getArrayKD());
            outputStream.writeObject(heatMapArguments.getStructureArray());
            outputStream.writeObject(heatMapArguments.getOutputText());
            outputStream.writeObject(heatMapArguments.getArr2DIgScore());
            outputStream.writeObject(heatMapArguments.getArr2DUniqueness());
            outputStream.writeObject(heatMapArguments.getArr2DConservation());
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    static HeatMapArguments getHeatMapArguments(String pathname) throws IOException, ClassNotFoundException {
        HeatMapArguments heatMapArguments = null;
        ObjectInputStream inputStream = null;
        try {
            inputStream =
                    new ObjectInputStream(
                    new BufferedInputStream(
                    new FileInputStream(pathname)));
            //HeatMapArguments
            heatMapArguments =
                    new HeatMapArguments(
                    (String) inputStream.readObject(), //inputType
                    (String) inputStream.readObject(), //entryName
                    (String) inputStream.readObject(), //inputSequence
                    (int) inputStream.readInt(), //inputSequenceLength
                    (int) inputStream.readInt(), //spanSetting
                    (char[]) inputStream.readObject(), //segmaskerChar
                    (double[]) inputStream.readObject(), //arrIgScoreHeatMap
                    (double[]) inputStream.readObject(), //arrIgScore
                    (String[]) inputStream.readObject(), //igScoreRank
                    (double[]) inputStream.readObject(), //arrUniquenessHeatMap
                    (String[]) inputStream.readObject(), //sortedAllUniquenessMatchList
                    (double[]) inputStream.readObject(), //arrConservationHeatMap
                    (String[]) inputStream.readObject(), //sortedAllConservationMatchList
                    (String[]) inputStream.readObject(), //peptide
                    (Object[]) inputStream.readObject(), //arrayAllRegion
                    (Object[]) inputStream.readObject(), //arrayAllSite
                    (String[]) inputStream.readObject(), //arrayRegionSite
                    (boolean[]) inputStream.readObject(), //arrayAllRegionSelectedState
                    (boolean[]) inputStream.readObject(), //arrayAllSiteSelectedState
                    (boolean) inputStream.readBoolean(), //hasTM
                    (int) inputStream.readInt(), //arrayTailLoop
                    (double[]) inputStream.readObject(), //arrayKDHeatMap
                    (double[]) inputStream.readObject(), //arrayKD
                    (String[]) inputStream.readObject(), //structureArray
                    (String) inputStream.readObject(), //outputText
                    (String[][]) inputStream.readObject(), //arr2DIgScore
                    (String[][]) inputStream.readObject(), //arr2DUniqueness
                    (String[][]) inputStream.readObject()); //arr2DConservation
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return heatMapArguments;
    }
}
