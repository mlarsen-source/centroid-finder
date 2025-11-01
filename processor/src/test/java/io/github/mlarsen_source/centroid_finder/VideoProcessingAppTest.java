package io.github.mlarsen_source.centroid_finder;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import java.io.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the VideoProcessingApp class.
 */
public class VideoProcessingAppTest {

  private static final String VALID_VIDEO = "sampleInput/salamander_video.mp4";
  private static final String VALID_OUTPUT = "output_test.csv";
  private static final String VALID_COLOR = "FFA500";
  private static final String VALID_THRESHOLD = "50";

  @BeforeEach
  void cleanUp() throws IOException {
    Files.deleteIfExists(Path.of(VALID_OUTPUT));
  }

  @Test
  void main_withMissingArgs_throwsHelpfulError() {
    String[] args = {};
    Executable run = () -> VideoProcessingApp.main(args);
    Exception ex = assertThrows(IllegalArgumentException.class, run);
    assertTrue(ex.getMessage().contains("Usage"));
}

  @Test
  void main_withInvalidPath_throwsHelpfulError() {
    String[] args = {"fakePath/nonexistent.mp4", VALID_OUTPUT, VALID_COLOR, VALID_THRESHOLD};
    Executable run = () -> VideoProcessingApp.main(args);
    Exception ex = assertThrows(IllegalArgumentException.class, run);
    assertTrue(ex.getMessage().contains("No such file path"));
  }

  @Test
  void main_withValidInput_createsOutputFile() throws Exception {
    Path outputPath = Path.of("output_valid_test.csv");
    Files.deleteIfExists(outputPath);

    String[] args = {VALID_VIDEO, outputPath.toString(), VALID_COLOR, VALID_THRESHOLD};
    assertDoesNotThrow(() -> VideoProcessingApp.main(args));

    assertTrue(Files.exists(outputPath), "Expected output file to be created");
    Files.deleteIfExists(outputPath);
  }
}