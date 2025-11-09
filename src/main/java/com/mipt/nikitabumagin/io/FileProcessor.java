package com.mipt.nikitabumagin.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileProcessor {

  /**
   * Разбивает файл на части указанного размера
   *
   * @param sourcePath путь к исходному файлу
   * @param outputDir  директория для сохранения частей
   * @param partSize   размер каждой части в байтах
   * @return список путей к созданным частям
   * @throws IOException если исходный файл не существует или не удастся записать по пути
   */
  public List<Path> splitFile(String sourcePath, String outputDir, int partSize)
      throws IOException {
    List<Path> partPaths = new ArrayList<>();

    Path sourceFile = Paths.get(sourcePath);
    if (!Files.exists(sourceFile)) {
      throw new IOException("Исходный файл не существует: " + sourcePath);
    }

    Path outputDirectory = Paths.get(outputDir);
    if (!Files.exists(outputDirectory)) {
      Files.createDirectories(outputDirectory);
    }

    String fileName = sourceFile.getFileName().toString();
    String baseName = fileName.contains(".")
        ? fileName.substring(0, fileName.lastIndexOf('.'))
        : fileName;

    try (FileChannel sourceChannel = FileChannel.open(sourceFile, StandardOpenOption.READ)) {
      long fileSize = sourceChannel.size();
      long bytesProcessed = 0;
      int currentPartNumber = 1;

      ByteBuffer buffer = ByteBuffer.allocate(partSize);

      while (bytesProcessed < fileSize) {
        String partFileName = String.format("%s.part%d", baseName, currentPartNumber);
        Path partPath = outputDirectory.resolve(partFileName);

        try (FileChannel partChannel = FileChannel.open(partPath,
            StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
          buffer.clear();
          int bytesRead = sourceChannel.read(buffer);
          if (bytesRead == -1) {
            break;
          }
          buffer.flip();
          partChannel.write(buffer);

          bytesProcessed += bytesRead;
          partPaths.add(partPath);
          ++currentPartNumber;
        }
      }
    }

    return partPaths;
  }

  /**
   * Объединяет части файла обратно в один файл
   *
   * @param partPaths  список путей к частям файла (в правильном порядке)
   * @param outputPath путь для результирующего файла
   */
  public void mergeFiles(List<Path> partPaths, String outputPath) throws IOException {
    for (Path part : partPaths) {
      if (!Files.exists(part)) {
        throw new IOException("Часть файла не существует: " + part);
      }
    }

    Path outputFile = Paths.get(outputPath);

    try (FileChannel outputChannel = FileChannel.open(outputFile,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

      ByteBuffer buffer = ByteBuffer.allocate(8192);

      for (Path partPath : partPaths) {

        try (FileChannel partChannel = FileChannel.open(partPath, StandardOpenOption.READ)) {
          buffer.clear();

          while (partChannel.read(buffer) > 0) {
            buffer.flip();
            outputChannel.write(buffer);
            buffer.clear();
          }
        }
      }

      outputChannel.force(true);
    }
  }
}
