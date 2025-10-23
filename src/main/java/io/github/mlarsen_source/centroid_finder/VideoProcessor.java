package io.github.mlarsen_source.centroid_finder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;

public class VideoProcessor {
  
  public File video;
  public double fps;
  public ImageGroupFinder videoGroupFinder;
  public FrameGrab frames;

  public VideoProcessor(File video, ImageGroupFinder videoGroupFinder) throws FileNotFoundException, IOException, JCodecException {
    this.video = video;
    this.videoGroupFinder = videoGroupFinder;
    this.fps = getFps();
    this.frames = getFrames();
  }

  public double getFps() throws FileNotFoundException, IOException {
    MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(NIOUtils.readableChannel(video));
    DemuxerTrack videoTrack = demuxer.getVideoTrack();
    return videoTrack.getMeta().getTotalFrames() / videoTrack.getMeta().getTotalDuration();
  }

  public double getTime(int frameNumber) {
    return frameNumber / fps;
  }

  public FrameGrab getFrames() throws FileNotFoundException, IOException, JCodecException {
    return FrameGrab.createFrameGrab(NIOUtils.readableChannel(video));
  }
}
