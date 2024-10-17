package com.github.plugnchug.bonusround;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.*;

import com.github.plugnchug.bonusround.scraper.BaVScraper;

import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

public class Game {
    private final int FIRST_ROW = 0;
    private final int SECOND_ROW = 12;
    private final int THIRD_ROW = 26;
    private final int FOURTH_ROW = 40;
    private final int MIDDLE_FIRST_FOURTH = 6;
    private final int MIDDLE_SECOND_THIRD = 7;

    @FXML
    Label categoryDisplay;
    @FXML
    Pane board;
    @FXML
    Button beginPuzzleButton;

    ObservableList<Node> spaces;
    
    List<Integer> whiteSpacePositions = new ArrayList<>();

    // Sound effects
    public static Sounds puzzleRevealSound = new Sounds("resources/puzzleReveal.wav");
    public static Sounds dingSound1 = new Sounds("resources/ding.wav");
    public static Sounds dingSound2 = new Sounds("resources/ding.wav");

    // Music cues
    public static Sounds rstlne = new Sounds("resources/rstlne.wav");
    public static Sounds chooseLetters = new Sounds("resources/chooseLetters.wav");


    

    @FXML
    private void webScrape() throws IOException {
        try {
            BaVScraper.scrapeSeasons("https://buyavowel.boards.net/page/compendiumindex");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    @FXML
    private void beginPuzzle() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        // Prevent button spamming that could glitch out the puzzle board
        beginPuzzleButton.setDisable(true);
        enableButtonTimer();

        Pair<String, String> puzzle = BonusGameBackend.getRandomAnswer();
        // Pair<String, String> puzzle = BonusGameBackend.snipeWordCount(3);
        
        categoryDisplay.setText(puzzle.getValue());

        spaces = board.getChildren();
        // Reset all board spaces to be invisible
        for (var space : spaces) {
            space.setVisible(false);
            ((Label) space).setText("");
        }
        
        // Get answer length
        int answerLen = puzzle.getKey().length();

        // Split the answer into the rows in a reasonable way
        List<String> words = new ArrayList<>(Arrays.asList(puzzle.getKey().split(" ")));

        // Begin music cues
        chooseLetters.stop();
        rstlne.play(1, true);

        calculateWordPosition(words, answerLen);

        System.out.println(puzzle.getKey() + " - " + answerLen);

        animateLetters(BonusGameBackend.rstlne, words);
    }

    /**
     * Places the given words in a reasonable manner on the puzzle board.
     * <p>The bonus round never uses the topmost and bottommost rows.
     * This also applies to toss-ups and round 4+'s.
     * 
     * @param words the list of words in the answer
     * @param answerLen the length of the answer, including spaces and special characters
     */
    private void calculateWordPosition(List<String> words, int answerLen) {
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
                animateReveal(words.get(0), SECOND_ROW, startPos);
                break;
            // Two word answers go through a much more complicated process since they have many more cases
            case 2:
                wordLengths.add(words.get(0).length());
                wordLengths.add(words.get(1).length());

                // Certain two word answers (specifically some shorter answers and answers with a one- or two-letter word) will only need one row
                if (((wordLengths.get(0) < 4 || wordLengths.get(1) < 4) && answerLen < 10) || wordLengths.get(0) <= 2 && answerLen <= 14) { 
                    startPos = (MIDDLE_SECOND_THIRD - (answerLen / 2));

                    animateReveal(words.get(0) + " " + words.get(1), SECOND_ROW, startPos);
                } 
                // On the other hand, longer answers must have two rows
                else if (answerLen >= 10) {
                    // This block will help with aligning to the larger word
                    if (wordLengths.get(0) > wordLengths.get(1)) {
                        startPos = (MIDDLE_SECOND_THIRD - (wordLengths.get(0) / 2));
                    } else {
                        startPos = (MIDDLE_SECOND_THIRD - (wordLengths.get(1) / 2));
                    }

                    animateReveal(words.get(0), SECOND_ROW, startPos);
                    animateReveal(words.get(1), THIRD_ROW, startPos);
                } 
                // Answers that don't fall into the above conditions will have a random chance of being either one or two row answers
                else {
                    if (random.nextBoolean()) {
                        startPos = (MIDDLE_SECOND_THIRD - (answerLen / 2));

                        animateReveal(words.get(0) + " " + words.get(1), SECOND_ROW, startPos);
                    } else {
                        // This block will help with aligning to the larger word
                        if (wordLengths.get(0) > wordLengths.get(1)) {
                            startPos = (MIDDLE_SECOND_THIRD - (wordLengths.get(0) / 2));
                        } else {
                            startPos = (MIDDLE_SECOND_THIRD - (wordLengths.get(1) / 2));
                        }

                        animateReveal(words.get(0), SECOND_ROW, startPos);
                        animateReveal(words.get(1), THIRD_ROW, startPos);
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
                    animateReveal(words.get(0) + " " + words.get(1) + " " + words.get(2), SECOND_ROW, MIDDLE_SECOND_THIRD - (answerLen / 2));
                }
                // ...which means that a two line answer with three words is much more common
                else {
                    int comboLength1 = wordLengths.get(0) + wordLengths.get(1) + 1;
                    int comboLength2 = wordLengths.get(1) + wordLengths.get(2) + 1;

                    if (comboLength1 > comboLength2) {
                        startPos = (MIDDLE_SECOND_THIRD - (comboLength2 / 2)); 

                        animateReveal(words.get(0), SECOND_ROW, startPos);
                        animateReveal(words.get(1) + " " + words.get(2), THIRD_ROW, startPos);
                        
                    } 
                    else if (wordLengths.get(0) <= 2 || wordLengths.get(1) <= 2) {
                        if (wordLengths.get(0) + wordLengths.get(1) + 1 < wordLengths.get(2)) {
                            startPos = MIDDLE_SECOND_THIRD - (wordLengths.get(2) / 2); 
                        } else {
                            startPos = MIDDLE_SECOND_THIRD - (comboLength1 / 2);
                        }

                        animateReveal(words.get(0) + " " + words.get(1), SECOND_ROW, startPos);
                        animateReveal(words.get(2), THIRD_ROW, startPos);
                    }
                    else {
                        startPos = (MIDDLE_SECOND_THIRD - (comboLength1 / 2));

                        animateReveal(words.get(0) + " " + words.get(1), SECOND_ROW, startPos);
                        animateReveal(words.get(2), THIRD_ROW, startPos);
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
                    for (int i = 0; i < words.size(); i++) {
                        int tempLen1 = 0;
                        int tempLen2 = words.get(i).length();
                        for (int k = 0; k < i; k++) {
                            tempLen1 += words.get(k).length();
                            // if (k < i - 1) {
                            //     tempLen1++;
                            // }
                        }
                        for (int j = i + 1; j < words.size(); j++) {
                            tempLen2 += words.get(j).length();
                            // if (j < words.size() - 1) {
                            //     tempLen2++;
                            // }
                        }
                        System.out.println("FirstLen: " + tempLen1);
                        System.out.println("SecondLen: " + tempLen2);
                        if (Math.abs(tempLen1 - tempLen2) < minLetterCountDiff) {
                            splitIndex = modifiedSplit;
                            minLetterCountDiff = Math.abs(tempLen1 - tempLen2);
                        }
                        modifiedSplit++;
                    }
                } else if (topLength - bottomLength >= 5 && splitIndex > 2) {
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

                if (topString.length() > bottomString.length()) {
                    startPos = MIDDLE_SECOND_THIRD - (topString.length() / 2); 
                } else {
                    startPos = MIDDLE_SECOND_THIRD - (bottomString.length() / 2);
                }

                animateReveal(topString, SECOND_ROW, startPos);
                animateReveal(bottomString, THIRD_ROW, startPos);
                break;
        }
    }

    @FXML
    private void stopSounds() {
        rstlne.stop();
        chooseLetters.play(1, true);
    }

    private void animateReveal(String words, int row, int startPos) {
        new AnimationTimer() {
            long startTime = 0;
            int position = 0;

            @Override
            public void handle(long now) {
                try {
                    // Note this only deals with one row: the row given in the parameters.
                    // This is why we call this method twice when an answer needs two rows
                    if (now > startTime + TimeUnit.MILLISECONDS.toNanos(60)) {
                        // Display any special characters automatically...
                        if (!Character.isLetter(words.toCharArray()[position]) && words.toCharArray()[position] != ' ') {
                            spaces.get(row + startPos + position).setVisible(true);
                            ((Label) spaces.get(row + startPos + position)).setText(Character.toString(words.toCharArray()[position]));
                        }
                        //...but don't activate tiles where a space would be!
                        if (words.toCharArray()[position] != ' ') {
                            spaces.get(row + startPos + position).setVisible(true);
                        }
                        startTime = now;
                        position++;
                    }
                    
                    if (position == words.length()) {
                        stop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    stop();
                }
                
            }
            
        }.start();
    }

    private void animateLetters(List<Character> letters, List<String> words) {
    new AnimationTimer() {
        long startTime = 0;
        boolean initialized = false;
        boolean revealStarted = false;

        List<Pair<Character, Integer>> letterAssignments = new ArrayList<>();
        int[] revealOrder = {25, 39, 24, 38, 23, 37, 22, 36, 21, 35, 20, 34, 19, 33, 18, 32, 17, 31, 16, 30, 15, 29, 14, 28, 13, 27, 12, 26};

        @Override
        public void handle(long now) {
            if (startTime == 0) {
                startTime = now;
            }

            try {
                long elapsedTime = now - startTime;

                // 2.25 second delay until list initialization
                if (!initialized && elapsedTime >= TimeUnit.MILLISECONDS.toNanos(2250)) {
                    initializeLetterAssignments(words);
                    initialized = true;
                }

                // 2.5 second delay until letter reveals
                if (initialized && (revealStarted || elapsedTime >= TimeUnit.MILLISECONDS.toNanos(2500))) {
                    revealStarted = true;

                    // Reveal letters every 1.1 seconds
                    if (elapsedTime >= TimeUnit.MILLISECONDS.toNanos(1100)) {
                        startTime = now;
                        boolean letterRevealed = revealNextLetter(letters);

                        // Stop if no letters were revealed
                        if (!letterRevealed) {
                            System.out.println("Done revealing letters!");
                            stop();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                stop();
            }
        }

        // Create list of letter-position pairs
        private void initializeLetterAssignments(List<String> words) {
            whiteSpacePositions.clear();
            for (Node space : spaces) {
                if (space.isVisible()) {
                    whiteSpacePositions.add(spaces.indexOf(space));
                }
            }

            int counter = 0;
            for (String word : words) {
                for (char letter : word.toCharArray()) {
                    letterAssignments.add(new Pair<>(letter, whiteSpacePositions.get(counter)));
                    counter++;
                }
            }
        }

        private boolean revealNextLetter(List<Character> letters) {
            for (int space : revealOrder) {
                // Skip unactivated tiles
                if (!spaces.get(space).isVisible()) {
                    continue;
                }

                for (Pair<Character, Integer> assignment : letterAssignments) {
                    if (assignment.getValue() == space && letters.contains(assignment.getKey())) {
                        Label label = (Label) spaces.get(space);
                        if (label.getStyle().compareTo("-fx-background-color: white; -fx-background-radius: 1;") == 0 && label.getText().isEmpty()) {
                            blueScreen(label, assignment.getKey().toString());
                            playSound();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        // Alternate between the two ding sounds since I encountered lag here
        private void playSound() {
            if (dingSound1.isPlaying()) {
                dingSound2.play(1);
            } else {
                dingSound1.play(1);
            }
        }
        
    }.start();
}

private void blueScreen(Label label, String letter) {
    new AnimationTimer() {
        long startTime = 0;
        Random random = new Random();
        @Override
        public void handle(long now) {
            label.setStyle("-fx-background-color: #5656fa; -fx-background-radius: 1;");
            if (startTime == 0) {
                startTime = now;
            }

            long elapsedTime = now - startTime;
            // After the blue screen, the letter will take between 2.2s and 3.2s to be revealed
            if (elapsedTime >= TimeUnit.MILLISECONDS.toNanos(2800 + random.nextInt(-600, 400))) {
                label.setStyle("-fx-background-color: white; -fx-background-radius: 1;");
                label.setText(letter);
                stop();
            }
        }
    }.start();
}


    private void enableButtonTimer() {
        new AnimationTimer() {
            long startTime = System.nanoTime();

            @Override
            public void handle(long now) {
                if (now - startTime > 1000000000) {
                    beginPuzzleButton.setDisable(false);
                    stop();
                }
            }
            
        }.start();
    }
}
