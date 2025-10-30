package io.github.mlarsen_source.centroid_finder;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.junit.jupiter.api.Test;

public class VideoProcessorTest {

  private static File createTestVideo(int fps, int seconds) throws IOException, JCodecException {
    int totalFrames = fps * seconds;
    File video = Files.createTempFile("videoprocessor-test-", ".mp4").toFile();
    video.deleteOnExit();

    AWTSequenceEncoder encoder = AWTSequenceEncoder.createSequenceEncoder(video, fps);
    for (int i = 0; i < totalFrames; i++) {
      BufferedImage frame = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = frame.createGraphics();
      try {
        g.setColor(new Color((i * 30) % 256, (i * 60) % 256, (i * 90) % 256));
        g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
      } finally {
        g.dispose();
      }
      encoder.encodeImage(frame);
    }
    encoder.finish();
    return video;
  }

  @Test
  void constructor_throwsWhenFileMissing() {
    File missing = new File("this-file-should-not-exist-12345.mp4");
    assertThrows(IOException.class, () -> {
      // JCodec demuxer access during construction should fail
      new VideoProcessor(missing);
    });
  }

  @Test
  void getFps_matchesEncodedFps() throws Exception {
    int fps = 5;
    File video = createTestVideo(fps, 5);
    VideoProcessor processor = new VideoProcessor(video);
    assertEquals(fps, Math.round(processor.getFps()));
  }

  @Test
  void getTime_convertsFrameIndexToSeconds() throws Exception {
    int fps = 4;
    File video = createTestVideo(fps, 5);
    VideoProcessor processor = new VideoProcessor(video);

    // frameNumber / fps
    assertEquals(0.0, processor.getTime(0), 1e-9);
    assertEquals(0.25, processor.getTime(1), 1e-9);
    assertEquals(1.0, processor.getTime(4), 1e-9);
    assertEquals(2.5, processor.getTime(10), 1e-9);
  }

  @Test
  void getFrames_iteratesAllFrames() throws Exception {
    int fps = 3;
    int seconds = 5;
    int expectedFrames = fps * seconds;
    File video = createTestVideo(fps, seconds);
    VideoProcessor processor = new VideoProcessor(video);

    FrameGrab grab = processor.getFrames();
    int framesSeen = 0;
    while (true) {
      org.jcodec.common.model.Picture p = grab.getNativeFrame();
      if (p == null) break;
      framesSeen++;
      if (framesSeen > expectedFrames) break; // safety to avoid infinite loops if any
    }
    assertEquals(expectedFrames, framesSeen);
  }
}


