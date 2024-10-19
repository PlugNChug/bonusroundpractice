package com.github.plugnchug.bonusround;

import java.io.IOException;
import java.util.*;
import javax.sound.sampled.*;

import com.github.plugnchug.bonusround.scraper.BaVScraper;

import javafx.animation.AnimationTimer;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Pair;

public class Game {
    // Sound effects
    public static Sounds puzzleRevealSound = new Sounds("resources/puzzleReveal.wav");
    public static Sounds dingSound1 = new Sounds("resources/ding.wav");
    public static Sounds dingSound2 = new Sounds("resources/ding.wav");
    public static Sounds buzzer = new Sounds("resources/buzzer.wav");
    public static Sounds doubleBuzzer = new Sounds("resources/doubleBuzzer.wav");

    // Music cues
    public static Sounds rstlne = new Sounds("resources/rstlne.wav");
    public static Sounds chooseLetters = new Sounds("resources/chooseLetters.wav");
    public static Sounds bonusClock = new Sounds("resources/bonusClock.wav");

    // Game settings
    public static boolean enableRSTLNE = true;
    public static boolean enableWildCard = false;

    // Animation/behind the scenes helpers
    Animators animation;
    BonusGameBackend bonusGameFunctionality;
    int consonantCounter = 0;
    Pair<String, String> puzzle;
    static List<String> words;

    // FXML inject variables
    @FXML
    Label categoryDisplay;
    @FXML
    Label readyText;
    @FXML
    Label countdown;
    @FXML
    Pane board;
    @FXML
    Button beginPuzzleButton;
    @FXML
    Button stopRoundButton;
    @FXML
    Button enterAnswerButton;
    @FXML
    CheckBox rstlneCheckBox;
    @FXML
    CheckBox wildCardCheckBox;
    @FXML
    TextField answerField;

    @FXML
    private HBox consonants;
    @FXML 
    private HBox vowels;
    @FXML
    private HBox chosenLetterDisplay;
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
        // Initial game setup
        for (Node n : consonants.getChildren()) {
            n.setDisable(false);
        }
        for (Node n : vowels.getChildren()) {
            n.setDisable(false);
        }

        // Set button states
        consonants.setDisable(true);
        vowels.setDisable(true);
        settings.setDisable(true);
        enterAnswerButton.setDisable(true);
        answerField.setDisable(true);

        // Prevent button spamming that could glitch out the puzzle board
        beginPuzzleButton.setDisable(true);
        stopRoundButton.setDisable(true);
        enableButtonTimer(stopRoundButton);

        // Reset some values
        Animators.requestedStop = false;
        consonantCounter = 0;

        // Initialize the game's backend stuff
        bonusGameFunctionality = new BonusGameBackend(enableWildCard);

        // Get a random puzzle
        puzzle = bonusGameFunctionality.getRandomAnswer();
        // Pair<String, String> puzzle = bonusGameFunctionality.snipeWordCount(2);

        // Split the answer into the rows in a reasonable way
        words = new ArrayList<>(Arrays.asList(puzzle.getKey().split(" ")));

        // Initialize animations
        animation = new Animators(words, enableRSTLNE);
        linkAnimations();
        bonusGameFunctionality.linkAnimation(animation);
        
        // Display the category of the puzzle
        categoryDisplay.setText(puzzle.getValue());

        // Link the board's tiles to the Animators class
        Animators.spaces = board.getChildren();
        // Reset all board spaces to be invisible
        for (Node space : Animators.spaces) {
            space.setVisible(false);
            ((Label) space).setStyle("-fx-background-color: white; -fx-background-radius: 1;");
            ((Label) space).setText("");
        }
        for (Node label : chosenLetterDisplay.getChildren()) {
            ((Label) label).setText("");
        }
        
        // Get answer length
        int answerLen = puzzle.getKey().length();

        // Begin music cues
        rstlne.play(1, true);
        chooseLetters.stop();

        // Begin puzzle layout calculation
        bonusGameFunctionality.calculateWordPosition(words, answerLen);
        // Begin revealing letters
        animation.animateLetters(BonusGameBackend.rstlne, 0, false);
        System.out.println(puzzle.getKey() + " - " + answerLen);
    }

    @FXML
    private void requestStop() {
        // Request to stop all animations (each animation method will check for the static variable Animators.requestedStop)
        Animators.requestedStop = true;
        // Clear board
        Animators.spaces = board.getChildren();
        // Reset all board spaces to be invisible
        for (Node space : Animators.spaces) {
            space.setVisible(false);
            ((Label) space).setText("");
            ((Label) space).setStyle("-fx-background-color: white; -fx-background-radius: 1;");
        }
        // Disable buttons and text field, enable settings and begin puzzle button
        consonants.setDisable(true);
        vowels.setDisable(true);
        stopRoundButton.setDisable(true);
        enterAnswerButton.setDisable(true);
        answerField.setDisable(true);
        settings.setDisable(false);
        beginPuzzleButton.setDisable(false);

        // Reset answer field if filled
        answerField.setText("");
        // Stop music, play buzzer
        rstlne.stop();
        chooseLetters.stop();
        if (!buzzer.isPlaying()) {
            buzzer.play(0.4f);
        }
    }

    @FXML
    private void letterButton(ActionEvent e) {
        // Detect which button was pressed, then add its represented letter to the chosen letter array
        ((Button) e.getSource()).setDisable(true);
        Character letter = ((Button) e.getSource()).getText().toCharArray()[0];
        bonusGameFunctionality.chosenLetters.add(letter);

        for (Node n : chosenLetterDisplay.getChildren()) {
            Label label = (Label) n;
            if (label.getText().isEmpty()) {
                label.setText(letter.toString());
                break;
            }
        }

        if (!bonusGameFunctionality.vowelList.contains(letter)) {
            consonantCounter++;
            if (consonantCounter >= bonusGameFunctionality.limit) {
                consonants.setDisable(true);
            }
        } else {
            vowels.setDisable(true);
        }

        if (consonants.isDisable() && vowels.isDisable()) {
            animation.animateLetters(bonusGameFunctionality.chosenLetters, 1, false);
        }

    }

    @FXML
    private void formatAnswer() {
        int pos = answerField.getCaretPosition();
        answerField.setText(answerField.getText().toUpperCase());
        answerField.positionCaret(pos);
    }

    @FXML
    private void enterAnswer() {
        if (answerField.getText().compareTo(puzzle.getKey()) == 0) {
            System.out.println("Solved!");
        }
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

    private void linkAnimations() {
        animation.linkConsonants(consonants);
        animation.linkVowels(vowels);
        animation.linkReady(readyText);
        animation.linkCountdown(countdown);
        animation.linkAnswerButton(enterAnswerButton);
        animation.linkAnswerField(answerField);
        animation.linkStopRoundButton(stopRoundButton);
        animation.linkBeginPuzzleButton(beginPuzzleButton);
        animation.linkSettings(settings);
    }

    public void enableLetters() {
        consonants.setDisable(false);
        vowels.setDisable(false);
    }

    private void enableButtonTimer(Button b) {
        new AnimationTimer() {
            long startTime = System.nanoTime();

            @Override
            public void handle(long now) {
                if (now - startTime > 1000000000) {
                    b.setDisable(false);
                    stop();
                }
            }
            
        }.start();
    }
}
