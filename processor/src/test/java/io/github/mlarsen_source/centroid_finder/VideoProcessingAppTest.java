package io.github.mlarsen_source.centroid_finder;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the VideoProcessingApp main() method that parses command-line arguments
 * and triggers the runner.
 */
public class VideoProcessingAppTest {

  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private ByteArrayOutputStream outContent;
  private ByteArrayOutputStream errContent;

  @BeforeEach
  void setupStreams() {
    outContent = new ByteArrayOutputStream();
    errContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @AfterEach
  void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
}

  /**
   * Helper to create a small dummy mp4 for valid input tests.
   */
  private File createDummyVideoFile() throws Exception {
    File temp = Files.createTempFile("videoprocessingapptest-", ".mp4").toFile();
    Files.write(temp.toPath(), List.of("fake content"));
    temp.deleteOnExit();
    return temp;
}

  @Test
  void main_printsUsage_whenNoArgumentsProvided() {
    VideoProcessingApp.main(new String[] {});
    String output = errContent.toString();
    assertTrue(output.toLowerCase().contains("usage") || output.toLowerCase().contains("arguments"));
  }

  @Test
  void main_printsError_whenTooFewArgumentsProvided() {
    String[] args = { "input.mp4", "output.csv", "FFA500" }; // missing threshold
    VideoProcessingApp.main(args);
    String output = errContent.toString();
    assertTrue(output.toLowerCase().contains("invalid") || output.toLowerCase().contains("error"));
  }

  @Test
  void main_handlesNonexistentVideoPath() {
    String[] args = { "nonexistent.mp4", "output.csv", "FFA500", "50" };
    VideoProcessingApp.main(args);
    String output = errContent.toString();
    assertTrue(output.toLowerCase().contains("no such") || output.toLowerCase().contains("not exist"));
  }

  @Test
  void main_runsSuccessfully_withValidArguments() throws Exception {
    File input = createDummyVideoFile();
    File output = Files.createTempFile("main-valid-output-", ".csv").toFile();
    output.delete();

    String[] args = {
      input.getAbsolutePath(),
      output.getAbsolutePath(),
      "FFA500",
      "50"
    };

    assertDoesNotThrow(() -> VideoProcessingApp.main(args));

    String stdOut = outContent.toString();
    assertTrue(stdOut.toLowerCase().contains("saved") || stdOut.toLowerCase().contains("results"));
  }

  @Test
  void main_handlesInvalidHexColorGracefully() throws Exception {
    File input = createDummyVideoFile();
    File output = Files.createTempFile("main-invalid-color-", ".csv").toFile();
    output.delete();

    String[] args = {
      input.getAbsolutePath(),
      output.getAbsolutePath(),
      "GGGGGG", // invalid hex
      "50"
    };

    VideoProcessingApp.main(args);
    String err = errContent.toString();
    assertTrue(err.toLowerCase().contains("invalid") || err.toLowerCase().contains("hex"));
  }

  @Test
  void main_handlesNegativeThresholdGracefully() throws Exception {
      File input = createDummyVideoFile();
      File output = Files.createTempFile("main-neg-threshold-", ".csv").toFile();
      output.delete();

      String[] args = {
          input.getAbsolutePath(),
          output.getAbsolutePath(),
          "FFA500",
          "-5"
      };

      VideoProcessingApp.main(args);
      String err = errContent.toString();
      assertTrue(err.toLowerCase().contains("threshold") || err.toLowerCase().contains("negative"));
  }
}
