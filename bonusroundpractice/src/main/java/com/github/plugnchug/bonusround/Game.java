package com.github.plugnchug.bonusround;

import java.io.IOException;
import java.util.*;
import javax.sound.sampled.*;

import com.github.plugnchug.bonusround.scraper.BaVScraper;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

public class Game {
    @FXML
    Label categoryDisplay;
    @FXML
    Pane board;
    @FXML
    Button beginPuzzleButton;

    // Sound effects
    public static Sounds puzzleRevealSound = new Sounds("resources/puzzleReveal.wav");

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

        // Pair<String, String> puzzle = BonusGameBackend.getRandomAnswer();
        Pair<String, String> puzzle = BonusGameBackend.snipeWordCount(5);
        
        categoryDisplay.setText(puzzle.getValue());

        Animators.spaces = board.getChildren();
        // Reset all board spaces to be invisible
        for (var space : Animators.spaces) {
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

        BonusGameBackend.calculateWordPosition(words, answerLen);

        System.out.println(puzzle.getKey() + " - " + answerLen);

        Animators.animateLetters(BonusGameBackend.rstlne, words);
    }

    @FXML
    private void stopSounds() {
        rstlne.stop();
        chooseLetters.play(1, true);
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
