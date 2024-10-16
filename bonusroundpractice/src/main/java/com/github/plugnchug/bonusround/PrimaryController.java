package com.github.plugnchug.bonusround;

import java.io.IOException;
import java.util.*;

import com.github.plugnchug.bonusround.scraper.BaVScraper;

import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

public class PrimaryController {
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

    @FXML
    private void webScrape() throws IOException {
        try {
            BaVScraper.scrapeSeasons("https://buyavowel.boards.net/page/compendiumindex");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    @FXML
    private void beginPuzzle() throws IOException {
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
        
        int answerLen = puzzle.getKey().length();

        // Split the answer into the rows in a reasonable way
        List<String> words = new ArrayList<>(Arrays.asList(puzzle.getKey().split(" ")));

        // Play the puzzle reveal sound
        Window.revealPuzzle.play();

        // Debug *****************
            // words.clear();
            // words.add("i");
            // words.add("am");
            // words.add("a");
            // words.add("man");
            // words.add("who");
            // words.add("is");
            // words.add("a");
            // words.add("guy");
            // words.add("lol");
            // answerLen = 0;
            // for (String w : words) {
            //     answerLen += w.length() + 1;
            // }
            // answerLen--;
        // ***********************
        
        int startPos;

        List<Integer> wordLengths = new ArrayList<>();
        Random random = new Random();

        // Perform the row splits based how many words there are in the puzzle
        switch (words.size()) {
            // Very simple case: one word means only one row!
            case 1:
                animateReveal(words.get(0), SECOND_ROW, MIDDLE_SECOND_THIRD - (answerLen / 2));
                break;
            // Gets crazier here...
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
                // Answers that don't fall into the above conditions will have a random chance of being either
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
            // Very similar situation to the two word cases
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

        System.out.println(puzzle.getKey() + " - " + answerLen);
    }

    private void animateReveal(String words, int row, int startPos) {
        new AnimationTimer() {
            long startTime = 0;
            int position = 0;

            @Override
            public void handle(long now) {
                try {
                    if (now > startTime + 20000000) {
                        if (!Character.isLetter(words.toCharArray()[position]) && words.toCharArray()[position] != ' ') {
                            spaces.get(row + startPos + position).setVisible(true);
                            ((Label) spaces.get(row + startPos + position)).setText(Character.toString(words.toCharArray()[position]));
                        }
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
