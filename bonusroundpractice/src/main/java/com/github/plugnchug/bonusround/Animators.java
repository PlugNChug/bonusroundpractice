package com.github.plugnchug.bonusround;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Pair;

public class Animators {
    public static ObservableList<Node> spaces;

    private static Sounds dingSound1 = new Sounds("resources/ding.wav");
    private static Sounds dingSound2 = new Sounds("resources/ding.wav");

    private static List<Integer> whiteSpacePositions = new ArrayList<>();

    public static void animateReveal(String words, int row, int startPos) {
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

    public static void animateLetters(List<Character> letters, List<String> words) {
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

    private static void blueScreen(Label label, String letter) {
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
}
