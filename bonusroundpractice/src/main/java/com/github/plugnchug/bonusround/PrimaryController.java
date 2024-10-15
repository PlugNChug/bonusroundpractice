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
        for (String s : words) {
            System.out.println(s);
        }

        // Play the puzzle reveal sound
        Window.revealPuzzle.play();

        // Debug *****************
            // words.clear();
            // words.add("A");
            // words.add("DAIQUIRI");
            // answerLen = 10;
        // ***********************
        
        int startPos;

        List<Integer> wordLengths = new ArrayList<>();

        // Perform the row splits based how many words there are in the puzzle
        switch (words.size()) {
            case 1:
                animateReveal(words.get(0), SECOND_ROW, MIDDLE_SECOND_THIRD - (answerLen / 2));
                break;
            case 2:
                wordLengths.add(words.get(0).length());
                wordLengths.add(words.get(1).length());

                if (((wordLengths.get(0) < 4 || wordLengths.get(1) < 4) && answerLen < 10) || wordLengths.get(0) <= 2 && answerLen < 14) { 
                    startPos = (MIDDLE_SECOND_THIRD - (answerLen / 2));

                    animateReveal(words.get(0) + " " + words.get(1), SECOND_ROW, startPos);
                } else if (answerLen >= 10) {
                    // This block will help with aligning to the larger word
                    if (wordLengths.get(0) > wordLengths.get(1)) {
                        startPos = (MIDDLE_SECOND_THIRD - (wordLengths.get(0) / 2));
                    } else {
                        startPos = (MIDDLE_SECOND_THIRD - (wordLengths.get(1) / 2));
                    }
                    animateReveal(words.get(0), SECOND_ROW, startPos);
                    animateReveal(words.get(1), THIRD_ROW, startPos);
                } else {
                    Random random = new Random();
                    if (random.nextBoolean()) {
                        startPos = (MIDDLE_SECOND_THIRD - (answerLen / 2));

                        animateReveal(words.get(0) + " " + words.get(1), SECOND_ROW, startPos);
                    } else {
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
            case 3:
                wordLengths.add(words.get(0).length());
                wordLengths.add(words.get(1).length());
                wordLengths.add(words.get(2).length());

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
