package main.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ToyManagementApp extends Application {
    private ToyDatabase database;

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            database = new ToyDatabase();
            try {
                database.autoLoadLatestBackup();
            } catch (Exception e) {
                System.err.println("⚠️ Ошибка автозагрузки: " + e.getMessage());
            }

            database.cleanOldBackups(5);

            URL fxmlUrl = getClass().getClassLoader().getResource("main.fxml");

            if (fxmlUrl == null) {
                throw new RuntimeException("❌ main.fxml НЕ НАЙДЕН!");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            ToyManagementController controller = new ToyManagementController();
            loader.setController(controller);

            var root = loader.load();
            controller.initialize();

            Scene scene = new Scene((Parent) root, 1400, 850);

            primaryStage.setTitle("🧸 Управление базой игрушек");
            primaryStage.setScene(scene);
            primaryStage.setWidth(1400);
            primaryStage.setHeight(850);
            primaryStage.centerOnScreen();
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);

            primaryStage.setOnCloseRequest(event -> {
                try {
                    if (database.createBackup()) {
                        System.out.println("✅ Автоматический бекап создан");
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Ошибка при создании бекапа: " + e.getMessage());
                }
                System.exit(0);
            });
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {
        System.out.println("🧸 Запуск приложения...");


        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}