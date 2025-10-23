package io.github.mlarsen_source.centroid_finder;

import java.io.File;
import java.io.IOException;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;

import java.awt.image.BufferedImage;  // From JDK (should work)
import org.jcodec.scale.AWTUtil;  

public class JcodecExampleD {

  public static BufferedImage getFrame(String path, int frameNumber)
  throws IOException, JCodecException {
    
    Picture picture = FrameGrab.getFrameFromFile(new File(path), frameNumber);
    
    //for JDK (jcodec-javase)
    BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);

    MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(
            NIOUtils.readableChannel(new File(path))
        );
        
        DemuxerTrack videoTrack = demuxer.getVideoTrack();
      
        // Get frame rate
        double fps = videoTrack.getMeta().getTotalFrames() / 
                     videoTrack.getMeta().getTotalDuration();
        
        System.out.println("FPS: " + fps);
        double currentSeconds = frameNumber / fps;
        System.out.println("Current Seconds: " + currentSeconds);


    return bufferedImage;
    
  }

}