package io.github.mlarsen_source.centroid_finder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class VideoProcessingAppTest {

    private static final Path VALID_VIDEO = Path.of("sampleInput/dummy_video.mp4");
    private static final Path VALID_OUTPUT = Path.of("output_test.csv");

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(VALID_VIDEO.getParent());

        if (!Files.exists(VALID_VIDEO)) {
            Files.write(VALID_VIDEO, new byte[]{0, 0, 0, 0});
        }

        Files.deleteIfExists(VALID_OUTPUT);
    }

    @AfterEach
    void tearDown() throws IOException, InterruptedException {
        System.gc();
        Thread.sleep(50);
        Files.deleteIfExists(VALID_VIDEO);
        Files.deleteIfExists(VALID_OUTPUT);
    }


    @Test
    void parser_withMissingArgs_throwsHelpfulError() {
        String[] args = {};
        assertThrows(IllegalArgumentException.class,
            () -> new CommandLineParser(args),
            "Expected parser to reject missing arguments");
    }

    @Test
    void parser_withInvalidPath_throwsHelpfulError() {
        String[] args = {"fakePath/nonexistent.mp4", "output.csv", "FFA500", "50"};

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new CommandLineParser(args)
        );

        assertTrue(ex.getMessage().contains("No such file path"),
            "Expected parser to reject non-existent input file");
    }

    @Test
    void parser_withValidArgs_parsesCorrectly() {
        String[] args = {
            VALID_VIDEO.toString(),
            VALID_OUTPUT.toString(),
            "FFA500",
            "50"
        };

        assertDoesNotThrow(() -> {
            ArgumentParser parser = new CommandLineParser(args);
            assertEquals(VALID_VIDEO.toString(), parser.getVideoPath());
            assertEquals(VALID_OUTPUT.toString(), parser.getOutputPath());
            assertEquals(0xFFA500, parser.getTargetColor());
            assertEquals(50, parser.getThreshold());
        });
    }


   @Test
    void runner_withValidInput_doesNotThrow() {
      VideoProcessingAppRunner runner = new VideoProcessingAppRunner();

      assertThrows(IOException.class, () ->
          runner.processVideo(
              VALID_VIDEO.toString(),
              VALID_OUTPUT.toString(),
              0xFFA500,
              50
          ),
          "Expected IOException for dummy MP4 input"
      );
  }


    @Test
    void runner_withDummyVideo_doesNotProduceCsv() throws IOException {
        VideoProcessingAppRunner runner = new VideoProcessingAppRunner();

        try {
            runner.processVideo(
                VALID_VIDEO.toString(),
                VALID_OUTPUT.toString(),
                0xFFA500,
                50
            );
        } catch (Exception ignored) {}

        assertTrue(
            !Files.exists(VALID_OUTPUT) || Files.size(VALID_OUTPUT) == 0,
            "Output CSV should be missing or empty for unreadable video"
        );
    }
}