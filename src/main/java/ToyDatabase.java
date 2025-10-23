import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
public class ToyDatabase {
    private static final String DATA_FILE = "toys.dat";
    private static final String INDEX_FILE = "toys.idx";

    private TreeMap<LocalDate, List<Long>> dateIndex;
    private TreeMap<String, List<Long>> supplierIndex;
    private TreeMap<String, List<Long>> ageRangeIndex;
    private List<Long> recordPositions;

    public ToyDatabase() {
        dateIndex = new TreeMap<>();
        supplierIndex = new TreeMap<>();
        ageRangeIndex = new TreeMap<>();
        recordPositions = new ArrayList<>();
        loadIndexes();
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

    public void addToy(Toy toy) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(DATA_FILE, "rw")) {
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

        System.out.println("База заполнена " + testToys.size() + " тестовыми записями.");
    }

    public List<Toy> getAllToys() throws IOException, ClassNotFoundException {
        List<Toy> toys = new ArrayList<>();
        File file = new File(DATA_FILE);

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

    public void buildIndexes() throws IOException, ClassNotFoundException {
        dateIndex.clear();
        supplierIndex.clear();
        ageRangeIndex.clear();
        recordPositions.clear();

        File file = new File(DATA_FILE);
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
        System.out.println("Индексы построены успешно.");
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

    public List<Toy> findByDateGreater(LocalDate date) throws IOException, ClassNotFoundException {
        return dateIndex.tailMap(date, false).values().stream()
                .flatMap(List::stream)
                .map(pos -> {
                    try {
                        return readToyAtPosition(pos);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Toy> findByDateLess(LocalDate date) throws IOException, ClassNotFoundException {
        return dateIndex.headMap(date, false).values().stream()
                .flatMap(List::stream)
                .map(pos -> {
                    try {
                        return readToyAtPosition(pos);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Toy> findBySupplier(String supplier) throws IOException, ClassNotFoundException {
        return findToysAtPositions(supplierIndex.get(supplier));
    }

    public List<Toy> findByAgeRange(String ageRange) throws IOException, ClassNotFoundException {
        return findToysAtPositions(ageRangeIndex.get(ageRange));
    }

    public int deleteByDate(LocalDate date) throws IOException, ClassNotFoundException {
        List<Long> positions = dateIndex.get(date);
        if (positions == null) {
            return 0;
        }

        int count = deleteToysAtPositions(new ArrayList<>(positions));
        buildIndexes();
        return count;
    }

    public int deleteBySupplier(String supplier) throws IOException, ClassNotFoundException {
        List<Long> positions = supplierIndex.get(supplier);
        if (positions == null) {
            return 0;
        }

        int count = deleteToysAtPositions(new ArrayList<>(positions));
        buildIndexes();
        return count;
    }

    public int deleteByAgeRange(String ageRange) throws IOException, ClassNotFoundException {
        List<Long> positions = ageRangeIndex.get(ageRange);
        if (positions == null) {
            return 0;
        }

        int count = deleteToysAtPositions(new ArrayList<>(positions));
        buildIndexes();
        return count;
    }

    private Toy readToyAtPosition(long position) throws IOException, ClassNotFoundException {
        try (RandomAccessFile raf = new RandomAccessFile(DATA_FILE, "r")) {
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

    private int deleteToysAtPositions(List<Long> positions) throws IOException, ClassNotFoundException {
        if (positions == null || positions.isEmpty()) {
            return 0;
        }

        Set<Long> positionsToDelete = new HashSet<>(positions);
        File tempFile = new File("toys_temp.dat");
        int deletedCount = 0;

        try (RandomAccessFile oldRaf = new RandomAccessFile(DATA_FILE, "r");
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

        File oldFile = new File(DATA_FILE);
        oldFile.delete();
        tempFile.renameTo(oldFile);

        return deletedCount;
    }

    private void saveIndexes() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(INDEX_FILE))) {
            oos.writeObject(dateIndex);
            oos.writeObject(supplierIndex);
            oos.writeObject(ageRangeIndex);
            oos.writeObject(recordPositions);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения индексов: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadIndexes() {
        File indexFile = new File(INDEX_FILE);
        if (!indexFile.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(INDEX_FILE))) {
            dateIndex = (TreeMap<LocalDate, List<Long>>) ois.readObject();
            supplierIndex = (TreeMap<String, List<Long>>) ois.readObject();
            ageRangeIndex = (TreeMap<String, List<Long>>) ois.readObject();
            recordPositions = (List<Long>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка загрузки индексов: " + e.getMessage());
        }
    }

    private void clearDatabase() {
        new File(DATA_FILE).delete();
        new File(INDEX_FILE).delete();
        dateIndex.clear();
        supplierIndex.clear();
        ageRangeIndex.clear();
        recordPositions.clear();
    }
}
