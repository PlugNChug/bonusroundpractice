package com.github.plugnchug.bonusround;

import java.io.IOException;
import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        Window.setRoot("primary");
    }
}