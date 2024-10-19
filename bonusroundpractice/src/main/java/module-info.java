module com.github.plugnchug.bonusround {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires transitive javafx.media;
    requires transitive javafx.graphics;
    requires org.jsoup;
    requires transitive java.desktop;
    

    opens com.github.plugnchug.bonusround to javafx.fxml;
    exports com.github.plugnchug.bonusround;
}
