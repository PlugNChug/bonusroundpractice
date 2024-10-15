package com.github.plugnchug.bonusround;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * JavaFX App
 */
public class Window extends Application {

    public static AudioClip revealPuzzle;
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        loadResources();

        scene = new Scene(loadFXML("primary"));
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Window.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public void loadResources() {
        revealPuzzle = new AudioClip(new File("puzzleReveal.mp3").toURI().toString());
        
        // The first time a sound effect is played its playback is delayed.
        // This is fixed by playing at zero volume right when it's loaded in
        revealPuzzle.play(0);
        
    }
}