package main.java;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Контроллер с РАБОЧИМ методом удаления
 */
public class ToyManagementController {

    @FXML public TableView<Toy> toysTable;
    @FXML public TextField searchField;
    @FXML public Label statusLabel;

    @FXML public Button fillDbButton;
    @FXML public Button refreshButton;
    @FXML public Button indexesButton;
    @FXML public Button addButton;
    @FXML public Button deleteButton;
    @FXML public Button sortButton;
    @FXML public Button exportButton;
    @FXML public Button searchButton;
    @FXML public Button loadBackupButton;

    @FXML public TableColumn<Toy, String> codeColumn;
    @FXML public TableColumn<Toy, String> nameColumn;
    @FXML public TableColumn<Toy, Integer> minAgeColumn;
    @FXML public TableColumn<Toy, Integer> maxAgeColumn;
    @FXML public TableColumn<Toy, Double> priceColumn;
    @FXML public TableColumn<Toy, Integer> quantityColumn;
    @FXML public TableColumn<Toy, LocalDate> dateColumn;
    @FXML public TableColumn<Toy, String> supplierColumn;

    private ToyDatabase database;
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private Timer autoBackupTimer;

    public void initialize() {
        try {
            System.out.println("📱 Инициализация контроллера...");

            database = new ToyDatabase();
            System.out.println("✅ БД инициализирована");

            database.loadIndexes();
            System.out.println("✅ Индексы загружены");

            setupTableColumns();
            System.out.println("✅ Таблица настроена");

            setupButtons();
            System.out.println("✅ Кнопки подключены");

            refreshTable();
            System.out.println("✅ Таблица загружена");

            startAutoBackupTimer();
            System.out.println("✅ Автобекап запущен");

        } catch (Exception e) {
            System.err.println("❌ Ошибка инициализации: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка инициализации",
                    "❌ " + e.getMessage());
        }
    }

    private void setupButtons() {
        if (fillDbButton != null) fillDbButton.setOnAction(e -> handleFillDatabase());
        if (refreshButton != null) refreshButton.setOnAction(e -> handleRefresh());
        if (indexesButton != null) indexesButton.setOnAction(e -> handleBuildIndexes());
        if (addButton != null) addButton.setOnAction(e -> handleAddToy());
        if (deleteButton != null) deleteButton.setOnAction(e -> handleDelete());
        if (sortButton != null) sortButton.setOnAction(e -> handleSort());
        if (exportButton != null) exportButton.setOnAction(e -> handleExport());
        if (searchButton != null) searchButton.setOnAction(e -> handleSearch());
        if (loadBackupButton != null) loadBackupButton.setOnAction(e -> handleLoadBackup());

        if (searchField != null) {
            searchField.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    handleSearch();
                }
            });
        }
    }

    private void setupTableColumns() {
        if (codeColumn != null) codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        if (nameColumn != null) nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (minAgeColumn != null) minAgeColumn.setCellValueFactory(new PropertyValueFactory<>("minAge"));
        if (maxAgeColumn != null) maxAgeColumn.setCellValueFactory(new PropertyValueFactory<>("maxAge"));
        if (priceColumn != null) priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (quantityColumn != null) quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        if (dateColumn != null) dateColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalDate"));
        if (supplierColumn != null) supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
    }

    private void startAutoBackupTimer() {
        autoBackupTimer = new Timer("AutoBackupTimer", true);
        long BACKUP_INTERVAL = 5 * 60 * 1000;

        autoBackupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (database.createBackup()) {
                        System.out.println("⏰ Автоматический бекап создан");
                        database.cleanOldBackups(10);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Ошибка при автоматическом бекапе: " + e.getMessage());
                }
            }
        }, BACKUP_INTERVAL, BACKUP_INTERVAL);
    }

    public void stopAutoBackupTimer() {
        if (autoBackupTimer != null) {
            autoBackupTimer.cancel();
            System.out.println("⏹️ Таймер автобекапа остановлен");
        }
    }

    private void handleFillDatabase() {
        try {
            database.fillWithTestData();
            refreshTable();
            showAlert(Alert.AlertType.INFORMATION, "Успех", "✅ База заполнена 10 тестовыми записями!");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка: " + e.getMessage());
        }
    }

    private void handleRefresh() {
        refreshTable();
        updateStatus("Таблица обновлена");
    }

    private void handleBuildIndexes() {
        try {
            database.buildIndexes();
            showAlert(Alert.AlertType.INFORMATION, "Успех", "✅ Индексы успешно построены!");
            updateStatus("Индексы построены");
        } catch (IOException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка: " + e.getMessage());
        }
    }

    private void handleAddToy() {
        Dialog<Toy> dialog = new Dialog<>();
        dialog.setTitle("Добавить игрушку");
        dialog.setHeaderText("Введите данные новой игрушки");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField codeField = new TextField();
        TextField nameField = new TextField();
        TextField minAgeField = new TextField();
        TextField maxAgeField = new TextField();
        TextField priceField = new TextField();
        TextField quantityField = new TextField();
        TextField dateField = new TextField();
        TextField supplierField = new TextField();

        grid.add(new Label("Код:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Название:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Мин. возраст:"), 0, 2);
        grid.add(minAgeField, 1, 2);
        grid.add(new Label("Макс. возраст:"), 0, 3);
        grid.add(maxAgeField, 1, 3);
        grid.add(new Label("Цена:"), 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(new Label("Кол-во:"), 0, 5);
        grid.add(quantityField, 1, 5);
        grid.add(new Label("Дата (dd.MM.yyyy):"), 0, 6);
        grid.add(dateField, 1, 6);
        grid.add(new Label("Поставщик:"), 0, 7);
        grid.add(supplierField, 1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    return new Toy(
                            codeField.getText().trim(),
                            nameField.getText().trim(),
                            Integer.parseInt(minAgeField.getText().trim()),
                            Integer.parseInt(maxAgeField.getText().trim()),
                            Double.parseDouble(priceField.getText().trim()),
                            Integer.parseInt(quantityField.getText().trim()),
                            LocalDate.parse(dateField.getText().trim(), DATE_FORMATTER),
                            supplierField.getText().trim()
                    );
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Проверьте корректность данных: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(toy -> {
            try {
                database.addToy(toy);
                refreshTable();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "✅ Игрушка добавлена!");
                updateStatus("Добавлена игрушка: " + toy.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка: " + e.getMessage());
            }
        });
    }

    private void handleDelete() {
        Toy selectedToy = toysTable.getSelectionModel().getSelectedItem();

        if (selectedToy != null) {
            handleDeleteSelected(selectedToy);
        } else {
            handleDeleteByIndex();
        }
    }

    private void handleDeleteSelected(Toy selectedToy) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Подтверждение");
        confirmDialog.setHeaderText("Удалить игрушку?");
        confirmDialog.setContentText("Вы уверены что хотите удалить:\n" + selectedToy.getName() + " (" + selectedToy.getCode() + ")?");

        if (confirmDialog.showAndWait().get() == ButtonType.OK) {
            try {
                boolean deleted = database.deleteToy(selectedToy);

                if (deleted) {
                    refreshTable();
                    showAlert(Alert.AlertType.INFORMATION, "Успех",
                            "✅ Удалено: " + selectedToy.getName());
                    updateStatus("Удалено: " + selectedToy.getName());
                } else {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Не удалось удалить игрушку!");
                }
            } catch (IOException | ClassNotFoundException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка при удалении: " + e.getMessage());
            }
        }
    }

    private void handleDeleteByIndex() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Удаление по индексам");
        dialog.setHeaderText("Выберите критерий удаления");

        VBox vbox = new VBox(10);
        vbox.setPadding(new javafx.geometry.Insets(20));

        Label info = new Label("Никакой элемент не выбран в таблице.");
        Label info2 = new Label("Удалить ВСЕ игрушки по критерию:");

        ToggleGroup group = new ToggleGroup();
        RadioButton supplierRb = new RadioButton("🏢 По поставщику");
        supplierRb.setToggleGroup(group);
        supplierRb.setSelected(true);

        RadioButton dateRb = new RadioButton("📅 По дате поступления");
        dateRb.setToggleGroup(group);

        RadioButton ageRb = new RadioButton("👶 По возрастному диапазону");
        ageRb.setToggleGroup(group);

        TextField valueField = new TextField();
        valueField.setPromptText("Введите значение");

        vbox.getChildren().addAll(info, info2, new Separator(), supplierRb, dateRb, ageRb,
                new Label("Введите значение:"), valueField);

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (supplierRb.isSelected()) return 0;
                if (dateRb.isSelected()) return 1;
                if (ageRb.isSelected()) return 2;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(deleteType -> {
            String value = valueField.getText().trim();
            if (value.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Внимание", "⚠️ Введите значение!");
                return;
            }

            try {
                int deleted = 0;
                String message = "";

                switch (deleteType) {
                    case 0:
                        deleted = database.deleteBySupplier(value);
                        message = "удалено игрушек от " + value;
                        break;
                    case 1:
                        try {
                            LocalDate date = LocalDate.parse(value, DATE_FORMATTER);
                            deleted = database.deleteByDate(date);
                            message = "удалено игрушек с датой " + value;
                        } catch (Exception e) {
                            showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Неверный формат даты! Используйте: dd.MM.yyyy");
                            return;
                        }
                        break;
                    case 2:
                        deleted = database.deleteByAgeRange(value);
                        message = "удалено игрушек для возраста " + value;
                        break;
                }

                if (deleted > 0) {
                    refreshTable();
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "✅ " + deleted + " " + message);
                    updateStatus(deleted + " игрушек удалено");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Не найдено", "❌ Игрушек не найдено с таким критерием");
                }
            } catch (IOException | ClassNotFoundException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка при удалении: " + e.getMessage());
            }
        });
    }

    private void handleExport() {
        try {
            List<Toy> toys = database.getAllToys();

            if (toys.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Внимание", "❌ Таблица пуста! Нечего экспортировать.");
                return;
            }

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Экспорт в CSV");
            dialog.setHeaderText("Введите имя файла");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20));

            Label label = new Label("Имя файла:");
            TextField fileNameField = new TextField();
            fileNameField.setText("toys_" + java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
            fileNameField.setPromptText("toys_export.csv");

            Label info = new Label("Файл будет сохранён в папку: exports/");
            info.setStyle("-fx-text-fill: #888888; -fx-font-size: 11;");

            grid.add(label, 0, 0);
            grid.add(fileNameField, 1, 0);
            grid.add(info, 0, 1, 2, 1);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    String fileName = fileNameField.getText().trim();
                    if (!fileName.endsWith(".csv")) {
                        fileName += ".csv";
                    }
                    return fileName;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(fileName -> {
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Подтверждение");
                confirmDialog.setHeaderText("Экспортировать таблицу?");
                confirmDialog.setContentText("Будут экспортированы " + toys.size() + " записей в файл:\n" + fileName);

                if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                    System.out.println("📥 Начало экспорта в CSV...");
                    if (CSVExporter.exportToCSV(toys, fileName)) {
                        showAlert(Alert.AlertType.INFORMATION, "Успех",
                                "✅ Таблица успешно экспортирована!\n\nФайл: exports/" + fileName + "\n\n" + toys.size() + " записей");
                        updateStatus("Экспортировано " + toys.size() + " записей в " + fileName);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка при экспорте!");
                    }
                }
            });
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Ошибка при подготовке к экспорту: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка: " + e.getMessage());
        }
    }

    private void handleLoadBackup() {
        try {
            List<String> backups = database.getBackupList();

            if (backups.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Внимание", "❌ Бекапов не найдено!");
                return;
            }

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Загрузить базу");
            dialog.setHeaderText("Выберите бекап для загрузки");

            VBox vbox = new VBox(10);
            vbox.setPadding(new javafx.geometry.Insets(20));

            ComboBox<String> backupCombo = new ComboBox<>();
            ObservableList<String> items = FXCollections.observableArrayList(backups);
            backupCombo.setItems(items);
            backupCombo.setValue(backups.get(0));

            Label label = new Label("Доступные бекапы:");
            Label info = new Label("Выбранный бекап будет загружен и восстановлен.\nТекущие данные будут сохранены в pre_restore_backup.dat");
            info.setWrapText(true);
            info.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 11;");

            vbox.getChildren().addAll(label, backupCombo, new Separator(), info);

            dialog.getDialogPane().setContent(vbox);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return backupCombo.getValue();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(selectedBackup -> {
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Подтверждение");
                confirmDialog.setHeaderText("Загрузить базу?");
                confirmDialog.setContentText("Вы уверены что хотите загрузить базу из:\n" + selectedBackup + "\n\nТекущие данные будут сохранены.");

                if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                    System.out.println("📥 Загрузка бекапа: " + selectedBackup);
                    if (database.restoreFromBackup(selectedBackup)) {
                        System.out.println("✅ Бекап загружен");
                        refreshTable();
                        showAlert(Alert.AlertType.INFORMATION, "Успех",
                                "✅ База загружена из бекапа:\n" + selectedBackup);
                        updateStatus("База загружена: " + selectedBackup);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Не удалось загрузить бекап!");
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Ошибка в handleLoadBackup: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ " + e.getMessage());
        }
    }

    private void handleSearch() {
        String searchValue = searchField.getText().trim();
        if (searchValue.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "⚠️ Введите значение для поиска!");
            return;
        }

        try {
            List<Toy> results = null;

            try {
                results = database.findBySupplier(searchValue);
                if (!results.isEmpty()) {
                    updateTable(results);
                    updateStatus("Поиск по поставщику: " + results.size() + " найдено");
                    return;
                }

                try {
                    LocalDate date = LocalDate.parse(searchValue, DATE_FORMATTER);
                    results = database.findByDate(date);
                    if (!results.isEmpty()) {
                        updateTable(results);
                        updateStatus("Поиск по дате: " + results.size() + " найдено");
                        return;
                    }
                } catch (Exception ignored) {}

                results = database.findByAgeRange(searchValue);
                if (!results.isEmpty()) {
                    updateTable(results);
                    updateStatus("Поиск по возрасту: " + results.size() + " найдено");
                    return;
                }

                showAlert(Alert.AlertType.WARNING, "Результат", "❌ Ничего не найдено по запросу: " + searchValue);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка поиска: " + e.getMessage());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ " + e.getMessage());
        }
    }

    private void handleSort() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Сортировка");
        dialog.setHeaderText("Выберите параметры сортировки");

        VBox vbox = new VBox(10);
        vbox.setPadding(new javafx.geometry.Insets(20));

        ToggleGroup group = new ToggleGroup();
        RadioButton dateRb = new RadioButton("📅 По дате поступления");
        dateRb.setToggleGroup(group);
        dateRb.setSelected(true);

        RadioButton supplierRb = new RadioButton("🏢 По поставщику");
        supplierRb.setToggleGroup(group);

        RadioButton ageRb = new RadioButton("👶 По возрасту");
        ageRb.setToggleGroup(group);

        CheckBox ascendingCb = new CheckBox("По возрастанию");
        ascendingCb.setSelected(true);

        vbox.getChildren().addAll(new Label("Выберите поле:"), dateRb, supplierRb, ageRb,
                new Separator(), ascendingCb);

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (dateRb.isSelected()) return 0;
                if (supplierRb.isSelected()) return 1;
                if (ageRb.isSelected()) return 2;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(sortType -> {
            try {
                List<Toy> sorted = null;
                boolean ascending = vbox.getChildren().stream()
                        .filter(n -> n instanceof CheckBox)
                        .map(n -> (CheckBox) n)
                        .findFirst()
                        .map(CheckBox::isSelected)
                        .orElse(true);

                switch (sortType) {
                    case 0:
                        sorted = database.getToysByDate(ascending);
                        break;
                    case 1:
                        sorted = database.getToysBySupplier(ascending);
                        break;
                    case 2:
                        sorted = database.getToysByAgeRange(ascending);
                        break;
                }

                if (sorted != null) {
                    updateTable(sorted);
                    updateStatus("Сортировка: " + sorted.size() + " записей");
                }
            } catch (IOException | ClassNotFoundException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка: " + e.getMessage());
            }
        });
    }

    private void refreshTable() {
        try {
            System.out.println("🔄 Обновление таблицы...");
            List<Toy> toys = database.getAllToys();
            System.out.println("✅ Загружено " + toys.size() + " записей");
            updateTable(toys);
            updateStatus("Всего записей: " + toys.size());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Ошибка загрузки таблицы: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка: " + e.getMessage());
        }
    }

    private void updateTable(List<Toy> toys) {
        ObservableList<Toy> data = FXCollections.observableArrayList(toys);
        if (toysTable != null) {
            toysTable.setItems(data);
        }
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText("✓ " + message);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}