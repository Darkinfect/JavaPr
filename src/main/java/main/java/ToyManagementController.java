package main.java;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Bounds;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    @FXML public TableColumn<Toy, String> dateColumn;
    @FXML public TableColumn<Toy, String> supplierColumn;

    // НОВОЕ: Кастомное меню
    @FXML public HBox customMenuBar;
    @FXML public Button btnFileMenu;
    @FXML public Button btnEditMenu;
    @FXML public Button btnViewMenu;
    @FXML public Button btnHelpMenu;
    @FXML public Label helpLabel;

    // ContextMenus создаются программно
    private ContextMenu fileContextMenu;
    private ContextMenu editContextMenu;
    private ContextMenu viewContextMenu;
    private ContextMenu helpContextMenu;

    private ToyDatabase database;
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private Timer autoBackupTimer;

    @FXML
    public void initialize() {
        try {
            database = new ToyDatabase();
            database.loadIndexes();
            setupTableColumns();
            setupButtons();
            setupCustomMenu(); // НОВОЕ: кастомное меню
            setupContextHelp();
            refreshTable();
            startAutoBackupTimer();
            updateStatus("Готово");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка инициализации", "❌ " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        if (codeColumn != null) codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        if (nameColumn != null) nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (minAgeColumn != null) minAgeColumn.setCellValueFactory(new PropertyValueFactory<>("minAge"));
        if (maxAgeColumn != null) maxAgeColumn.setCellValueFactory(new PropertyValueFactory<>("maxAge"));
        if (priceColumn != null) priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (quantityColumn != null) quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        if (dateColumn != null) dateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getArrivalDate() != null
                        ? cell.getValue().getArrivalDate().format(DATE_FORMATTER)
                        : "")
        );
        if (supplierColumn != null) supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
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

    // ====== НОВОЕ: Кастомное меню ======
    private void setupCustomMenu() {
        System.out.println("DEBUG: setupCustomMenu() начало");

        // Создаём ContextMenus программно (не через FXML!)
        createFileMenu();
        createEditMenu();
        createViewMenu();
        createHelpMenu();

        // Привязываем кнопки к ContextMenus
        if (btnFileMenu != null) {
            btnFileMenu.setOnAction(e -> {
                if (fileContextMenu != null) {
                    Bounds bounds = btnFileMenu.localToScreen(btnFileMenu.getBoundsInLocal());
                    fileContextMenu.show(btnFileMenu, bounds.getCenterX(), bounds.getCenterY() + 25);
                }
            });
        }

        if (btnEditMenu != null) {
            btnEditMenu.setOnAction(e -> {
                if (editContextMenu != null) {
                    Bounds bounds = btnEditMenu.localToScreen(btnEditMenu.getBoundsInLocal());
                    editContextMenu.show(btnEditMenu, bounds.getCenterX(), bounds.getCenterY() + 25);
                }
            });
        }

        if (btnViewMenu != null) {
            btnViewMenu.setOnAction(e -> {
                if (viewContextMenu != null) {
                    Bounds bounds = btnViewMenu.localToScreen(btnViewMenu.getBoundsInLocal());
                    viewContextMenu.show(btnViewMenu, bounds.getCenterX(), bounds.getCenterY() + 25);
                }
            });
        }

        if (btnHelpMenu != null) {
            btnHelpMenu.setOnAction(e -> {
                if (helpContextMenu != null) {
                    Bounds bounds = btnHelpMenu.localToScreen(btnHelpMenu.getBoundsInLocal());
                    helpContextMenu.show(btnHelpMenu, bounds.getCenterX(), bounds.getCenterY() + 25);
                }
            });
        }

        System.out.println("DEBUG: setupCustomMenu() окончено");
    }

    private void createFileMenu() {
        fileContextMenu = new ContextMenu();

        MenuItem miOpenData = new MenuItem("Открыть базу...");
        miOpenData.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Открыть файл данных");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Файл базы (*.dat)", "*.dat"));
            javafx.stage.Window window = toysTable != null && toysTable.getScene() != null ? toysTable.getScene().getWindow() : null;
            File file = fc.showOpenDialog(window);
            if (file != null) {
                try {
                    java.nio.file.Files.copy(
                            file.toPath(),
                            new File("data" + File.separator + "toys.dat").toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );
                    database.buildIndexes();
                    refreshTable();
                    updateStatus("Загружен файл базы: " + file.getName());
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "База загружена:\n" + file.getAbsolutePath());
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть файл: " + ex.getMessage());
                }
            }
        });

        MenuItem miOpenBackup = new MenuItem("Загрузить бекап...");
        miOpenBackup.setOnAction(e -> handleLoadBackup());

        MenuItem miExportCsv = new MenuItem("Экспорт в CSV...");
        miExportCsv.setOnAction(e -> handleExport());

        MenuItem miExit = new MenuItem("Выход");
        miExit.setOnAction(e -> {
            try { if (database != null) database.createBackup(); } catch (Exception ignored) {}
            stopAutoBackupTimer();
            System.exit(0);
        });

        fileContextMenu.getItems().addAll(miOpenData, miOpenBackup, new SeparatorMenuItem(), miExportCsv, new SeparatorMenuItem(), miExit);
    }

    private void createEditMenu() {
        editContextMenu = new ContextMenu();

        MenuItem miAddToy = new MenuItem("Добавить запись...");
        miAddToy.setOnAction(e -> handleAddToy());

        MenuItem miDeleteToy = new MenuItem("Удалить запись/по критерию...");
        miDeleteToy.setOnAction(e -> handleDelete());

        MenuItem miBuildIndexes = new MenuItem("Перестроить индексы");
        miBuildIndexes.setOnAction(e -> handleBuildIndexes());

        MenuItem miFillTestData = new MenuItem("Заполнить тестовыми данными");
        miFillTestData.setOnAction(e -> handleFillDatabase());

        editContextMenu.getItems().addAll(miAddToy, miDeleteToy, new SeparatorMenuItem(), miBuildIndexes, miFillTestData);
    }

    private void createViewMenu() {
        viewContextMenu = new ContextMenu();

        CheckMenuItem miShowStatusHelp = new CheckMenuItem("Показывать подсказки в статусе");
        miShowStatusHelp.setSelected(true);

        MenuItem miSort = new MenuItem("Сортировка...");
        miSort.setOnAction(e -> handleSort());

        MenuItem miRefresh = new MenuItem("Обновить");
        miRefresh.setOnAction(e -> handleRefresh());

        viewContextMenu.getItems().addAll(miShowStatusHelp, miSort, miRefresh);
    }

    private void createHelpMenu() {
        helpContextMenu = new ContextMenu();

        MenuItem miAbout = new MenuItem("О программе");
        miAbout.setOnAction(e -> showAlert(Alert.AlertType.INFORMATION, "О программе",
                "Toy Management\nJavaFX-приложение с индексной БД, бекапами и CSV-экспортом."));

        MenuItem miHelp = new MenuItem("Справка по действиям");
        miHelp.setOnAction(e -> showAlert(Alert.AlertType.INFORMATION, "Справка",
                "Поиск: поставщик | дата dd.MM.yyyy | возраст min-max\n" +
                        "Экспорт: Файл → Экспорт в CSV\n" +
                        "Бекап: Файл → Загрузить бекап, авто-бекап включён."));

        helpContextMenu.getItems().addAll(miAbout, miHelp);
    }

    private void setupContextHelp() {
        if (helpLabel == null) return;

        Map<Control, String> tips = new LinkedHashMap<>();
        tips.put(searchField, "Введите: поставщик | дата (dd.MM.yyyy) | возраст (min-max)");
        tips.put(searchButton, "Найти записи по введённому запросу");
        tips.put(fillDbButton, "Заполнить БД тестовыми данными (перезапись)");
        tips.put(refreshButton, "Обновить таблицу из файла данных");
        tips.put(indexesButton, "Перестроить индексы по дате/поставщику/возрасту");
        tips.put(addButton, "Добавить новую запись об игрушке");
        tips.put(deleteButton, "Удалить выделенную или по критерию");
        tips.put(sortButton, "Показать диалог сортировки");
        tips.put(exportButton, "Экспорт текущих записей в CSV (папка exports)");
        tips.put(loadBackupButton, "Загрузить одну из резервных копий");
        tips.put(btnFileMenu, "Файловые операции");
        tips.put(btnEditMenu, "Редактирование данных");
        tips.put(btnViewMenu, "Вид и сортировка");
        tips.put(btnHelpMenu, "Помощь и информация");

        tips.forEach((control, msg) -> {
            if (control == null) return;
            control.setOnMouseEntered(ev -> helpLabel.setText(msg));
            control.setOnMouseExited(ev -> helpLabel.setText(""));
        });
    }

    // ====== ВСЕ ОСТАЛЬНЫЕ ОБРАБОТЧИКИ ======

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
                    return null;
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
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "✅ Удалено: " + selectedToy.getName());
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
        dialog.setTitle("Удаление по критериям");
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
        RadioButton ageRb = new RadioButton(" По возрастному диапазону");
        ageRb.setToggleGroup(group);

        TextField valueField = new TextField();
        valueField.setPromptText("Введите значение");

        vbox.getChildren().addAll(info, info2, new Separator(), supplierRb, dateRb, ageRb, new Label("Введите значение:"), valueField);

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

        vbox.getChildren().addAll(new Label("Выберите поле:"), dateRb, supplierRb, ageRb, new Separator(), ascendingCb);

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
                boolean ascending = ascendingCb.isSelected();

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
            showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка: " + e.getMessage());
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
            } catch (Exception ignored) {}

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
            if (!backups.isEmpty()) {
                backupCombo.setValue(backups.get(0));
            }

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
                    if (database.restoreFromBackup(selectedBackup)) {
                        refreshTable();
                        showAlert(Alert.AlertType.INFORMATION, "Успех", "✅ База загружена из бекапа:\n" + selectedBackup);
                        updateStatus("База загружена: " + selectedBackup);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Не удалось загрузить бекап!");
                    }
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ " + e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            List<Toy> toys = database.getAllToys();
            updateTable(toys);
            updateStatus("Всего записей: " + toys.size());
        } catch (IOException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "❌ Ошибка: " + e.getMessage());
        }
    }

    private void updateTable(List<Toy> toys) {
        ObservableList<Toy> data = FXCollections.observableArrayList(toys);
        toysTable.setItems(data);
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

    private void startAutoBackupTimer() {
        autoBackupTimer = new Timer("AutoBackupTimer", true);
        long BACKUP_INTERVAL = 5 * 60 * 1000;
        autoBackupTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                try {
                    if (database.createBackup()) {
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
        }
    }
}
