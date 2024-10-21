package com.github.plugnchug.bonusround;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Pair;

public class Animators {
    public static boolean requestedStop = false;

    public static ObservableList<Node> spaces;
    private static List<Integer> whiteSpacePositions = new ArrayList<>();

    private List<String> words;
    private boolean noRSTLNE;
    private boolean fastMode;

    private HBox consonants;
    private HBox vowels;
    private VBox settings;
    private Label readyText;
    private Label countdownText;
    private Button enterAnswerButton;
    private TextField answerField;
    private Button stopRoundButton;
    private Button beginPuzzleButton;

    public Animators(List<String> w, boolean n, boolean f) {
        this.words = w;
        this.noRSTLNE = n;
        this.fastMode = f;
    }

    public void linkConsonants(HBox c) {
        this.consonants = c;
    }
    public void linkVowels(HBox v) {
        this.vowels = v;
    }
    public void linkSettings(VBox s) {
        this.settings = s;
    }
    public void linkReady(Label r) {
        this.readyText = r;
    }
    public void linkCountdown(Label c) {
        this.countdownText = c;
    }
    public void linkAnswerButton(Button b) {
        this.enterAnswerButton = b;
    }
    public void linkAnswerField(TextField a) {
        this.answerField = a;
    }
    public void linkStopRoundButton(Button s) {
        this.stopRoundButton = s;
    }
    public void linkBeginPuzzleButton(Button b) {
        this.beginPuzzleButton = b;
    }

    public void animateReveal(String words, int row, int startPos) {
        new AnimationTimer() {
            long startTime = 0;
            int position = 0;

            @Override
            public void handle(long now) {
                if (requestedStop) {
                    stop();
                }
                try {
                    int delay;
                    if (fastMode) {
                        delay = 20;
                    } else {
                        delay = 60;
                    }

                    // Note this only deals with one row: the row given in the parameters.
                    // This is why we call this method twice when an answer needs two rows
                    if (now > startTime + TimeUnit.MILLISECONDS.toNanos(delay)) {
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

    public void animateLetters(List<Character> letters, int context, boolean showAnswerMode) {
        new AnimationTimer() {
            long startTime = 0;
            boolean initialized = false;
            boolean revealStarted = false;

            List<Pair<Character, Integer>> letterAssignments = new ArrayList<>();
            List<Integer> revealOrder = Arrays.asList(25, 39, 24, 38, 23, 37, 22, 36, 21, 35, 20, 34, 19, 33, 18, 32, 17, 31, 16, 30, 15, 29, 14, 28, 13, 27, 12, 26);
            List<Integer> reverseReveal = Arrays.asList(26, 12, 27, 13, 28, 14, 29, 15, 30, 16, 31, 17, 32, 18, 33, 19, 34, 20, 35, 21, 36, 22, 37, 23, 38, 24, 39, 25);

            @Override
            public void handle(long now) {
                if (requestedStop) {
                    stop();
                }

                if (startTime == 0) {
                    startTime = now;
                }

                try {
                    long elapsedTime = now - startTime;
                    if (!showAnswerMode) {
                        long initializeDelay;
                        long firstLetterDelay;
                        if (fastMode) {
                            initializeDelay = 500;
                            firstLetterDelay = 750;
                        } else {
                            initializeDelay = 2000;
                            firstLetterDelay = 2500;
                        }

                        // Delay list initialization, or skip straight to letter choosing if noRSTLNE is false
                        if (!initialized && elapsedTime >= TimeUnit.MILLISECONDS.toNanos(initializeDelay)) {
                            initializeLetterAssignments(words);
                            initialized = true;
                            // Only skip if this is called in the RSTLNE phase
                            if (noRSTLNE && context == 0) {
                                System.out.println("RSTLNE disabled!");
                                chooseLettersTransition();
                                stop();
                            }
                        }

                        // 2.5 second delay until letter reveals
                        if (initialized && (revealStarted || elapsedTime >= TimeUnit.MILLISECONDS.toNanos(firstLetterDelay))) {
                            revealStarted = true;

                            if (fastMode) {
                                allLettersAtOnce(letters);
                                chooseLettersTransition();
                                stop();
                            } else {
                                // Reveal letters every 1.15 seconds
                                if (elapsedTime >= TimeUnit.MILLISECONDS.toNanos(1150)) {
                                    startTime = now;
                                    boolean letterRevealed = revealNextLetter(letters);
                                    // Stop if no letters were revealed
                                    if (!letterRevealed) {
                                        System.out.println("Done revealing letters!");

                                        // Transition to chooseletters
                                        chooseLettersTransition();
                                        stop();
                                    }
                                }
                            }
                            
                        }
                    } else {
                        // 0.5 second delay until list initialization
                        if (!initialized && elapsedTime >= TimeUnit.MILLISECONDS.toNanos(100)) {
                            initializeLetterAssignments(words);
                            initialized = true;
                        }
                        // Reveal letters every 0.3 seconds
                        if (elapsedTime >= TimeUnit.MILLISECONDS.toNanos(300)) {
                            startTime = now;
                            boolean letterRevealed = revealNextLetter(letters);

                            // Stop if no letters were revealed
                            if (!letterRevealed) {
                                System.out.println("Answer revealed!");
                                stopRoundButton.setDisable(true);
                                beginPuzzleButton.setDisable(false);
                                settings.setDisable(false);
                                stop();
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    stop();
                }
            }

            private void chooseLettersTransition() {
                switch (context) {
                    case 0:
                        Game.rstlne.stop();
                        Game.chooseLetters.play(0.6f, true, 56494);
                        enableHBoxes();
                        break;
                    case 1:
                        guessTransitionTimer();
                        break;
                
                    default:
                        break;
                }
            }

            // Enables the FXML letter buttons
            private void enableHBoxes() {
                if (consonants != null) {
                    if (!Game.noRSTLNE) {
                        for (Node node : consonants.getChildren()) {
                            Button b = (Button) node;
                            if (BonusGameBackend.rstlne.contains(b.getText().toCharArray()[0])) {
                                node.setDisable(true);
                            }
                        }
                    }
                    consonants.setDisable(false);
                }
                if (vowels != null) {
                    if (!Game.noRSTLNE) {
                        for (Node node : vowels.getChildren()) {
                            Button b = (Button) node;
                            if (b.getText().compareTo("E") == 0) {
                                node.setDisable(true);
                            }
                        }
                    }
                    vowels.setDisable(false);
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
                if (!showAnswerMode) {
                    for (int space : revealOrder) {
                        // Skip unactivated tiles
                        if (!spaces.get(space).isVisible()) {
                            continue;
                        }

                        for (Pair<Character, Integer> assignment : letterAssignments) {
                            if (assignment.getValue() == space && letters.contains(assignment.getKey())) {
                                Label label = (Label) spaces.get(space);
                                if (label.getStyle().compareTo("-fx-background-color: white; -fx-background-radius: 1;") == 0 && label.getText().isEmpty()) {
                                    blueScreen(label, assignment.getKey().toString(), fastMode);
                                    playSound();
                                    return true;
                                }
                            }
                        }
                    }
                } else {
                    for (int space : reverseReveal) {
                        // Skip unactivated tiles
                        if (!spaces.get(space).isVisible()) {
                            continue;
                        }

                        for (Pair<Character, Integer> assignment : letterAssignments) {
                            if (assignment.getValue() == space && letters.contains(assignment.getKey())) {
                                Label label = (Label) spaces.get(space);
                                if (label.getText().isEmpty()) {
                                    label.setText(assignment.getKey().toString());
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
            }

            private void allLettersAtOnce(List<Character> letters) {
                boolean notPlayed = true;
                for (int space : revealOrder) {
                    // Skip unactivated tiles
                    if (!spaces.get(space).isVisible()) {
                        continue;
                    }

                    for (Pair<Character, Integer> assignment : letterAssignments) {
                        if (assignment.getValue() == space && letters.contains(assignment.getKey())) {
                            Label label = (Label) spaces.get(space);
                            if (label.getStyle().compareTo("-fx-background-color: white; -fx-background-radius: 1;") == 0 && label.getText().isEmpty()) {
                                // Play ding sound only once
                                if (notPlayed) {
                                    notPlayed = false;
                                    playSound();
                                }

                                blueScreen(label, assignment.getKey().toString(), fastMode);
                            }
                        }
                    }
                }
            }

            // Alternate between the two ding sounds since I encountered lag here when trying to
            private void playSound() {
                if (Game.dingSound1.isPlaying()) {
                    Game.dingSound2.play(1);
                } else {
                    Game.dingSound1.play(1);
                }
            }
        }.start();
    }

    private void blueScreen(Label label, String letter, boolean fastMode) {
        new AnimationTimer() {
            long startTime = 0;
            Random random = new Random();
            @Override
            public void handle(long now) {
                if (requestedStop) {
                    stop();
                }
                label.setStyle("-fx-background-color: #5656fa; -fx-background-radius: 1;");
                if (startTime == 0) {
                    startTime = now;
                }

                long elapsedTime = now - startTime;
                long duration;

                if (fastMode) {
                    duration = 1000;
                } else {
                    duration = 2700 + random.nextInt(-500, 500);
                }
                // After the blue screen, the letter will take a random duration to be revealed
                if (elapsedTime >= TimeUnit.MILLISECONDS.toNanos(duration)) {
                    label.setStyle("-fx-background-color: white; -fx-background-radius: 1;");
                    label.setText(letter);
                    stop();
                }
            }
        }.start();
    }

    private void guessTransitionTimer() {
        new AnimationTimer() {
            long startTime = 0;
            @Override
            public void handle(long now) {
                readyText.setVisible(true);
                if (requestedStop) {
                    stop();
                }

                if (startTime == 0) {
                    startTime = now;
                }

                long elapsedTime = now - startTime;
                // Give 3 seconds to the player to think of an answer, then begin the countdown
                if (elapsedTime >= TimeUnit.SECONDS.toNanos(3)) {
                    readyText.setVisible(false);
                    guessTimer();
                    stop();
                } 
                // Enable and focus on the text field one second before the countdown
                else if (elapsedTime >= TimeUnit.SECONDS.toNanos(2)) {
                    answerField.setDisable(false);
                    answerField.requestFocus();
                    enterAnswerButton.setDisable(false);
                }
            }
        }.start();
    }

    private void guessTimer() {
        new AnimationTimer() {
            long startTime = 0;
            Integer counter = 10;
            @Override
            public void handle(long now) {
                if (startTime == 0) {
                    Game.chooseLetters.stop();
                    Game.bonusClock.play(0.4f);
                    startTime = now;
                }
                countdownText.setVisible(true);
                if (counter > 0) {
                    countdownText.setText(counter.toString());
                    long elapsedTime = now - startTime;
                    if (elapsedTime >= TimeUnit.SECONDS.toNanos(1)) {
                        counter--;
                        startTime = now;
                    }
                } else {
                    Game.doubleBuzzer.play(1);
                    answerField.setText("");
                    answerField.setDisable(true);
                    enterAnswerButton.setDisable(true);
                    revealAnswerTimer();
                    stop();
                }
            }
        }.start();
    }

    private void revealAnswerTimer() {
        new AnimationTimer() {
            long startTime = 0;
            @Override
            public void handle(long now) {
                if (requestedStop) {
                    stop();
                }

                if (startTime == 0) {
                    countdownText.setVisible(false);
                    startTime = now;
                }

                long elapsedTime = now - startTime;
                // Reveal answer after 1.5 seconds
                if (elapsedTime >= TimeUnit.MILLISECONDS.toNanos(1500)) {
                    List<Character> allLetters = new ArrayList<>();
                    for (int i = 65; i <= 90; i++) {
                        allLetters.add((char) i);
                    }
                    animateLetters(allLetters, 2, true);
                    stop();
                }
            }
        }.start();
    }
}
