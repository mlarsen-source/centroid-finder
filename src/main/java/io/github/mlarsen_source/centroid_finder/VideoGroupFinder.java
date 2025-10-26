package io.github.mlarsen_source.centroid_finder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

/**
 * Extracts time-based centroid coordinates from a video by analyzing
 * each frame for connected groups of pixels that match a target color.
 *
 */
public class VideoGroupFinder {

  /** Provides access to video frames and timing information. */
  private final VideoProcessor processor;

  /** Converts each video frame into a binary 2D array based on color distance. */
  private final ImageBinarizer binarizer;

  /** Finds connected white pixel groups in a binary image using DFS traversal. */
  private final ImageGroupFinder groupFinder;

  /**
   * Constructs a VideoGroupFinder using the specified components.
   *
   * @param processor the VideoProcessor responsible for frame extraction
   * @param binarizer the DistanceImageBinarizer used to convert frames to binary images
   * @param groupFinder the DfsBinaryGroupFinder used to identify connected pixel groups
   */
  public VideoGroupFinder(VideoProcessor processor, ImageBinarizer binarizer, ImageGroupFinder groupFinder) {
    this.processor = processor;
    this.binarizer = binarizer;
    this.groupFinder = groupFinder;
  }

  /**
   * Processes each frame in the video to find time-based centroid coordinates.
   * Frames with no detected groups are skipped. The frame number is used to
   * compute the timestamp based on the video's frame rate (FPS).
   *
   * @return a list of TimedCoordinate objects representing centroids over time
   * @throws IOException if an error occurs while reading the video file
   * @throws JCodecException if an error occurs while decoding video frames
   */
  public List<TimedCoordinate> getTimeGroups() throws IOException, JCodecException {

    List<TimedCoordinate> timedCoordinatesList = new ArrayList<>();
    FrameGrab frames = processor.getFrames();
    Picture picture;
    int frameCount = 1;

    while ((picture = frames.getNativeFrame()) != null) {
      BufferedImage frame = AWTUtil.toBufferedImage(picture);

      // Identify connected white pixel groups
      List<Group> groups = groupFinder.findConnectedGroups(frame);

      // Skip frames without any detected groups
      if (groups.isEmpty()) {
        frameCount++;
        continue;
      }

      // Extract the largest group and its centroid location
      Group largest = groups.get(0);
      Coordinate location = largest.centroid();

      // Calculate the timestamp for this frame
      double timeFromStart = processor.getTime(frameCount);

      // Create a new TimedCoordinate record and store it
      TimedCoordinate tc = new TimedCoordinate(timeFromStart, location);
      timedCoordinatesList.add(tc);

      frameCount++;
    }

    return timedCoordinatesList;

  }
}

