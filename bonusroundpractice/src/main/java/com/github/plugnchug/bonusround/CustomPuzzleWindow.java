package com.github.plugnchug.bonusround;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class CustomPuzzleWindow {
    public static String topRow = "";
    public static String topMiddleRow = "";
    public static String bottomMiddleRow = "";
    public static String bottomRow = "";
    public static String category = "";
    public static int wordCount = 0;
    public static int length = 0;

    @FXML
    Button cancelButton;
    @FXML
    Button submitButton;
    @FXML
    ToggleButton infoButton;
    @FXML
    Label infoLabel;
    @FXML
    TextField topField;
    @FXML
    TextField topMiddleField;
    @FXML
    TextField bottomMiddleField;
    @FXML
    TextField bottomField;
    @FXML
    TextField categoryField;

    @FXML
    private void infoToggle() {
        if (infoButton.isSelected()) {
            infoLabel.setVisible(true);
        } else {
            infoLabel.setVisible(false);
        }
    }

    @FXML
    private void cancel(ActionEvent event) { 
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void submit(ActionEvent event) {
        boolean invalid = false;
        if (topField.getText().isEmpty() && topMiddleField.getText().isEmpty() && bottomMiddleField.getText().isEmpty() && bottomField.getText().isEmpty()) {
            topField.setStyle("-fx-border-color: red;");
            topMiddleField.setStyle("-fx-border-color: red;");
            bottomMiddleField.setStyle("-fx-border-color: red;");
            bottomField.setStyle("-fx-border-color: red;");
            invalid = true;
        }
        if (categoryField.getText().isEmpty()) {
            categoryField.setStyle("-fx-border-color: red;");
            invalid = true;
        }
        if (invalid) {
            return;
        }

        topRow = topField.getText();
        topMiddleRow = topMiddleField.getText();
        bottomMiddleRow = bottomMiddleField.getText();
        bottomRow = bottomField.getText();
        category = categoryField.getText();

        System.out.println(topRow + " " + topMiddleRow + " " + bottomMiddleRow + " " + bottomRow);
        System.out.println(category);

        if (!verifyAnswer()) {
            System.out.println("Bad inputs");
            topRow = "";
            topMiddleRow = "";
            bottomMiddleRow = "";
            bottomRow = "";
            return;
        }

        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void formatAnswer(KeyEvent event) {
        TextField field = (TextField) event.getSource();
        int pos = field.getCaretPosition();
        field.setText(field.getText().toUpperCase());
        field.positionCaret(pos);
        field.setStyle("");

        topField.setStyle("-fx-border-color: transparent;");
        topMiddleField.setStyle("-fx-border-color: transparent;");
        bottomMiddleField.setStyle("-fx-border-color: transparent;");
        bottomField.setStyle("-fx-border-color: transparent;");

        if (topField.getText().length() > 12) {
            topField.setStyle("-fx-border-color: red;");
        }
        if (bottomField.getText().length() > 12) {
            bottomField.setStyle("-fx-border-color: red;");
        }
        if (topMiddleField.getText().length() > 14) {
            topMiddleField.setStyle("-fx-border-color: red;");
        }
        if (bottomMiddleField.getText().length() > 14) {
            bottomMiddleField.setStyle("-fx-border-color: red;");
        }

        if ((!topField.getText().isBlank() || !bottomField.getText().isBlank()) && topMiddleField.getText().length() > 12) {
            topMiddleField.setStyle("-fx-border-color: red;");
        }
        if ((!topField.getText().isBlank() || !bottomField.getText().isBlank()) && bottomMiddleField.getText().length() > 12) {
            bottomMiddleField.setStyle("-fx-border-color: red;");
        }
    }

    @FXML
    private void restoreCategoryField() {
        categoryField.setStyle("");
    }

    private boolean verifyAnswer() {
        if (topRow.length() > 12 || bottomRow.length() > 12 || topMiddleRow.length() > 14 || bottomMiddleRow.length() > 14) {
            return false;
        }

        if ((!topRow.isBlank() || !bottomRow.isBlank()) && (topMiddleRow.length() > 12 || bottomMiddleRow.length() > 12)) {
            return false;
        }

        return true;
    }

}
