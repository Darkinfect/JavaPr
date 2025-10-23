import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static ToyDatabase database;
    private static Scanner scanner;

    public static void main(String[] args) {
        database = new ToyDatabase();
        scanner = new Scanner(System.in);

        if (args.length > 0) {
            processCommand(args);
            return;
        }

        interactiveMode();
    }

    private static void processCommand(String[] args) {
        try {
            String command = args[0].toLowerCase();

            switch (command) {
                case "fill":
                    database.fillWithTestData();
                    break;
                case "list":
                    listAllToys();
                    break;
                case "build-index":
                    database.buildIndexes();
                    break;
                default:
                    System.out.println("Неизвестная команда: " + command);
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void interactiveMode() {
        System.out.println("=== СИСТЕМА УПРАВЛЕНИЯ БАЗОЙ ИГРУШЕК ===");

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        database.fillWithTestData();
                        break;
                    case "2":
                        listAllToys();
                        break;
                    case "3":
                        database.buildIndexes();
                        break;
                    case "4":
                        sortByIndex();
                        break;
                    case "5":
                        searchByIndex();
                        break;
                    case "6":
                        deleteByIndex();
                        break;
                    case "7":
                        addToy();
                        break;
                    case "0":
                        System.out.println("Выход из программы.");
                        return;
                    default:
                        System.out.println("Неверный выбор. Попробуйте снова.");
                }
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }

            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("\n--- МЕНЮ ---");
        System.out.println("1. Заполнить базу тестовыми данными");
        System.out.println("2. Показать все игрушки");
        System.out.println("3. Построить индексы");
        System.out.println("4. Вывести с сортировкой по индексу");
        System.out.println("5. Поиск по индексу");
        System.out.println("6. Удаление по индексу");
        System.out.println("7. Добавить игрушку");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
    }

    private static void listAllToys() throws IOException, ClassNotFoundException {
        List<Toy> toys = database.getAllToys();

        if (toys.isEmpty()) {
            System.out.println("База пустая.");
            return;
        }

        System.out.println("\n=== ВСЕ ИГРУШКИ (" + toys.size() + ") ===");
        for (int i = 0; i < toys.size(); i++) {
            System.out.println((i + 1) + ". " + toys.get(i));
        }
    }

    private static void sortByIndex() throws IOException, ClassNotFoundException {
        System.out.println("\nВыберите индекс для сортировки:");
        System.out.println("1. Дата поступления");
        System.out.println("2. Поставщик");
        System.out.println("3. Возрастной диапазон");
        System.out.print("Выбор: ");

        String indexChoice = scanner.nextLine().trim();

        System.out.print("Порядок сортировки (1 - по возрастанию, 2 - по убыванию): ");
        String orderChoice = scanner.nextLine().trim();
        boolean ascending = orderChoice.equals("1");

        List<Toy> toys = null;
        String indexName = "";

        switch (indexChoice) {
            case "1":
                toys = database.getToysByDate(ascending);
                indexName = "дате поступления";
                break;
            case "2":
                toys = database.getToysBySupplier(ascending);
                indexName = "поставщику";
                break;
            case "3":
                toys = database.getToysByAgeRange(ascending);
                indexName = "возрастному диапазону";
                break;
            default:
                System.out.println("Неверный выбор индекса.");
                return;
        }

        if (toys.isEmpty()) {
            System.out.println("Нет данных.");
            return;
        }

        System.out.println("\n=== СОРТИРОВКА ПО " + indexName.toUpperCase() +
                (ascending ? " (ВОЗРАСТАНИЕ)" : " (УБЫВАНИЕ)") + " ===");
        for (int i = 0; i < toys.size(); i++) {
            System.out.println((i + 1) + ". " + toys.get(i));
        }
    }

    private static void searchByIndex() throws IOException, ClassNotFoundException {
        System.out.println("\nВыберите индекс для поиска:");
        System.out.println("1. Дата поступления");
        System.out.println("2. Поставщик");
        System.out.println("3. Возрастной диапазон");
        System.out.print("Выбор: ");

        String indexChoice = scanner.nextLine().trim();

        System.out.println("Тип поиска:");
        System.out.println("1. Равно значению");
        System.out.println("2. Больше значения");
        System.out.println("3. Меньше значения");
        System.out.print("Выбор: ");

        String searchType = scanner.nextLine().trim();

        List<Toy> toys = null;

        switch (indexChoice) {
            case "1":
                toys = searchByDate(searchType);
                break;
            case "2":
                toys = searchBySupplier(searchType);
                break;
            case "3":
                toys = searchByAgeRange(searchType);
                break;
            default:
                System.out.println("Неверный выбор индекса.");
                return;
        }

        if (toys == null || toys.isEmpty()) {
            System.out.println("Игрушки не найдены.");
            return;
        }

        System.out.println("\n=== РЕЗУЛЬТАТЫ ПОИСКА (" + toys.size() + ") ===");
        for (int i = 0; i < toys.size(); i++) {
            System.out.println((i + 1) + ". " + toys.get(i));
        }
    }

    private static List<Toy> searchByDate(String searchType) throws IOException, ClassNotFoundException {
        System.out.print("Введите дату (дд.мм.гггг): ");
        String dateStr = scanner.nextLine().trim();

        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);

            switch (searchType) {
                case "1":
                    return database.findByDate(date);
                case "2":
                    return database.findByDateGreater(date);
                case "3":
                    return database.findByDateLess(date);
                default:
                    System.out.println("Неверный тип поиска.");
                    return null;
            }
        } catch (DateTimeParseException e) {
            System.out.println("Ошибка формата даты.");
            return null;
        }
    }

    private static List<Toy> searchBySupplier(String searchType) throws IOException, ClassNotFoundException {
        if (!searchType.equals("1")) {
            System.out.println("Для поставщика доступен только точный поиск.");
            return null;
        }

        System.out.print("Введите название поставщика: ");
        String supplier = scanner.nextLine().trim();

        return database.findBySupplier(supplier);
    }

    private static List<Toy> searchByAgeRange(String searchType) throws IOException, ClassNotFoundException {
        if (!searchType.equals("1")) {
            System.out.println("Для возрастного диапазона доступен только точный поиск.");
            return null;
        }

        System.out.print("Введите возрастной диапазон (например, 7-12): ");
        String ageRange = scanner.nextLine().trim();

        return database.findByAgeRange(ageRange);
    }

    private static void deleteByIndex() throws IOException, ClassNotFoundException {
        System.out.println("\nВыберите индекс для удаления:");
        System.out.println("1. Дата поступления");
        System.out.println("2. Поставщик");
        System.out.println("3. Возрастной диапазон");
        System.out.print("Выбор: ");

        String indexChoice = scanner.nextLine().trim();
        int deletedCount = 0;

        switch (indexChoice) {
            case "1":
                System.out.print("Введите дату (дд.мм.гггг): ");
                String dateStr = scanner.nextLine().trim();
                try {
                    LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
                    deletedCount = database.deleteByDate(date);
                } catch (DateTimeParseException e) {
                    System.out.println("Ошибка формата даты.");
                    return;
                }
                break;
            case "2":
                System.out.print("Введите поставщика: ");
                String supplier = scanner.nextLine().trim();
                deletedCount = database.deleteBySupplier(supplier);
                break;
            case "3":
                System.out.print("Введите возрастной диапазон (например, 7-12): ");
                String ageRange = scanner.nextLine().trim();
                deletedCount = database.deleteByAgeRange(ageRange);
                break;
            default:
                System.out.println("Неверный выбор индекса.");
                return;
        }

        System.out.println("Удалено записей: " + deletedCount);
    }

    private static void addToy() throws IOException {
        System.out.println("\n=== ДОБАВЛЕНИЕ ИГРУШКИ ===");

        try {
            System.out.print("Код игрушки: ");
            String code = scanner.nextLine().trim();

            System.out.print("Название: ");
            String name = scanner.nextLine().trim();

            System.out.print("Минимальный возраст: ");
            int minAge = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Максимальный возраст: ");
            int maxAge = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Цена: ");
            double price = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Количество: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Дата поступления (дд.мм.гггг): ");
            String dateStr = scanner.nextLine().trim();
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);

            System.out.print("Поставщик: ");
            String supplier = scanner.nextLine().trim();

            Toy toy = new Toy(code, name, minAge, maxAge, price, quantity, date, supplier);
            database.addToy(toy);

            System.out.println("Игрушка добавлена успешно.");

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: неверный формат числа.");
        } catch (DateTimeParseException e) {
            System.out.println("Ошибка: неверный формат даты.");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации: " + e.getMessage());
        }
    }
}
