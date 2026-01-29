package org.commonprovenance.framework.store.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;

import reactor.core.publisher.Mono;

public class FileUtils {
  private final static Path TEST_RESOURCES_DIR = Path
      .of("src" + File.separator + "test" + File.separator + "resources" + File.separator);

  private final static Path INTEGRATION_TEST_RESOURCES_DIR = Path
      .of("src" + File.separator + "integrationTest" + File.separator + "resources" + File.separator);

  public static Mono<Path> exists(Path filePath) {
    try {
      return Files.exists(filePath)
          ? Mono.just(filePath)
          : Mono.error(new NotFoundException("File not exists: " + filePath.toString()));
    } catch (Exception exception) {
      return Mono.error(new InternalApplicationException("FileUtils::exists failed!", exception));
    }
  }

  public static Mono<Path> isFile(Path filePath) {
    try {
      return Files.isRegularFile(filePath)
          ? Mono.just(filePath)
          : Mono.error(new NotFoundException("File does not exists, or is not regular file: " + filePath.toString()));
    } catch (Exception exception) {
      return Mono.error(new InternalApplicationException("FileUtils::isFile failed!", exception));
    }
  }

  private static Mono<String> fileString(Path filePath, Charset charset) {
    try {
      return Mono.just(Files.readString(filePath, charset));
    } catch (IOException ioException) {
      return Mono.error(new InternalApplicationException(
          "I/O error occurs, or a malformed or unmappable byte sequence is read: " + filePath.toString(),
          ioException));
    } catch (OutOfMemoryError ioException) {
      return Mono
          .error(new InternalApplicationException(
              "File is extremely large, for example larger than 2GB: " + filePath.toString(),
              ioException));
    } catch (Exception exception) {
      return Mono
          .error(new InternalApplicationException(
              "FileUtils::readString failed: " + filePath.toString(),
              exception));
    }
  }

  public static Mono<String> readFileString(Path filePath, Charset charset) {
    return FileUtils.exists(filePath)
        .flatMap(FileUtils::isFile)
        .flatMap(path -> FileUtils.fileString(path, charset));
  }

  public static Mono<String> readFileString(Path filePath) {
    return FileUtils.readFileString(filePath, StandardCharsets.UTF_8);
  }

  private static Mono<InputStream> fileStream(Path filePath) {
    try {
      return Mono
          .just(Files.newInputStream(filePath));
    } catch (IOException ioException) {
      return Mono
          .error(new InternalApplicationException("I/O error occurs: " + filePath.toString(), ioException));
    } catch (Exception exception) {
      return Mono
          .error(new InternalApplicationException("FileUtils::fileStream failed: " + filePath.toString(), exception));
    }
  }

  public static Mono<InputStream> readFileStream(Path filePath) {
    return FileUtils.exists(filePath)
        .flatMap(FileUtils::isFile)
        .flatMap(FileUtils::fileStream);
  }

  private static Mono<Void> writeString(Path filePath, String content, Charset charset) {
    try {
      return Mono.just(Files.writeString(filePath, content, charset)).then();
    } catch (IllegalArgumentException exception) {
      return Mono
          .error(new InternalApplicationException(
              "Options contains an invalid combination of options: " + filePath.toString(), exception));
    } catch (IOException exception) {
      return Mono
          .error(new InternalApplicationException(
              "I/O error occurs writing to or creating the file, or the text cannot be encoded using the specified charset: "
                  + filePath.toString(),
              exception));
    } catch (UnsupportedOperationException exception) {
      return Mono
          .error(new InternalApplicationException("An unsupported option is specified: " + filePath.toString(),
              exception));
    } catch (Exception exception) {
      return Mono
          .error(new InternalApplicationException("FileUtils::writeString failed: " + filePath.toString(), exception));
    }
  }

  public static Mono<Void> writeFileString(Path filePath, String content, Charset charset) {
    return FileUtils.writeString(filePath, content, charset);
  }

  public static Mono<Void> writeFileString(Path filePath, String content) {
    return FileUtils.writeFileString(filePath, content, StandardCharsets.UTF_8);
  }

  public static Path getTestResourcesDir() {
    return TEST_RESOURCES_DIR;
  }

  public static Path getIntegrationTestResourcesDir() {
    return INTEGRATION_TEST_RESOURCES_DIR;
  }
}
