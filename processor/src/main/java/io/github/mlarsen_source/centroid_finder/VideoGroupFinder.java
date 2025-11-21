package io.github.mlarsen_source.centroid_finder;

import java.io.IOException;
import java.util.List;

import org.jcodec.api.JCodecException;

/**
 * Interface for extracting time-based centroid coordinates from a video by analyzing
 * each frame for connected groups of pixels that match a target color.
 */
public interface VideoGroupFinder {

    /**
     * Processes each frame in the video to find time-based centroid coordinates.
     * Frames with no detected groups are skipped. The frame number is used to
     * compute the timestamp based on the video's frame rate (FPS).
     *
     * @return a list of TimedCoordinate objects representing centroids over time
     * @throws IOException if an error occurs while reading the video file
     * @throws JCodecException if an error occurs while decoding video frames
     */
    List<TimedCoordinate> getTimeGroups() throws IOException, JCodecException;
}
