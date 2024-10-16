package com.github.plugnchug.bonusround;

import java.io.IOException;
import java.util.*;

import com.github.plugnchug.bonusround.scraper.BaVScraper;

import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
        Pair<String, String> puzzle = BonusGameBackend.getRandomAnswer();
        
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
            // words.add("PAIR");
            // words.add("OF");
            // words.add("CHOP");
            // answerLen = 12;
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
            case 4:
                break;
            default:
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
            }
            
        }.start();
    }
}
