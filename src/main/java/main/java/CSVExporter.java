package main.java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter {

    /**
     * Экспортировать список игрушек в CSV файл
     *
     * @param toys     Список игрушек
     * @param filePath Путь до файла
     * @return успешно ли завершён экспорт
     */
    public static boolean exportToCSV(List<Toy> toys, String filePath) {
        try {
            File exportsDir = new File("exports");
            if (!exportsDir.exists()) {
                exportsDir.mkdir();
            }

            String fullPath = "exports" + File.separator + filePath;

            try (FileWriter writer = new FileWriter(fullPath)) {
                writer.append("Код;Название;Мин. возраст;Макс. возраст;Цена;Кол-во;Дата поступления;Поставщик\n");

                for (Toy toy : toys) {
                    writer.append(escapeCsv(toy.getCode())).append(";");
                    writer.append(escapeCsv(toy.getName())).append(";");
                    writer.append(String.valueOf(toy.getMinAge())).append(";");
                    writer.append(String.valueOf(toy.getMaxAge())).append(";");
                    writer.append(String.valueOf(toy.getPrice())).append(";");
                    writer.append(String.valueOf(toy.getQuantity())).append(";");
                    writer.append(toy.getArrivalDate().toString()).append(";");
                    writer.append(escapeCsv(toy.getSupplier())).append("\n");
                }

                System.out.println("✅ Экспорт завершён: " + fullPath);
                return true;
            }
        } catch (IOException e) {
            System.err.println("❌ Ошибка экспорта: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Экранировать текст для CSV (добавить кавычки если нужно)
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
