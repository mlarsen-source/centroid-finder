package io.github.mlarsen_source.centroid_finder;

import java.io.File;
import java.io.IOException;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;

/**
 * Handles video processing operations such as reading frames,
 * determining the frame rate (FPS), and converting frame numbers to timestamps.
 *
 */
public class VideoProcessor {

    /** The video file to be processed. */
    private final File video;

    private final FrameData frameData;

    /**
     * Constructs a VideoProcessor for the specified video file.
     *
     * @param video the video file to process
     * @throws IOException if the file cannot be read or metadata cannot be extracted
     * @throws JCodecException if an error occurs while parsing the video
     */
    public VideoProcessor(File video) throws IOException, JCodecException {
        this.video = video;
        FrameData frameData = computeFrameData();
        this.frameData = frameData;
    }

    /**
     * Computes the video's frame rate (FPS) using JCodec.
     *
     * @return the computed frames-per-second (FPS) value
     * @throws IOException if the video file cannot be accessed
     */
    private FrameData computeFrameData() throws IOException {
        MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(NIOUtils.readableChannel(video));
        DemuxerTrack videoTrack = demuxer.getVideoTrack();
        int totalFrames = videoTrack.getMeta().getTotalFrames();
        double totalDuration = videoTrack.getMeta().getTotalDuration();
        double fps = totalFrames / totalDuration;
        return new FrameData(totalFrames, fps);
    }

    /**
     * Returns the video's frame rate (FPS).
     *
     * @return the video's frames-per-second (FPS)
     */
     public double getFps() {
        return frameData.fps();
    }

    /**
     * Converts a frame index to a timestamp in seconds.
     *
     * The timestamp is calculated as frameNumber divided by fps.
     * For example, if the video runs at 30 FPS, frame 60 corresponds to 2.0 seconds.
     *
     * @param frameNumber the zero-based index of the frame
     * @return the time in seconds from the start of the video corresponding to that frame
     */
    public double getTime(int frameNumber) {
        return frameNumber / frameData.fps();
    }

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
    public FrameGrab getFrames() throws IOException, JCodecException {
        return FrameGrab.createFrameGrab(NIOUtils.readableChannel(video));
    }

    public int getTotalFrames() {
        return frameData.totalFrames();
    }
}

