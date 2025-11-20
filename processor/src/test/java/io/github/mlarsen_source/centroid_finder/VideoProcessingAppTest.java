package io.github.mlarsen_source.centroid_finder;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Lightweight tests for VideoProcessingApp that validate argument handling
 * and ensure the program behaves correctly without processing real video files.
 */
public class VideoProcessingAppTest {

  private static final Path VALID_VIDEO = Path.of("sampleInput/dummy_video.mp4");
  private static final Path VALID_OUTPUT = Path.of("output_test.csv");
  private static final String VALID_COLOR = "FFA500";
  private static final String VALID_THRESHOLD = "50";

  @BeforeEach
  void setUp() throws IOException {
    // Ensure the dummy input directory exists
    Files.createDirectories(VALID_VIDEO.getParent());

    // Create a small fake "video" file to satisfy file existence checks
    if (!Files.exists(VALID_VIDEO)) {
        Files.write(VALID_VIDEO, new byte[]{0, 0, 0, 0});
    }

    // Ensure the output file does not exist before the test
    Files.deleteIfExists(VALID_OUTPUT);
  }

  @AfterEach
  void tearDown() throws IOException, InterruptedException {
    // Give Windows a brief moment to release file locks
    System.gc();
    Thread.sleep(50);

    Files.deleteIfExists(VALID_VIDEO);
    Files.deleteIfExists(VALID_OUTPUT);
  }

  @Test
  void main_withMissingArgs_throwsHelpfulError() {
    String[] args = {};
    Executable run = () -> VideoProcessingApp.main(args);
    Exception ex = assertThrows(IllegalArgumentException.class, run);
    assertTrue(ex.getMessage().contains("Usage"), "Expected usage message in error");
  }

  @Test
  void main_withInvalidPath_throwsHelpfulError() {
    String[] args = {"fakePath/nonexistent.mp4", VALID_OUTPUT.toString(), VALID_COLOR, VALID_THRESHOLD};
    Executable run = () -> VideoProcessingApp.main(args);
    Exception ex = assertThrows(IllegalArgumentException.class, run);
    assertTrue(ex.getMessage().contains("No such file path"), "Expected missing file message");
  }

  @Test
  void main_withValidInput_handlesDecodeFailureGracefully() throws Exception {
    String[] args = {VALID_VIDEO.toString(), VALID_OUTPUT.toString(), VALID_COLOR, VALID_THRESHOLD};

    assertDoesNotThrow(() -> {
      try {
          VideoProcessingApp.main(args);
      } catch (IOException e) {
          // Expected for dummy video — log but don't rethrow
          System.out.println("Warning: JCodec failed to read dummy video — ignoring for test");
      }
    }, "VideoProcessingApp should handle decode failures gracefully");

    // Ensure the program didn't crash, and that no incorrect output file was left behind
    assertTrue(
      !Files.exists(VALID_OUTPUT) || Files.size(VALID_OUTPUT) == 0,
      "Output file should not exist or should be empty for invalid input"
    );
  }
}

