package main.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ToyManagementApp extends Application {

    private ToyDatabase database;

    @Override
    public void start(Stage primaryStage) throws IOException {
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.verbose", "true");
        System.setProperty("javafx.platform", "desktop");
        System.setProperty("glass.win.uiScale", "1.0");
        System.setProperty("_JAVA_AWT_WM_NONREPARENTING", "1");

        try {
            database = new ToyDatabase();
            try {
                database.autoLoadLatestBackup();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            database.cleanOldBackups(5);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        URL fxmlUrl = getClass().getClassLoader().getResource("main.fxml");
        if (fxmlUrl == null) throw new RuntimeException("main.fxml не найден!");

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        ToyManagementController controller = new ToyManagementController();
        loader.setController(controller);
        Parent root = loader.load();
        controller.initialize();

        Scene scene = new Scene(root, 1400, 850);

        primaryStage.initStyle(StageStyle.DECORATED);

        primaryStage.setTitle("Toy Management");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1400);
        primaryStage.setHeight(850);
        primaryStage.centerOnScreen();
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);

        primaryStage.setOnCloseRequest(event -> {
            try {
                if (database != null) database.createBackup();
            } catch (Exception ignored) {}
            if (controller != null) controller.stopAutoBackupTimer();
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        System.clearProperty("WAYLAND_DISPLAY");
        launch(args);
    }
}