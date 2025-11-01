package io.github.mlarsen_source.centroid_finder;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * Thorough validation tests for the CommandLineParser class.
 * 
 * Each argument (videoPath, outputPath, hexColor, threshold)
 * is tested across multiple edge and invalid conditions.
 */
public class CommandLineParserTest {


  private File createTempMp4() throws IOException {
      File video = Files.createTempFile("test-video", ".mp4").toFile();
      video.deleteOnExit();
      return video;
  }

  private String createValidCsvPath() throws IOException {
      File dir = Files.createTempDirectory("csv-out").toFile();
      dir.deleteOnExit();
      return new File(dir, "output.csv").getAbsolutePath();
  }

  private String[] baseArgs(File video, String output, String color, String threshold) {
      return new String[] { video.getAbsolutePath(), output, color, threshold };
  }

  @Test
  void videoPath_acceptsExistingMp4() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "FFA500", "25");
    assertDoesNotThrow(() -> new CommandLineParser(args));
}

  @Test
  void videoPath_rejectsNonExistingFile() throws IOException {
    File fake = new File("does-not-exist-12345.mp4");
    String[] args = baseArgs(fake, createValidCsvPath(), "FFA500", "25");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
}

  @Test
  void videoPath_rejectsWrongExtension() throws IOException {
    File wrongExt = Files.createTempFile("wrong", ".avi").toFile();
    String[] args = baseArgs(wrongExt, createValidCsvPath(), "FFA500", "25");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void videoPath_rejectsDirectoryInsteadOfFile() throws IOException {
    File dir = Files.createTempDirectory("fake-video-dir").toFile();
    String[] args = baseArgs(dir, createValidCsvPath(), "FFA500", "25");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void videoPath_rejectsEmptyString() throws IOException {
    String[] args = { "", createValidCsvPath(), "FFA500", "25" };
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
}

  @Test
  void outputPath_acceptsValidCsv() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "FFA500", "25");
    assertDoesNotThrow(() -> new CommandLineParser(args));
}

  @Test
  void outputPath_rejectsNonCsvExtension() throws IOException {
    File video = createTempMp4();
    File badFile = new File("bad-output.txt");
    String[] args = baseArgs(video, badFile.getAbsolutePath(), "FFA500", "25");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void outputPath_rejectsNonExistingDirectory() throws IOException {
    File video = createTempMp4();
    File fakeDir = new File("missing_dir_xyz");
    String badOutput = new File(fakeDir, "output.csv").getAbsolutePath();
    String[] args = baseArgs(video, badOutput, "FFA500", "25");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void outputPath_rejectsDirectoryItself() throws IOException {
    File video = createTempMp4();
    File dir = Files.createTempDirectory("output-dir").toFile();
    String[] args = baseArgs(video, dir.getAbsolutePath(), "FFA500", "25");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void outputPath_rejectsEmptyString() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, "", "FFA500", "25");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void hexColor_acceptsValidUppercase() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "FFA500", "25");
    CommandLineParser parser = new CommandLineParser(args);
    assertEquals(0xFFA500, parser.getTargetColor());
  }

  @Test
  void hexColor_acceptsValidLowercase() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "ffa500", "25");
    CommandLineParser parser = new CommandLineParser(args);
    assertEquals(0xFFA500, parser.getTargetColor());
}

  @Test
  void hexColor_rejectsTooShort() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "FFF", "25");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void hexColor_rejectsTooLong() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "1234567", "25");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void hexColor_rejectsInvalidCharacters() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "GGG123", "25");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void threshold_acceptsZero() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "FFA500", "0");
    CommandLineParser parser = new CommandLineParser(args);
    assertEquals(0, parser.getThreshold());
}

  @Test
  void threshold_acceptsPositiveInteger() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "FFA500", "75");
    CommandLineParser parser = new CommandLineParser(args);
    assertEquals(75, parser.getThreshold());
}

  @Test
  void threshold_rejectsNegativeNumber() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "FFA500", "-10");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void threshold_rejectsNonNumeric() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "FFA500", "abc");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void threshold_rejectsDecimalValue() throws IOException {
    File video = createTempMp4();
    String[] args = baseArgs(video, createValidCsvPath(), "FFA500", "5.5");
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void throwsWhenTooFewArguments() throws IOException {
    String[] args = { "video.mp4", "output.csv", "FFA500" };
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }

  @Test
  void throwsWhenTooManyArguments() throws IOException {
    File video = createTempMp4();
    String output = createValidCsvPath();
    String[] args = { video.getAbsolutePath(), output, "FFA500", "25", "EXTRA" };
    assertThrows(IllegalArgumentException.class, () -> new CommandLineParser(args));
  }
}
