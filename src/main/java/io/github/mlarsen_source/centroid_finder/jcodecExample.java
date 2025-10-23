package io.github.mlarsen_source.centroid_finder;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.jcodec.api.JCodecException;

public class jcodecExample {

  public static void main(String[] args) throws IOException, JCodecException {
    
    File videoFile = new File("sampleInput/sample-video2.mp4");
    getFrame(videoFile, 2);

  }

  public static File getFrame(File videoFile, int frameNumber) throws IOException, JCodecException {
    
    Picture picture = FrameGrab.getFrameFromFile(videoFile, frameNumber);
    
    BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
    
    File outputFile = new File("sampleOutput/frame" + frameNumber + ".png");
    
    ImageIO.write(bufferedImage, "png", outputFile);
    
    return outputFile;
  }
}
