package com.github.plugnchug.bonusround;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import javax.sound.sampled.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class Window extends Application {
    private static Scene scene;
    public static Stage window;

    @Override
    public void start(Stage stage) throws IOException, LineUnavailableException {
        audioInitialization();

        scene = new Scene(loadFXML("game"));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Bonus Round Practice");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Window.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    private void audioInitialization() {
        Game.puzzleRevealSound.play(0);
    }
}