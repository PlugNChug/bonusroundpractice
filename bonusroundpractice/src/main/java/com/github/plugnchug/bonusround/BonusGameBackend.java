package com.github.plugnchug.bonusround;

import java.io.*;
import java.util.*;

import javafx.util.Pair;

public class BonusGameBackend {
    // Variables
    public static List<Character> rstlne = new ArrayList<>(Arrays.asList('R', 'S', 'T', 'L', 'N', 'E'));

    // private static final int FIRST_ROW = 0;
    private static final int SECOND_ROW = 12;
    private static final int THIRD_ROW = 26;
    // private static final int FOURTH_ROW = 40;
    // private static final int MIDDLE_FIRST_FOURTH = 6;
    private static final int MIDDLE_SECOND_THIRD = 7;
    private static List<String> answers = new ArrayList<>();

    private Animators animation;

    public BonusGameBackend(Animators anim) {
        this.animation = anim;
    }

    /**
     * Checks the answers.csv file generated using the "Get Bonus Puzzles from the Web" button, shuffles the rows, and picks the first row
     * @return A random answer and category as a pair
     * @throws IOException
     */
    public Pair<String, String> getRandomAnswer() throws IOException {
        File file = new File("answers.csv");
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String s;
        while ((s = reader.readLine()) != null) {
            answers.add(s);
        }

        reader.close();

        Collections.shuffle(answers);
        Random random = new Random();
        int index = random.nextInt(0, answers.size());
        return new Pair<String,String>(answers.get(index).split(",")[0], answers.get(index).split(",")[1]);
    }

    public Pair<String, String> snipeWordCount(int wordCount) throws IOException {

        File file = new File("answers.csv");
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String s;
        while ((s = reader.readLine()) != null) {
            if (!s.isEmpty() && Arrays.asList(s.split(",")[0].split(" ")).size() == wordCount) {
                answers.add(s);
            }
        }

        reader.close();

        Collections.shuffle(answers);
        return new Pair<String,String>(answers.get(0).split(",")[0], answers.get(0).split(",")[1]);
    }

    /**
     * Places the given words in a reasonable manner on the puzzle board.
     * <p>The bonus round never uses the topmost and bottommost rows.
     * This also applies to toss-ups and round 4+'s.
     * 
     * @param words the list of words in the answer
     * @param answerLen the length of the answer, including spaces and special characters
     */
    public void calculateWordPosition(List<String> words, int answerLen) {
        // startPos represents the calculated leftmost white tile (which should be in the same column for both rows)
        int startPos;
        // wordLengths stores each word in the answer's length for use in calculations
        List<Integer> wordLengths = new ArrayList<>();

        // Random function used in some two word answers to add variety to some layouts
        Random random = new Random();

        // Perform the row splits based how many words there are in the puzzle
        switch (words.size()) {
            // Very simple case: one word means only one row, guaranteed!
            case 1:
            startPos = MIDDLE_SECOND_THIRD - (answerLen / 2);
                animation.animateReveal(words.get(0), SECOND_ROW, startPos);
                break;
            // Two word answers go through a much more complicated process since they have many more cases
            case 2:
                wordLengths.add(words.get(0).length());
                wordLengths.add(words.get(1).length());

                // Certain two word answers (specifically some shorter answers and answers with a one- or two-letter word) will only need one row
                if (((wordLengths.get(0) < 4 || wordLengths.get(1) < 4) && answerLen < 10) || wordLengths.get(0) <= 2 && answerLen <= 14) { 
                    startPos = (MIDDLE_SECOND_THIRD - (answerLen / 2));

                    animation.animateReveal(words.get(0) + " " + words.get(1), SECOND_ROW, startPos);
                } 
                // On the other hand, longer answers must have two rows
                else if (answerLen >= 10) {
                    // This block will help with aligning to the larger word
                    if (wordLengths.get(0) > wordLengths.get(1)) {
                        startPos = (MIDDLE_SECOND_THIRD - (wordLengths.get(0) / 2));
                    } else {
                        startPos = (MIDDLE_SECOND_THIRD - (wordLengths.get(1) / 2));
                    }

                    animation.animateReveal(words.get(0), SECOND_ROW, startPos);
                    animation.animateReveal(words.get(1), THIRD_ROW, startPos);
                } 
                // Answers that don't fall into the above conditions will have a random chance of being either one or two row answers
                else {
                    if (random.nextBoolean()) {
                        startPos = (MIDDLE_SECOND_THIRD - (answerLen / 2));

                        animation.animateReveal(words.get(0) + " " + words.get(1), SECOND_ROW, startPos);
                    } else {
                        // This block will help with aligning to the larger word
                        if (wordLengths.get(0) > wordLengths.get(1)) {
                            startPos = (MIDDLE_SECOND_THIRD - (wordLengths.get(0) / 2));
                        } else {
                            startPos = (MIDDLE_SECOND_THIRD - (wordLengths.get(1) / 2));
                        }

                        animation.animateReveal(words.get(0), SECOND_ROW, startPos);
                        animation.animateReveal(words.get(1), THIRD_ROW, startPos);
                    }
                }
                break;
            // Three word answers go through a similar process to the two word cases
            case 3:
                wordLengths.add(words.get(0).length());
                wordLengths.add(words.get(1).length());
                wordLengths.add(words.get(2).length());
                
                // One row three-word answers are pretty rare...
                if (answerLen < 10) {
                    animation.animateReveal(words.get(0) + " " + words.get(1) + " " + words.get(2), SECOND_ROW, MIDDLE_SECOND_THIRD - (answerLen / 2));
                }
                // ...which means that a two line answer with three words is much more common.
                // This block tracks the combinations of the first and second words and the second and third words.
                // The lengths of each combination are taken into account when determining the board layout.
                else {
                    int comboLength1 = wordLengths.get(0) + wordLengths.get(1) + 1;
                    int comboLength2 = wordLengths.get(1) + wordLengths.get(2) + 1;

                    if (comboLength1 > comboLength2) {
                        startPos = (MIDDLE_SECOND_THIRD - (comboLength2 / 2)); 

                        animation.animateReveal(words.get(0), SECOND_ROW, startPos);
                        animation.animateReveal(words.get(1) + " " + words.get(2), THIRD_ROW, startPos);
                        
                    } 
                    else if (wordLengths.get(0) <= 2 || wordLengths.get(1) <= 2) {
                        if (wordLengths.get(0) + wordLengths.get(1) + 1 < wordLengths.get(2)) {
                            startPos = MIDDLE_SECOND_THIRD - (wordLengths.get(2) / 2); 
                        } else {
                            startPos = MIDDLE_SECOND_THIRD - (comboLength1 / 2);
                        }

                        animation.animateReveal(words.get(0) + " " + words.get(1), SECOND_ROW, startPos);
                        animation.animateReveal(words.get(2), THIRD_ROW, startPos);
                    }
                    else {
                        startPos = (MIDDLE_SECOND_THIRD - (comboLength1 / 2));

                        animation.animateReveal(words.get(0) + " " + words.get(1), SECOND_ROW, startPos);
                        animation.animateReveal(words.get(2), THIRD_ROW, startPos);
                    }
                }
                break;
            // Four or more word answers will have a more general formula
            default:
                for (int i = 0; i < words.size(); i++) {
                    wordLengths.add(words.get(i).length());
                }

                String topString = "";
                String bottomString = "";
                int topLength = 0;
                int bottomLength = 0;
                int splitIndex = 0;
                
                // Prevent lopsided-looking answer by shifting the split forward one word
                if (answerLen <= 28) {
                    int modifiedSplit = 0;
                    int minLetterCountDiff = 14;

                    // Loop through combinations of row lengths given the answer words, and find the best combination of words to split between two rows
                    for (int i = 0; i < words.size(); i++) {
                        int rowLen1 = 0;
                        int rowLen2 = words.get(i).length();
                        for (int k = 0; k < i; k++) {
                            rowLen1 += words.get(k).length();
                        }
                        for (int j = i + 1; j < words.size(); j++) {
                            rowLen2 += words.get(j).length();
                        }

                        // Update the min letter count difference if there's a new lowest difference between row lengths
                        if (Math.abs(rowLen1 - rowLen2) < minLetterCountDiff) {
                            splitIndex = modifiedSplit;
                            minLetterCountDiff = Math.abs(rowLen1 - rowLen2);
                        }
                        modifiedSplit++;
                    }
                } 
                // Make some edge cases look better on the board
                else if (topLength - bottomLength >= 5 && splitIndex > 2) {
                    splitIndex--;
                }
                    
                // Place the words on the board, affected by the split index
                for (int i = 0; i < words.size(); i++) {
                    if (i < splitIndex) {
                        topString += words.get(i) + " ";
                    } else {
                        bottomString += words.get(i) + " ";
                    }
                }

                // Left-align the rows
                if (topString.length() > bottomString.length()) {
                    startPos = MIDDLE_SECOND_THIRD - (topString.length() / 2); 
                } else {
                    startPos = MIDDLE_SECOND_THIRD - (bottomString.length() / 2);
                }

                animation.animateReveal(topString, SECOND_ROW, startPos);
                animation.animateReveal(bottomString, THIRD_ROW, startPos);
                break;
        }
    }
}
