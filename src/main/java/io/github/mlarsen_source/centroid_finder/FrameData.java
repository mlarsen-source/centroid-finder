package io.github.mlarsen_source.centroid_finder;

/**
 * Holds the total number of frames and the frames per second values.
 *
 * @param totalFrames the number of frames in the video 
 * @param fps         the frame rate in frames per second
 */
public record FrameData(int totalFrames, double fps) { }
