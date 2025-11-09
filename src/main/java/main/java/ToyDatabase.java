package main.java;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ToyDatabase {
    private static final String DATA_FILE = "toys.dat";
    private static final String INDEX_FILE = "toys.idx";
    private static final String BACKUP_DIR = "backups";
    private static final String DATA_DIR = "data";

    private TreeMap<LocalDate, List<Long>> dateIndex;
    private TreeMap<String, List<Long>> supplierIndex;
    private TreeMap<String, List<Long>> ageRangeIndex;
    private List<Long> recordPositions;

    public ToyDatabase() {
        dateIndex = new TreeMap<>();
        supplierIndex = new TreeMap<>();
        ageRangeIndex = new TreeMap<>();
        recordPositions = new ArrayList<>();

        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }

        loadIndexes();
    }

    private String getDataFilePath() {
        return DATA_DIR + File.separator + DATA_FILE;
    }

    private String getIndexFilePath() {
        return DATA_DIR + File.separator + INDEX_FILE;
    }

    private String getBackupDirPath() {
        return BACKUP_DIR;
    }

    private byte[] serializeToy(Toy toy) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(toy);
        }
        return baos.toByteArray();
    }

    private Toy deserializeToy(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Toy) ois.readObject();
        }
    }

    public boolean createBackup() {
        try {
            File dataFile = new File(getDataFilePath());
            if (!dataFile.exists()) {
                System.out.println("❌ Нечего бекапить - файл не существует");
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String backupFileName = "backup_" + now.format(formatter) + ".dat";
            String backupPath = getBackupDirPath() + File.separator + backupFileName;

            try (FileInputStream fis = new FileInputStream(dataFile);
                 FileOutputStream fos = new FileOutputStream(backupPath)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }

            System.out.println("✅ Бекап создан: " + backupFileName);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Ошибка создания бекапа: " + e.getMessage());
            return false;
        }
    }
    public List<String> getBackupList() {
        List<String> backups = new ArrayList<>();
        File backupDir = new File(getBackupDirPath());

        if (!backupDir.exists()) {
            return backups;
        }

        File[] files = backupDir.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".dat"));
        if (files != null) {
            Arrays.sort(files, (f1, f2) -> f2.getName().compareTo(f1.getName()));
            for (File file : files) {
                backups.add(file.getName());
            }
        }

        return backups;
    }

    public boolean restoreFromBackup(String backupFileName) {
        try {
            String backupPath = getBackupDirPath() + File.separator + backupFileName;
            File backupFile = new File(backupPath);

            if (!backupFile.exists()) {
                System.out.println("❌ Бекап не найден: " + backupFileName);
                return false;
            }

            File currentDataFile = new File(getDataFilePath());
            if (currentDataFile.exists()) {
                try (FileInputStream fis = new FileInputStream(currentDataFile);
                     FileOutputStream fos = new FileOutputStream(
                             getBackupDirPath() + File.separator + "pre_restore_backup.dat")) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
            }

            try (FileInputStream fis = new FileInputStream(backupFile);
                 FileOutputStream fos = new FileOutputStream(getDataFilePath())) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }

            buildIndexes();
            System.out.println("✅ Восстановлено из бекапа: " + backupFileName);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Ошибка восстановления: " + e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void autoLoadLatestBackup() throws IOException, ClassNotFoundException {
        List<String> backups = getBackupList();

        if (!backups.isEmpty()) {
            String latestBackup = backups.get(0);

            File currentDataFile = new File(getDataFilePath());

            if (!currentDataFile.exists()) {
                System.out.println("ℹ️ Текущих данных не найдено. Загружаю последний бекап...");
                restoreFromBackup(latestBackup);
                System.out.println("✅ Данные загружены из бекапа: " + latestBackup);
            } else {
                System.out.println("✅ Текущие данные найдены");
                loadIndexes();
            }
        } else {
            System.out.println("ℹ️ Бекапов не найдено. Запуск с пустой базой.");
        }
    }

    public void cleanOldBackups(int keepCount) {
        List<String> backups = getBackupList();

        if (backups.size() <= keepCount) {
            System.out.println("ℹ️ Старых бекапов нет. У вас " + backups.size() + " бекапов.");
            return;
        }

        int toDelete = backups.size() - keepCount;
        for (int i = 0; i < toDelete; i++) {
            String oldBackup = backups.get(backups.size() - 1 - i);
            File fileToDelete = new File(getBackupDirPath() + File.separator + oldBackup);
            if (fileToDelete.delete()) {
                System.out.println("✅ Удалён старый бекап: " + oldBackup);
            }
        }
    }

    public void addToy(Toy toy) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(getDataFilePath(), "rw")) {
            long position = raf.length();
            raf.seek(position);
            byte[] data = serializeToy(toy);
            raf.writeInt(data.length);
            raf.write(data);
            recordPositions.add(position);
            updateIndexes(toy, position);
            saveIndexes();
        }
    }

    public void fillWithTestData() throws IOException {
        clearDatabase();
        List<Toy> testToys = Arrays.asList(
                new Toy("T001", "Конструктор LEGO", 7, 12, 2500.0, 15,
                        LocalDate.of(2025, 1, 15), "ToyWorld"),
                new Toy("T002", "Кукла Барби", 3, 8, 1200.0, 25,
                        LocalDate.of(2025, 2, 20), "Mattel"),
                new Toy("T003", "Робот-трансформер", 8, 14, 3500.0, 10,
                        LocalDate.of(2025, 1, 10), "Hasbro"),
                new Toy("T004", "Мягкая игрушка", 0, 5, 800.0, 30,
                        LocalDate.of(2025, 3, 5), "ToyWorld"),
                new Toy("T005", "Настольная игра", 10, 15, 1800.0, 20,
                        LocalDate.of(2025, 2, 20), "GameFactory"),
                new Toy("T006", "Машинка Hot Wheels", 5, 10, 450.0, 50,
                        LocalDate.of(2025, 1, 10), "Mattel"),
                new Toy("T007", "Пазл 1000 элементов", 12, 99, 950.0, 18,
                        LocalDate.of(2025, 3, 1), "Ravensburger"),
                new Toy("T008", "Детский велосипед", 4, 8, 8500.0, 5,
                        LocalDate.of(2025, 2, 15), "SportKids"),
                new Toy("T009", "Электронная игра", 8, 14, 4200.0, 12,
                        LocalDate.of(2025, 1, 15), "GameFactory"),
                new Toy("T010", "Набор для творчества", 6, 12, 1500.0, 22,
                        LocalDate.of(2025, 3, 5), "ArtKids")
        );

        for (Toy toy : testToys) {
            addToy(toy);
        }

        createBackup();
        System.out.println("✅ База заполнена " + testToys.size() + " тестовыми записями.");
    }

    public List<Toy> getAllToys() throws IOException, ClassNotFoundException {
        List<Toy> toys = new ArrayList<>();
        File file = new File(getDataFilePath());
        if (!file.exists()) {
            return toys;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            while (raf.getFilePointer() < raf.length()) {
                int length = raf.readInt();
                byte[] data = new byte[length];
                raf.read(data);
                Toy toy = deserializeToy(data);
                if (toy != null) {
                    toys.add(toy);
                }
            }
        }
        return toys;
    }

    private void updateIndexes(Toy toy, long position) {
        dateIndex.computeIfAbsent(toy.getArrivalDate(), k -> new ArrayList<>()).add(position);
        supplierIndex.computeIfAbsent(toy.getSupplier(), k -> new ArrayList<>()).add(position);
        ageRangeIndex.computeIfAbsent(toy.getAgeRange(), k -> new ArrayList<>()).add(position);
    }

    public boolean deleteToy(Toy toyToDelete) throws IOException, ClassNotFoundException {
        Long positionToDelete = null;

        try (RandomAccessFile raf = new RandomAccessFile(getDataFilePath(), "r")) {
            while (raf.getFilePointer() < raf.length()) {
                long currentPos = raf.getFilePointer();
                int length = raf.readInt();
                byte[] data = new byte[length];
                raf.read(data);

                Toy toy = deserializeToy(data);
                if (toy != null && toy.getCode().equals(toyToDelete.getCode())) {
                    positionToDelete = currentPos;
                    break;
                }
            }
        }

        if (positionToDelete != null) {
            int deleted = deleteToysAtPositions(Arrays.asList(positionToDelete));
            buildIndexes();
            System.out.println("✅ Удалено: " + toyToDelete.getName());
            return deleted > 0;
        }

        return false;
    }

    public int deleteByCode(String code) throws IOException, ClassNotFoundException {
        Long positionToDelete = null;

        try (RandomAccessFile raf = new RandomAccessFile(getDataFilePath(), "r")) {
            while (raf.getFilePointer() < raf.length()) {
                long currentPos = raf.getFilePointer();
                int length = raf.readInt();
                byte[] data = new byte[length];
                raf.read(data);

                Toy toy = deserializeToy(data);
                if (toy != null && toy.getCode().equals(code)) {
                    positionToDelete = currentPos;
                    break;
                }
            }
        }

        if (positionToDelete != null) {
            int deleted = deleteToysAtPositions(Arrays.asList(positionToDelete));
            buildIndexes();
            System.out.println("✅ Удалено по коду: " + code);
            return deleted;
        }

        return 0;
    }

    public int deleteBySupplier(String supplier) throws IOException, ClassNotFoundException {
        List<Long> positions = supplierIndex.get(supplier);
        if (positions == null || positions.isEmpty()) {
            System.out.println("❌ Не найдено игрушек от: " + supplier);
            return 0;
        }

        int deleted = deleteToysAtPositions(new ArrayList<>(positions));
        buildIndexes();
        System.out.println("✅ Удалено " + deleted + " игрушек от: " + supplier);
        return deleted;
    }

    public int deleteByDate(LocalDate date) throws IOException, ClassNotFoundException {
        List<Long> positions = dateIndex.get(date);
        if (positions == null || positions.isEmpty()) {
            System.out.println("❌ Не найдено игрушек с датой: " + date);
            return 0;
        }

        int deleted = deleteToysAtPositions(new ArrayList<>(positions));
        buildIndexes();
        System.out.println("✅ Удалено " + deleted + " игрушек с датой: " + date);
        return deleted;
    }

    public int deleteByAgeRange(String ageRange) throws IOException, ClassNotFoundException {
        List<Long> positions = ageRangeIndex.get(ageRange);
        if (positions == null || positions.isEmpty()) {
            System.out.println("❌ Не найдено игрушек для возраста: " + ageRange);
            return 0;
        }

        int deleted = deleteToysAtPositions(new ArrayList<>(positions));
        buildIndexes();
        System.out.println("✅ Удалено " + deleted + " игрушек для возраста: " + ageRange);
        return deleted;
    }

    private int deleteToysAtPositions(List<Long> positions) throws IOException, ClassNotFoundException {
        if (positions == null || positions.isEmpty()) {
            return 0;
        }

        Set<Long> positionsToDelete = new HashSet<>(positions);
        File tempFile = new File(DATA_DIR + File.separator + "toys_temp.dat");
        int deletedCount = 0;

        try (RandomAccessFile oldRaf = new RandomAccessFile(getDataFilePath(), "r");
             RandomAccessFile newRaf = new RandomAccessFile(tempFile, "rw")) {

            while (oldRaf.getFilePointer() < oldRaf.length()) {
                long currentPos = oldRaf.getFilePointer();
                int length = oldRaf.readInt();
                byte[] data = new byte[length];
                oldRaf.read(data);

                if (!positionsToDelete.contains(currentPos)) {
                    newRaf.writeInt(length);
                    newRaf.write(data);
                } else {
                    deletedCount++;
                }
            }
        }

        File oldFile = new File(getDataFilePath());
        oldFile.delete();
        tempFile.renameTo(oldFile);

        return deletedCount;
    }

    public void buildIndexes() throws IOException, ClassNotFoundException {
        dateIndex.clear();
        supplierIndex.clear();
        ageRangeIndex.clear();
        recordPositions.clear();

        File file = new File(getDataFilePath());
        if (!file.exists()) {
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            while (raf.getFilePointer() < raf.length()) {
                long position = raf.getFilePointer();
                int length = raf.readInt();
                byte[] data = new byte[length];
                raf.read(data);
                Toy toy = deserializeToy(data);
                if (toy != null) {
                    recordPositions.add(position);
                    updateIndexes(toy, position);
                }
            }
        }
        saveIndexes();
        System.out.println("✅ Индексы построены успешно.");
    }

    public List<Toy> getToysByDate(boolean ascending) throws IOException, ClassNotFoundException {
        List<Toy> result = new ArrayList<>();
        NavigableMap<LocalDate, List<Long>> map = ascending ? dateIndex : dateIndex.descendingMap();
        for (List<Long> positions : map.values()) {
            for (Long pos : positions) {
                result.add(readToyAtPosition(pos));
            }
        }
        return result;
    }

    public List<Toy> getToysBySupplier(boolean ascending) throws IOException, ClassNotFoundException {
        List<Toy> result = new ArrayList<>();
        NavigableMap<String, List<Long>> map = ascending ? supplierIndex : supplierIndex.descendingMap();
        for (List<Long> positions : map.values()) {
            for (Long pos : positions) {
                result.add(readToyAtPosition(pos));
            }
        }
        return result;
    }

    public List<Toy> getToysByAgeRange(boolean ascending) throws IOException, ClassNotFoundException {
        List<Toy> result = new ArrayList<>();
        NavigableMap<String, List<Long>> map = ascending ? ageRangeIndex : ageRangeIndex.descendingMap();
        for (List<Long> positions : map.values()) {
            for (Long pos : positions) {
                result.add(readToyAtPosition(pos));
            }
        }
        return result;
    }

    public List<Toy> findByDate(LocalDate date) throws IOException, ClassNotFoundException {
        return findToysAtPositions(dateIndex.get(date));
    }

    public List<Toy> findBySupplier(String supplier) throws IOException, ClassNotFoundException {
        return findToysAtPositions(supplierIndex.get(supplier));
    }

    public List<Toy> findByAgeRange(String ageRange) throws IOException, ClassNotFoundException {
        return findToysAtPositions(ageRangeIndex.get(ageRange));
    }

    private Toy readToyAtPosition(long position) throws IOException, ClassNotFoundException {
        try (RandomAccessFile raf = new RandomAccessFile(getDataFilePath(), "r")) {
            raf.seek(position);
            int length = raf.readInt();
            byte[] data = new byte[length];
            raf.read(data);
            return deserializeToy(data);
        }
    }

    private List<Toy> findToysAtPositions(List<Long> positions) throws IOException, ClassNotFoundException {
        if (positions == null) {
            return new ArrayList<>();
        }

        List<Toy> result = new ArrayList<>();
        for (Long pos : positions) {
            result.add(readToyAtPosition(pos));
        }
        return result;
    }

    public void saveIndexes() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(getIndexFilePath()))) {
            oos.writeObject(dateIndex);
            oos.writeObject(supplierIndex);
            oos.writeObject(ageRangeIndex);
            oos.writeObject(recordPositions);
        } catch (IOException e) {
            System.err.println("❌ Ошибка сохранения индексов: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadIndexes() {
        File indexFile = new File(getIndexFilePath());
        if (!indexFile.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(getIndexFilePath()))) {
            dateIndex = (TreeMap<LocalDate, List<Long>>) ois.readObject();
            supplierIndex = (TreeMap<String, List<Long>>) ois.readObject();
            ageRangeIndex = (TreeMap<String, List<Long>>) ois.readObject();
            recordPositions = (List<Long>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Ошибка загрузки индексов: " + e.getMessage());
        }
    }

    private void clearDatabase() {
        new File(getDataFilePath()).delete();
        new File(getIndexFilePath()).delete();
        dateIndex.clear();
        supplierIndex.clear();
        ageRangeIndex.clear();
        recordPositions.clear();
    }
}
