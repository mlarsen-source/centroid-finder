package io.github.mlarsen_source.centroid_finder;

public class VideoGroupFinder {
  
  public videoGroupFinder(videoprocessor, binarizer, distanceFinder, groupFinder) {
    this.groupFinder
  }

  getTimeGroups(video) {
    
    FrameGrab frames = getFrames(video);
    List<TimedCoordinate> TimedCoordinates = new ArrayList<>();
    for each frame { 

      //processing logic
      int[][] binaryImage = binarizer.toBinaryArray(frame);
      List<Group> groups = groupFinder.findConnectedGroups(frame);
      
      //location extraction
      Group largest = groups.get(0);
      Coordinate location = largest.Coordinate;

      //timestamp
      double timeFromStart = getTime(frame)
      TimedCoordinate = new TimedCoordinate(timeFromStart, location);

      timeGroups.add(TimedCoordinate);

    }
    return timeGroups;
  }
}
