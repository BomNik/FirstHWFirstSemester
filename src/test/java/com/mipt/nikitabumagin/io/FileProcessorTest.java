package com.mipt.nikitabumagin.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileProcessorTest {

  @TempDir
  Path tempDir;

  @Test
  void testSplitAndMergeFile() throws IOException {
    FileProcessor processor = new FileProcessor();

    // Создать тестовый файл
    Path testFile = tempDir.resolve("test.dat");
    byte[] testData = new byte[1500]; // 1.5KB данных
    new Random().nextBytes(testData);
    Files.write(testFile, testData);

    // Разбить на части по 500 байт
    Path outputDir = tempDir.resolve("parts");
    List<Path> parts = processor.splitFile(testFile.toString(), outputDir.toString(), 500);

    assertEquals(3, parts.size(), "Должно быть создано 3 части");
    assertEquals(500, Files.size(parts.get(0)), "Первая часть 500 байт");
    assertEquals(500, Files.size(parts.get(1)), "Вторая часть 500 байт");
    assertEquals(500, Files.size(parts.get(2)), "Третья часть 500 байт");
    assertTrue(parts.get(0).getFileName().toString().endsWith(".part1"));
    assertTrue(parts.get(1).getFileName().toString().endsWith(".part2"));
    assertTrue(parts.get(2).getFileName().toString().endsWith(".part3"));

    // Объединить обратно
    Path mergedFile = tempDir.resolve("merged.dat");
    processor.mergeFiles(parts, mergedFile.toString());

    // Проверить что исходный и объединенный файлы идентичны
    assertArrayEquals(Files.readAllBytes(testFile), Files.readAllBytes(mergedFile));
  }

  @Test
  void testSplitFileWithNonmultiplePart() throws IOException {
    FileProcessor processor = new FileProcessor();

    // Файл не кратный размеру части
    Path testFile = tempDir.resolve("nonmultiple.dat");
    byte[] testData = new byte[1200]; // 1.2KB
    new Random().nextBytes(testData);
    Files.write(testFile, testData);

    Path outputDir = tempDir.resolve("nonmultiple");
    List<Path> parts = processor.splitFile(testFile.toString(), outputDir.toString(), 500);

    assertEquals(3, parts.size());
    assertEquals(500, Files.size(parts.get(0)));
    assertEquals(500, Files.size(parts.get(1)));
    assertEquals(200, Files.size(parts.get(2)));

    // Объединить обратно
    Path mergedFile = tempDir.resolve("merged.dat");
    processor.mergeFiles(parts, mergedFile.toString());

    // Проверить что исходный и объединенный файлы идентичны
    assertArrayEquals(Files.readAllBytes(testFile), Files.readAllBytes(mergedFile));
  }

  @Test
  void testSplitFileWithEmptyFile() throws IOException {
    FileProcessor processor = new FileProcessor();

    Path emptyFile = tempDir.resolve("empty.dat");
    Files.createFile(emptyFile); // создаем пустой файл

    Path outputDir = tempDir.resolve("empty");
    List<Path> parts = processor.splitFile(emptyFile.toString(), outputDir.toString(), 500);

    // Пустой файл должен создать 0 частей
    assertEquals(0, parts.size());

    // Объединить обратно
    Path mergedFile = tempDir.resolve("merged.dat");
    processor.mergeFiles(parts, mergedFile.toString());

    // Проверить что исходный и объединенный файлы идентичны
    assertArrayEquals(Files.readAllBytes(emptyFile), Files.readAllBytes(mergedFile));
  }

  @Test
  void testSplitFileSourceNotExists() {
    FileProcessor processor = new FileProcessor();

    Path outputDir = tempDir.resolve("nonexistent_parts");

    // Должно выбросить исключение
    assertThrows(IOException.class,
        () -> processor.splitFile("/nonexistent/file.dat", outputDir.toString(), 500));
  }

  @Test
  void testMergeFilesWithMissingPart() throws IOException {
    FileProcessor processor = new FileProcessor();

    Path part1 = tempDir.resolve("test.part1");
    Path part2 = tempDir.resolve("test.part2");
    Files.write(part1, new byte[100]);
    // part2 отсутствует

    List<Path> parts = List.of(part1, part2);
    Path mergedFile = tempDir.resolve("merged.dat");

    // Должно выбросить исключение
    assertThrows(IOException.class, () -> processor.mergeFiles(parts, mergedFile.toString()));
  }

  @Test
  void testMergeFilesInCorrectOrder() throws IOException {
    FileProcessor processor = new FileProcessor();

    Path part1 = tempDir.resolve("test.part1");
    Path part2 = tempDir.resolve("test.part2");
    Path part3 = tempDir.resolve("test.part3");

    Files.write(part1, new byte[]{1, 2, 3});
    Files.write(part2, new byte[]{4, 5, 6});
    Files.write(part3, new byte[]{7, 8, 9});

    List<Path> parts = List.of(part1, part2, part3);
    Path mergedFile = tempDir.resolve("merged_order.dat");

    processor.mergeFiles(parts, mergedFile.toString());

    byte[] mergedData = Files.readAllBytes(mergedFile);
    assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, mergedData);
  }

  @Test
  void testSplitAndMergeWithTextFile() throws IOException {
    FileProcessor processor = new FileProcessor();

    Path textFile = tempDir.resolve("text.txt");
    String textContent =
        """
                БОБРОВОЕ Суперлуние смогут увидеть россияне уже послезавтра — оно станет самым ярким и крупным полнолунием в 2025 году.
            
                Полнолуние наступит 5 ноября в 16:20 по Москве, а уже в 1:30 Луна подойдёт на минимальное расстояние к Земле.
            
                Готовим фотоаппараты.
                """;
    Files.writeString(textFile, textContent);

    // Разбиваем на части по 30 байт
    Path outputDir = tempDir.resolve("text_parts");
    List<Path> parts = processor.splitFile(textFile.toString(), outputDir.toString(), 30);

    // Объединить обратно
    Path mergedFile = tempDir.resolve("merged_text.txt");
    processor.mergeFiles(parts, mergedFile.toString());

    // Проверить что исходный и объединенный файлы идентичны
    String mergedContent = Files.readString(mergedFile);
    assertEquals(textContent, mergedContent);
  }
}