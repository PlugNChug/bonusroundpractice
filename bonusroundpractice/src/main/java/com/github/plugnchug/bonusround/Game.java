package com.github.plugnchug.bonusround;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.*;

import javafx.animation.AnimationTimer;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

public class Game {
    // Sound effects
    public static Sounds puzzleRevealSound = new Sounds("sounds/puzzleReveal.wav");
    public static Sounds dingSound1 = new Sounds("sounds/ding.wav");
    public static Sounds dingSound2 = new Sounds("sounds/ding.wav");
    public static Sounds buzzer = new Sounds("sounds/buzzer.wav");
    public static Sounds doubleBuzzer = new Sounds("sounds/doubleBuzzer.wav");

    // Music cues
    public static Sounds rstlne = new Sounds("sounds/rstlne.wav");
    public static Sounds chooseLetters = new Sounds("sounds/chooseLetters2.wav");
    public static Sounds bonusClock = new Sounds("sounds/bonusClock.wav");
    public static Sounds bonusClockEnd = new Sounds("sounds/bonusClockEnd.wav");
    public static Sounds win = new Sounds("sounds/win.wav");
    public static Sounds speedUp = new Sounds("sounds/speedUp.wav");

    // Game settings
    public static boolean noRSTLNE = false;
    public static boolean enableWildCard = false;
    public static boolean fastMode = false;
    public static boolean specificWordCount = false;
    public static boolean specificSeason = false;
    public static boolean endlessMode = false;
    public static boolean customPuzzle = false;
    public static int customTimer = 0;

    // Animation/behind the scenes helpers
    Animators animation;
    BonusGameBackend bonusGameFunctionality;
    int consonantCounter = 0;
    Pair<String, String> puzzle;
    static List<String> words;
    static int endlessStreak = 0;
    static boolean endlessKeepGoing = true;

    // FXML inject variables
    @FXML
    Label categoryDisplay;
    @FXML
    Label readyText;
    @FXML
    Label countdown;
    @FXML
    Pane board;

    // Game control/answering elements
    @FXML
    Button beginPuzzleButton;
    @FXML
    Button stopRoundButton;
    @FXML
    Button enterAnswerButton;
    @FXML
    TextField answerField;
    @FXML
    Pane outcomePane;

    // Settings elements
    @FXML
    private VBox settings;
    @FXML
    CheckBox rstlneCheckBox;
    @FXML
    CheckBox wildCardCheckBox;
    @FXML
    CheckBox fastModeCheckBox;
    @FXML
    CheckBox endlessModeCheckBox;
    @FXML
    CheckBox wordCountCheckBox;
    @FXML
    Slider wordCountSlider;
    @FXML
    CheckBox seasonCheckBox;
    @FXML
    TextField seasonTextField;
    @FXML
    CheckBox customPuzzleCheckBox;
    @FXML
    Button customPuzzleButton;
    @FXML
    CheckBox timerCheckBox;
    @FXML
    TextField timerTextField;

    // Buttons/display elements
    @FXML
    private HBox consonants;
    @FXML 
    private HBox vowels;
    @FXML
    private HBox rstlneDisplay;
    @FXML
    private HBox chosenLetterDisplay;
    

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

        // If the win fanfare bleeds over (because the player clicked the button to begin a new round quick enough after a solve), stop it
        win.stop();

        // Reset outcome display
        outcomePane.setVisible(false);
        setOutcome(false);

        // Reset some values
        Animators.requestedStop = false;
        consonantCounter = 0;
        endlessKeepGoing = true;

        // Initialize the game's backend stuff
        bonusGameFunctionality = new BonusGameBackend(noRSTLNE, enableWildCard);

        // If custom timer is enabled, make sure the time entered is a integer-parseable number
        if (timerCheckBox.isSelected()) {
            try {
                customTimer = Integer.parseInt(timerTextField.getText());
            } catch (NumberFormatException e) {
                timerTextField.setStyle("-fx-border-color: red;");
                return;
            }
        }

        // Get a puzzle
        if (customPuzzle) {
            String answer = CustomPuzzleWindow.topRow + " " + CustomPuzzleWindow.topMiddleRow + " " + CustomPuzzleWindow.bottomMiddleRow + " " + CustomPuzzleWindow.bottomRow;
            // Remove duplicate spaces
            answer.replaceAll("  ", " ");
            puzzle = new Pair<String, String> (answer.strip(), CustomPuzzleWindow.category);
            System.out.println(puzzle.getKey());
        } else if (specificWordCount) {
            // If specific word count is enabled, call the special method that gets only puzzles with the word count
            puzzle = bonusGameFunctionality.snipeWordCount((int) Math.round(wordCountSlider.getValue()));
        } else if (specificSeason) {
            // If specific season is enabled, make sure the season entered is a integer-parseable number
            int season = 0;
            try {
                season = Integer.parseInt(seasonTextField.getText());
            } catch (NumberFormatException e) {
                seasonTextField.setStyle("-fx-border-color: red;");
                return;
            }
            puzzle = bonusGameFunctionality.snipeSeason(season);
        } else {
            puzzle = bonusGameFunctionality.getRandomAnswer();
        }

        // Get answer length
        int answerLen = puzzle.getKey().length();

        // Split the answer into a list of the words it contains
        words = new ArrayList<>(Arrays.asList(puzzle.getKey().split(" ")));

        // If there's only one word but it's longer than 14 characters,
        // ex: "WINDOW-SHOPPING", only possible with a hyphen,
        // split it into two words at the hyphen
        if (words.size() == 1 && answerLen > 14) {
            String s = puzzle.getKey();
            words.clear();
            words.add(s.substring(0, s.indexOf('-') + 1));
            words.add(s.substring(s.indexOf('-') + 1, s.length()));
        }

        // Disable most buttons (settings, etc.)
        startButtonStates();

        // Initialize animations
        animation = new Animators(words, noRSTLNE, fastMode);
        linkAnimations();
        bonusGameFunctionality.linkAnimation(animation);
        
        // Display the category of the puzzle
        categoryDisplay.setText(puzzle.getValue());

        // Link the board's tiles to the Animators class
        Animators.spaces = board.getChildren();
        // Reset labels to be invisible/cleared
        clearLabels();

        // Begin music cues
        if (endlessMode) {
            if (!speedUp.isPlaying()) {
                speedUp.play(1, true);
            }
        } else {
            rstlne.play(1, true);
            chooseLetters.stop();
        }
        

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
        endlessKeepGoing = false;
        
        // Reset labels to be invisible/cleared
        clearLabels();

        // Set element states accordingly
        stopButtonStates();
        categoryDisplay.setText("No Puzzle Loaded");
        outcomePane.setVisible(false);

        // Reset answer field if filled
        answerField.setText("");

        // Reset endless streak
        endlessStreak = 0;

        // Stop music, play buzzer
        rstlne.stop();
        chooseLetters.stop();
        if (!buzzer.isPlaying()) {
            buzzer.play(0.4f);
        }
        speedUp.stop();
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
            if (consonantCounter >= bonusGameFunctionality.consonantLimit) {
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
            // Display game win label
            setOutcome(true);
            outcomePane.setVisible(true);

            // Stop all previous animators
            Animators.requestedStop = true;

            // Check for endless mode, if so, start a new puzzle after 2 seconds
            if (endlessMode) {
                beginNewPuzzleTimer();
            } else {
                // Re-enable settings
                stopButtonStates();
                win.play(0.4f);
                chooseLetters.stop();
            }
            
            animation.revealAnswerTimer(true);
            System.out.println("Solved!");
        }
    }

    @FXML
    private void modifyRSTLNE() {
        if (rstlneCheckBox.isSelected()) {
            noRSTLNE = true;
        } else {
            noRSTLNE = false;
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

    @FXML
    private void modifyFastMode() {
        if (fastModeCheckBox.isSelected()) {
            fastMode = true;
        } else {
            fastMode = false;
        }
    }

    @FXML
    private void modifyEndlessMode() {
        if (endlessModeCheckBox.isSelected()) {
            endlessMode = true;

            customPuzzleCheckBox.setSelected(false);
            customPuzzle = false;
        } else {
            endlessMode = false;
        }
    }

    @FXML
    private void modifyWordCount() {
        if (wordCountCheckBox.isSelected()) {
            specificWordCount = true;
            wordCountSlider.setDisable(false);

            seasonCheckBox.setSelected(false);
            seasonTextField.setDisable(true);
            specificSeason = false;

            customPuzzleCheckBox.setSelected(false);
            customPuzzle = false;
        } else {
            specificWordCount = false;
            wordCountSlider.setDisable(true);
        }
    }

    @FXML
    private void modifySeason() {
        if (seasonCheckBox.isSelected()) {
            specificSeason = true;
            seasonTextField.setDisable(false);

            wordCountCheckBox.setSelected(false);
            wordCountSlider.setDisable(true);
            specificWordCount = false;

            customPuzzleCheckBox.setSelected(false);
            customPuzzle = false;
        } else {
            specificSeason = false;
            seasonTextField.setStyle("-fx-border-color: transparent;");
            seasonTextField.setDisable(true);
        }
    }

    @FXML
    private void modifyTimer() {
        if (timerCheckBox.isSelected()) {
            timerTextField.setDisable(false);
        } else {
            customTimer = 0;
            timerCheckBox.setStyle("-fx-border-color: transparent;");
            timerTextField.setDisable(true);
        }
    }

    @FXML
    private void restoreTextField(KeyEvent event) {
        TextField field = (TextField) event.getSource();
        field.setStyle("");
    }

    @FXML
    private void modifyCustomPuzzle() {
        if (customPuzzleCheckBox.isSelected()) {
            customPuzzle = true;
            customPuzzleButton.setDisable(false);

            seasonCheckBox.setSelected(false);
            seasonTextField.setDisable(true);
            specificSeason = false;

            wordCountCheckBox.setSelected(false);
            wordCountSlider.setDisable(true);
            specificWordCount = false;

            endlessModeCheckBox.setSelected(false);
            endlessMode = false;
        } else {
            customPuzzle = false;
            customPuzzleButton.setDisable(true);
        }
    }

    @FXML
    private void showCustomPuzzle() throws IOException {
        FXMLLoader customPuzzle = new FXMLLoader(getClass().getResource("custom.fxml"));
        Parent window = customPuzzle.load();

        Stage popUp = new Stage();
        popUp.initModality(Modality.APPLICATION_MODAL);
        popUp.initOwner(Window.window);
        popUp.initStyle(StageStyle.UNDECORATED);
        Scene popUpScene = new Scene(window);
        popUp.setScene(popUpScene);
        popUp.show();
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
        animation.linkOutcomePane(outcomePane);
        animation.linkRSTLNEDisplay(rstlneDisplay);
    }

    private void setOutcome(boolean gameWon) {
        Label l = (Label) outcomePane.getChildren().get(outcomePane.getChildren().size() - 1);

        // If the game was won...
        if (gameWon) {
            // ...set the outcome label to the proper win graphic, depending on if endless mode is enabled
            l.setStyle("-fx-background-color: transparent; -fx-text-fill: #222222; -fx-border-color: #222222; -fx-border-width: 4;");
            if (endlessMode) {
                endlessStreak++;
                l.setFont(Font.font("Constantia", FontWeight.BOLD, 32));
                if (endlessStreak == 1) {
                    l.setText(String.format("Nice solve!\n%d puzzle solved.", endlessStreak));
                } else {
                    l.setText(String.format("Nice solve!\n%d puzzles solved.", endlessStreak));
                }
                
            } else {
                l.setFont(Font.font("Constantia", FontWeight.BOLD, 54));
                l.setText("YOU WIN!");
            }
        } else {
            // ...otherwise set the outcome label to the proper loss graphic, depending on if endless mode is enabled
            l.setStyle("-fx-background-color: #222222; -fx-text-fill: white; -fx-border-color: #2a905c; -fx-border-width: 4;");
            if (endlessMode) {
                l.setFont(Font.font("Constantia", FontWeight.BOLD, 32));
                if (endlessStreak == 1) {
                    l.setText(String.format("TIME'S UP!\n%d puzzle solved.", endlessStreak));
                } else {
                    l.setText(String.format("TIME'S UP!\n%d puzzles solved.", endlessStreak));
                }
            } else {
                l.setFont(Font.font("Constantia", FontWeight.BOLD, 54));
                l.setText("TIME'S UP!");
            }
        }
    }

    public void startButtonStates() {
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
    }

    private void stopButtonStates() {
        // Set button states
        consonants.setDisable(true);
        vowels.setDisable(true);
        stopRoundButton.setDisable(true);
        enterAnswerButton.setDisable(true);
        answerField.setDisable(true);
        settings.setDisable(false);
        beginPuzzleButton.setDisable(false);
    }

    private void clearLabels() {
        for (Node space : Animators.spaces) {
            space.setVisible(false);
            ((Label) space).setStyle("-fx-background-color: white; -fx-background-radius: 1;");
            ((Label) space).setText("");
        }
        for (Node label : rstlneDisplay.getChildren()) {
            ((Label) label).setText("");
        }
        for (Node label : chosenLetterDisplay.getChildren()) {
            ((Label) label).setText("");
        }
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
                // Re-enable specified button after 1 second
                if (now - startTime > TimeUnit.MILLISECONDS.toNanos(1000)) {
                    b.setDisable(false);
                    stop();
                }
            }
            
        }.start();
    }

    private void beginNewPuzzleTimer() {
        new AnimationTimer() {
            long startTime = System.nanoTime();

            @Override
            public void handle(long now) {
                if (!endlessKeepGoing) {
                    stop();
                }
                // Automatically begin a new game after 2 seconds
                if (now - startTime > TimeUnit.MILLISECONDS.toNanos(2000)) {
                    try {
                        beginPuzzle();
                    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                        e.printStackTrace();
                    }
                    stop();
                }
            }
            
        }.start();
    }
}
