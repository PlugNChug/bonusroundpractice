package com.github.plugnchug.bonusround;

import java.io.*;
import java.util.*;

import javafx.util.Pair;

public class BonusGameBackend {
    // Local variables
    private static List<String> answers = new ArrayList<>();

    public static char[] rstlne = {'r', 's', 't', 'l', 'n', 'e'};
    
    public static Pair<String, String> getRandomAnswer() throws IOException {
        File file = new File("answers.csv");
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String s;
        while ((s = reader.readLine()) != null) {
            answers.add(s);
        }

        reader.close();

        Collections.shuffle(answers);
        return new Pair<String,String>(answers.get(0).split(",")[0], answers.get(0).split(",")[1]);
    }
}
