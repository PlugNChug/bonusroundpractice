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

    private HBox consonants;
    private HBox vowels;

    public void linkConsonants(HBox c) {
        this.consonants = c;
    }
    public void linkVowels(HBox v) {
        this.vowels = v;
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

    public void animateLetters(List<Character> letters, List<String> words, boolean enableRSTLNE) {
        new AnimationTimer() {
            long startTime = 0;
            boolean initialized = false;
            boolean revealStarted = false;

            List<Pair<Character, Integer>> letterAssignments = new ArrayList<>();
            int[] revealOrder = {25, 39, 24, 38, 23, 37, 22, 36, 21, 35, 20, 34, 19, 33, 18, 32, 17, 31, 16, 30, 15, 29, 14, 28, 13, 27, 12, 26};

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

                    // 2.25 second delay until list initialization, or skip straight to letter choosing if enableRSTLNE is false
                    if (!initialized && elapsedTime >= TimeUnit.MILLISECONDS.toNanos(2250)) {
                        initializeLetterAssignments(words);
                        initialized = true;
                        if (!enableRSTLNE) {
                            System.out.println("RSTLNE disabled!");
                            chooseLettersTransition();
                            stop();
                        }
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

                                // Transition to chooseletters
                                chooseLettersTransition();
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
                Game.rstlne.stop();
                Game.chooseLetters.play(1, true);
                enableHBoxes();
            }

            // Enables the FXML letter buttons
            private void enableHBoxes() {
                if (consonants != null) {
                    if (Game.enableRSTLNE) {
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
                    if (Game.enableRSTLNE) {
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

    private void blueScreen(Label label, String letter) {
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
                // After the blue screen, the letter will take between 2.2s and 3.2s to be revealed
                if (elapsedTime >= TimeUnit.MILLISECONDS.toNanos(2700 + random.nextInt(-500, 500))) {
                    label.setStyle("-fx-background-color: white; -fx-background-radius: 1;");
                    label.setText(letter);
                    stop();
                }
            }
        }.start();
    }
}
