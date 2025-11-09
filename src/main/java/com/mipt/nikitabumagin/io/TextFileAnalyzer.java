package com.mipt.nikitabumagin.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TextFileAnalyzer {

  /**
   * Анализирует текстовый файл
   *
   * @param filePath путь к файлу
   * @return {@code AnalysisResult} результат анализа
   */
  public AnalysisResult analyzeFile(String filePath) {
    long lineCount = 0;
    long wordCount = 0;
    long charCount = 0;

    try (BufferedReader reader = new BufferedReader(
        new FileReader(filePath, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        ++lineCount;
        charCount += line.length();
        wordCount += line.trim().isEmpty() ? 0 : line.trim().split("\\s+").length;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new AnalysisResult(lineCount, wordCount, charCount);
  }

  /**
   * Записывает результаты анализа в текстовый файл
   *
   * @param result     результаты анализа какого-то текстового файла
   * @param outputPath путь для записи
   */
  public void saveAnalysisResult(AnalysisResult result, String outputPath) {
    try (BufferedWriter writer = new BufferedWriter(
        new FileWriter(outputPath, StandardCharsets.UTF_8))) {
      writer.write(String.format("Количество строк: %,d", result.lineCount()));
      writer.newLine();
      writer.write(String.format("Количество слов: %,d", result.wordCount()));
      writer.newLine();
      writer.write(String.format("Количество символов: %,d", result.charCount()));
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Класс для хранения некоторой статистики текстового файла
   *
   * @param lineCount количество строк в файле
   * @param wordCount количество слов в файле
   * @param charCount количество символов в файле
   */
  public record AnalysisResult(long lineCount, long wordCount, long charCount) {

    @Override
    public String toString() {
      return String.format(
          "AnalysisResult{lines=%d, words=%d, chars=%d}",
          lineCount, wordCount, charCount
      );
    }
  }
}