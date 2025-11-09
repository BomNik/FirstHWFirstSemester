package com.mipt.nikitabumagin.io;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.nikitabumagin.io.TextFileAnalyzer.AnalysisResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TextFileAnalyzerTest {

  // Для автоматического удаления тестовых файлов
  @TempDir
  Path tempDir;

  @Test
  void testAnalyzeFile() throws IOException {
    TextFileAnalyzer analyzer = new TextFileAnalyzer();

    Path testFile = tempDir.resolve("test.txt");
    List<String> lines = Arrays.asList("Hello world!", "This is a test.", "Java IO");
    Files.write(testFile, lines);

    AnalysisResult result = analyzer.analyzeFile(testFile.toString());

    assertEquals(3, result.lineCount(), "Количество строк должно быть 3");
    assertEquals(8, result.wordCount(), "Количество слов должно быть 8");
    assertEquals(34, result.charCount(), "Количество символов должно быть 34");
  }

  @Test
  void testAnalyzeFileWithEmptyString() throws IOException {
    TextFileAnalyzer analyzer = new TextFileAnalyzer();

    Path emptyStringFile = tempDir.resolve("empty.txt");
    List<String> emptyString = List.of("");
    Files.write(emptyStringFile, emptyString);

    TextFileAnalyzer.AnalysisResult result = analyzer.analyzeFile(emptyStringFile.toString());

    assertEquals(1, result.lineCount(), "Одна пустая строка");
    assertEquals(0, result.wordCount(), "Нет слов");
    assertEquals(0, result.charCount(), "Нет символов");
  }

  @Test
  void testAnalyzeFileWithEmptyFile() throws IOException {
    TextFileAnalyzer analyzer = new TextFileAnalyzer();

    Path emptyFile = tempDir.resolve("empty.txt");
    Files.createFile(emptyFile);

    TextFileAnalyzer.AnalysisResult result = analyzer.analyzeFile(emptyFile.toString());

    assertEquals(0, result.lineCount(), "Нет строк");
    assertEquals(0, result.wordCount(), "Нет слов");
    assertEquals(0, result.charCount(), "Нет символов");
  }

  @Test
  void testSaveAnalysisResult() throws IOException {
    TextFileAnalyzer analyzer = new TextFileAnalyzer();

    AnalysisResult result = new AnalysisResult(2, 5, 20);

    Path outputFile = tempDir.resolve("analysis.txt");
    analyzer.saveAnalysisResult(result, outputFile.toString());

    assertTrue(Files.exists(outputFile), "Файл должен быть создан");
    assertTrue(Files.size(outputFile) > 0, "Файл не должен быть пустым");

    List<String> savedLines = Files.readAllLines(outputFile);
    String content = String.join("\n", savedLines);

    assertTrue(content.contains("Количество строк: 2"));
    assertTrue(content.contains("Количество слов: 5"));
    assertTrue(content.contains("Количество символов: 20"));
  }
}