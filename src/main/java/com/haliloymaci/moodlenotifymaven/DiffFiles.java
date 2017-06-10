/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.haliloymaci.moodlenotifymaven;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author halil
 */
public class DiffFiles {

    public static List<String> diffTwoFileThatIsMultiline(String file1, String file2) {
        List<String> original = fileToLines(file1);
        List<String> revised = fileToLines(file2);

        // Compute diff. Get the Patch object. Patch is the container for computed deltas.
        Patch patch = DiffUtils.diff(original, revised);

        List<String> list = new ArrayList<>();
        for (Delta delta : patch.getDeltas()) {
            list.add(delta.toString());
        }
        return list;
    }

    private static List<String> fileToLines(String filename) {
        List<String> lines = new LinkedList<String>();
        String line = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static String readFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            return everything;
        } catch (IOException ex) {
            Logger.getLogger(LoginWebSite.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String readFile(String fileName, int specificLineNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            int counter = 1;
            while (line != null) {
                sb.append(line);
                if (counter == specificLineNumber) {
                    return line;
                }
                ++counter;
                line = br.readLine();
            }
            String everything = sb.toString();
            return everything;
        } catch (IOException ex) {
            Logger.getLogger(LoginWebSite.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
