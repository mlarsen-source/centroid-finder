package io.github.mlarsen_source.centroid_finder;

import java.io.File;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;

public class jcodecExample {

  public static void main(String[] args) {

    File file = new File("sample-video.mp4");
    
    FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
    
    Picture picture;

    while (null != (picture = grab.getNativeFrame())) {
    
}
}
  

}
