module com.github.plugnchug.bonusround {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires org.jsoup;

    opens com.github.plugnchug.bonusround to javafx.fxml;
    exports com.github.plugnchug.bonusround;
}
