package com.github.plugnchug.bonusround;

import java.io.IOException;
import java.util.*;
import javax.sound.sampled.*;

import com.github.plugnchug.bonusround.scraper.BaVScraper;

import javafx.animation.AnimationTimer;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Pair;

public class Game {
    // Sound effects
    public static Sounds puzzleRevealSound = new Sounds("resources/puzzleReveal.wav");
    public static Sounds dingSound1 = new Sounds("resources/ding.wav");
    public static Sounds dingSound2 = new Sounds("resources/ding.wav");
    public static Sounds buzzer = new Sounds("resources/buzzer.wav");

    // Music cues
    public static Sounds rstlne = new Sounds("resources/rstlne.wav");
    public static Sounds chooseLetters = new Sounds("resources/chooseLetters.wav");

    // Game settings
    public static boolean enableRSTLNE = true;
    public static boolean enableWildCard = true;

    // FXML inject variables
    @FXML
    Label categoryDisplay;
    @FXML
    Pane board;
    @FXML
    Button beginPuzzleButton;
    @FXML
    CheckBox rstlneCheckBox;
    @FXML
    CheckBox wildCardCheckBox;

    @FXML
    private HBox consonants;
    @FXML 
    private HBox vowels;
    @FXML
    private VBox settings;
    

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
        Animators.requestedStop = false;
        // Initial game setup
        for (var n : consonants.getChildren()) {
            n.setDisable(false);
        }
        for (var n : vowels.getChildren()) {
            n.setDisable(false);
        }
        consonants.setDisable(true);
        vowels.setDisable(true);
        settings.setDisable(true);

        // Initialize animations
        Animators animation = new Animators();
        animation.linkConsonants(consonants);
        animation.linkVowels(vowels);
        BonusGameBackend bonusGameFunctionality = new BonusGameBackend(animation);

        // Prevent button spamming that could glitch out the puzzle board
        beginPuzzleButton.setDisable(true);
        enableButtonTimer();

        Pair<String, String> puzzle = bonusGameFunctionality.getRandomAnswer();
        // Pair<String, String> puzzle = bonusGameFunctionality.snipeWordCount(2);
        
        // Display the category of the puzzle
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
        rstlne.play(1, true);
        chooseLetters.stop();

        bonusGameFunctionality.calculateWordPosition(words, answerLen);

        System.out.println(puzzle.getKey() + " - " + answerLen);

        animation.animateLetters(BonusGameBackend.rstlne, words, enableRSTLNE);
    }

    @FXML
    private void requestStop() {
        // Request to stop all animations (each animation method will check for the static variable Animators.requestedStop)
        Animators.requestedStop = true;
        // Clear board
        Animators.spaces = board.getChildren();
        // Reset all board spaces to be invisible
        for (var space : Animators.spaces) {
            space.setVisible(false);
            ((Label) space).setText("");
            ((Label) space).setStyle("-fx-background-color: white; -fx-background-radius: 1;");
        }
        // Disable letter buttons, enable settings
        consonants.setDisable(true);
        vowels.setDisable(true);
        settings.setDisable(false);
        // Stop music, player buzzer
        rstlne.stop();
        chooseLetters.stop();
        if (!buzzer.isPlaying()) {
            buzzer.play(0.4f);
        }
    }

    @FXML
    private void letterButton(ActionEvent e) {
        ((Button) e.getSource()).setDisable(true);
    }

    public void enableLetters() {
        consonants.setDisable(false);
        vowels.setDisable(false);
    }

    @FXML
    private void modifyRSTLNE() {
        if (rstlneCheckBox.isSelected()) {
            enableRSTLNE = true;
        } else {
            enableRSTLNE = false;
        }
    }

    @FXML
    private void modifyWildCard() {
        if (wildCardCheckBox.isSelected()) {
            enableWildCard = true;
        } else {
            enableWildCard = false;
        }
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
