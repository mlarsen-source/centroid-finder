package io.github.mlarsen_source.centroid_finder;

import java.io.IOException;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;

/**
 * Interface for video processing operations such as reading frames,
 * determining the frame rate (FPS), and converting frame numbers to timestamps.
 */
public interface VideoProcessor {

    /**
     * Returns the video's frame rate (FPS).
     *
     * @return the video's frames-per-second (FPS)
     */
    double getFps();

    /**
     * Converts a frame index to a timestamp in seconds.
     *
     * The timestamp is calculated as frameNumber divided by fps.
     * For example, if the video runs at 30 FPS, frame 60 corresponds to 2.0 seconds.
     *
     * @param frameNumber the zero-based index of the frame
     * @return the time in seconds from the start of the video corresponding to that frame
     */
    double getTime(int frameNumber);

    /**
     * Returns a new FrameGrab object that can iterate through the frames of the video.
     *
     * Each call creates a new FrameGrab instance starting from the first frame.
     * This method does not reuse any internal state and should be called once per
     * frame-processing session.
     *
     * @return a new FrameGrab for sequential frame access
     * @throws IOException if the video file cannot be read
     * @throws JCodecException if an error occurs while initializing frame extraction
     */
    FrameGrab getFrames() throws IOException, JCodecException;

    /**
     * Returns the total number of frames in the video.
     *
     * @return the total frame count
     */
    int getTotalFrames();
}
