package io.github.mlarsen_source.centroid_finder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jcodec.api.JCodecException;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link VideoGroupFinder} using a programmatically generated 5-second MP4.
 */
public class VideoGroupFinderTest {

  /**
   * Simple fake that returns a scripted sequence of group lists, one per frame.
   */
  private static class ScriptedImageGroupFinder implements ImageGroupFinder {
    private final List<List<Group>> scriptedPerCall;
    int calls;

    ScriptedImageGroupFinder(List<List<Group>> scriptedPerCall) {
      this.scriptedPerCall = scriptedPerCall == null ? Collections.emptyList() : scriptedPerCall;
    }

    @Override
    public List<Group> findConnectedGroups(BufferedImage image) {
      int idx = calls++;
      if (idx < scriptedPerCall.size()) {
        return scriptedPerCall.get(idx);
      }
      return Collections.emptyList();
    }
  }

  /**
   * Subclass of VideoProcessor that throws from getFrames (constructor still validates the file and FPS).
   */
  private static class ThrowingVideoProcessor extends Mp4VideoProcessor {
    public ThrowingVideoProcessor(File video) throws IOException, JCodecException {
      super(video);
    }

    @Override
    public org.jcodec.api.FrameGrab getFrames() throws IOException, JCodecException {
      throw new IOException("boom");
    }
  }

  /**
   * Creates a temporary MP4 with the specified fps and seconds using JCodec's AWTSequenceEncoder.
   * Each frame is a simple solid color image to avoid heavy encoding overhead.
   */
  private static File createTestVideo(int fps, int seconds) throws IOException, JCodecException {
    int totalFrames = fps * seconds;
    File video = Files.createTempFile("videogroupfinder-test-", ".mp4").toFile();
    video.deleteOnExit();

    AWTSequenceEncoder encoder = AWTSequenceEncoder.createSequenceEncoder(video, fps);

    for (int i = 0; i < totalFrames; i++) {
      BufferedImage frame = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = frame.createGraphics();
      try {
        // Vary color by frame index for determinism
        g.setColor(new Color((i * 40) % 256, (i * 80) % 256, (i * 120) % 256));
        g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
        g.setColor(Color.WHITE);
        g.fillRect(i % frame.getWidth(), i % frame.getHeight(), 2, 2);
      } finally {
        g.dispose();
      }
      encoder.encodeImage(frame);
    }

    encoder.finish();
    return video;
  }

  @Test
  void getTimeGroups_returnsEmpty_whenNoFramesHaveGroups() throws Exception {
    File video = createTestVideo(1, 5); // 5 seconds at 1 fps => 5 frames
    VideoProcessor processor = new Mp4VideoProcessor(video);

    List<List<Group>> scripted = new ArrayList<>();
    // 5 empty frames
    for (int i = 0; i < 5; i++) scripted.add(Collections.emptyList());
    ScriptedImageGroupFinder groupFinder = new ScriptedImageGroupFinder(scripted);

    VideoGroupFinder finder = new VideoGroupFinder(processor, groupFinder);
    List<TimedCoordinate> actual = finder.getTimeGroups();

    assertNotNull(actual);
    assertEquals(0, actual.size());
    assertEquals(5, groupFinder.calls, "Should inspect each frame once");
  }

  @Test
  void getTimeGroups_skipsEmptyFrames_andUsesCorrectTimestamp() throws Exception {
    File video = createTestVideo(1, 5); // fps=1 for simple expected timestamps
    VideoProcessor processor = new Mp4VideoProcessor(video);

    List<List<Group>> scripted = new ArrayList<>();
    // frame 1: empty
    scripted.add(Collections.emptyList());
    // frame 2: one group at (2,3)
    scripted.add(Collections.singletonList(new Group(10, new Coordinate(2, 3))));
    // frames 3-5: empty
    scripted.add(Collections.emptyList());
    scripted.add(Collections.emptyList());
    scripted.add(Collections.emptyList());

    ScriptedImageGroupFinder groupFinder = new ScriptedImageGroupFinder(scripted);
    VideoGroupFinder finder = new VideoGroupFinder(processor, groupFinder);

    List<TimedCoordinate> actual = finder.getTimeGroups();
    assertEquals(1, actual.size());

    TimedCoordinate only = actual.get(0);
    // VideoGroupFinder uses frameCount starting at 1; this hit on frame 2
    assertEquals(processor.getTime(2), only.time(), 1e-9);
    assertEquals(2, only.centroid().x());
    assertEquals(3, only.centroid().y());
  }

  @Test
  void getTimeGroups_emitsOnePerNonEmptyFrame_inOrder() throws Exception {
    File video = createTestVideo(2, 5); // fps=2 => 10 frames
    VideoProcessor processor = new Mp4VideoProcessor(video);

    List<List<Group>> scripted = new ArrayList<>();
    // Frame 1: group
    scripted.add(Collections.singletonList(new Group(5, new Coordinate(1, 1))));
    // Frame 2: empty
    scripted.add(Collections.emptyList());
    // Frame 3: group
    scripted.add(Collections.singletonList(new Group(7, new Coordinate(4, 0))));
    // Remaining frames empty (to cover whole video)
    for (int i = 0; i < 7; i++) scripted.add(Collections.emptyList());

    ScriptedImageGroupFinder groupFinder = new ScriptedImageGroupFinder(scripted);
    VideoGroupFinder finder = new VideoGroupFinder(processor, groupFinder);

    List<TimedCoordinate> actual = finder.getTimeGroups();
    assertEquals(2, actual.size());

    TimedCoordinate f1 = actual.get(0);
    assertEquals(processor.getTime(1), f1.time(), 1e-9);
    assertEquals(1, f1.centroid().x());
    assertEquals(1, f1.centroid().y());

    TimedCoordinate f3 = actual.get(1);
    assertEquals(processor.getTime(3), f3.time(), 1e-9);
    assertEquals(4, f3.centroid().x());
    assertEquals(0, f3.centroid().y());
  }

  @Test
  void getTimeGroups_usesFirstGroupAsLargest_byContract() throws Exception {
    File video = createTestVideo(1, 1); // single frame video
    VideoProcessor processor = new Mp4VideoProcessor(video);

    List<Group> groups = new ArrayList<>();
    // By contract ImageGroupFinder returns DESC sorted; ensure first is the largest
    Group largest = new Group(10, new Coordinate(9, 9));
    Group smaller = new Group(3, new Coordinate(1, 1));
    groups.add(largest);
    groups.add(smaller);

    ScriptedImageGroupFinder groupFinder = new ScriptedImageGroupFinder(Collections.singletonList(groups));
    VideoGroupFinder finder = new VideoGroupFinder(processor, groupFinder);

    List<TimedCoordinate> actual = finder.getTimeGroups();
    assertEquals(1, actual.size());
    TimedCoordinate tc = actual.get(0);
    assertEquals(largest.centroid().x(), tc.centroid().x());
    assertEquals(largest.centroid().y(), tc.centroid().y());
  }

  @Test
  void getTimeGroups_propagatesExceptionFromVideoProcessorGetFrames() throws Exception {
    File video = createTestVideo(1, 1);
    VideoProcessor throwing = new ThrowingVideoProcessor(video);

    ScriptedImageGroupFinder groupFinder = new ScriptedImageGroupFinder(Collections.emptyList());
    VideoGroupFinder finder = new VideoGroupFinder(throwing, groupFinder);

    assertThrows(IOException.class, finder::getTimeGroups);
  }
}


