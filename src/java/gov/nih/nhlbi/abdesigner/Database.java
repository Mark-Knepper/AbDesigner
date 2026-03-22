/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nih.nhlbi.abdesigner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author Pisitkun
 */
class Database {

    //Get hashMap
    public static HashMap<String, String> getHashMap(String file) throws IOException, ClassNotFoundException {
        HashMap<String, String> hashMap = null;
        ObjectInputStream inputStream = null;
        try {
            inputStream =
                    new ObjectInputStream(
                    new BufferedInputStream(
                    new FileInputStream(file)));
            hashMap = (HashMap<String, String>) inputStream.readObject();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return hashMap;
    }

    //Get linkedHashMap
    public static LinkedHashMap<String, Integer[]> getLinkedHashMap(String file) throws IOException, ClassNotFoundException {
        LinkedHashMap<String, Integer[]> linkedHashMap = null;
        ObjectInputStream inputStream = null;
        try {
            inputStream =
                    new ObjectInputStream(
                    new BufferedInputStream(
                    new FileInputStream(file)));
            linkedHashMap = (LinkedHashMap<String, Integer[]>) inputStream.readObject();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return linkedHashMap;
    }

    //Get locusSequence2DArrayList
    public static ArrayList<ArrayList<String>> get2DArrayList(String file) throws IOException, ClassNotFoundException {
        ArrayList<ArrayList<String>> locusSequence2DArrayList = null;
        ObjectInputStream inputStream = null;
        try {
            inputStream =
                    new ObjectInputStream(
                    new BufferedInputStream(
                    new FileInputStream(file)));
            locusSequence2DArrayList = (ArrayList<ArrayList<String>>) inputStream.readObject();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return locusSequence2DArrayList;
    }
}
