package io.github.mlarsen_source.centroid_finder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jcodec.api.JCodecException;

/**
 * Coordinates all necessary video processing steps.
 * 
 */
public class VideoProcessingAppRunner {

    /**
     * Executes the video processing steps.
     * 
     * @param videoPath the path to the input mp4 video file
     * @param outputPath the path where the CSV file will be written
     * @param targetColor the target color as an integer
     * @param threshold the color distance threshold
     * @throws IOException if a file cannot be read or written
     * @throws JCodecException if a video processing error occurs
     */
    public void processVideo(String videoPath, String outputPath, int targetColor, int threshold)
            throws IOException, JCodecException {

        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);
        ImageGroupFinder groupFinder = new BinarizingImageGroupFinder(binarizer, new BfsBinaryGroupFinder());

        VideoProcessor videoProcessor = new Mp4VideoProcessor(new File(videoPath));
        VideoGroupFinder videoGroupFinder = new Mp4VideoGroupFinder(videoProcessor, groupFinder);

        List<TimedCoordinate> timedCoordinatesList = videoGroupFinder.getTimeGroups();

        DataWriter writer = new CsvWriter();
        writer.writeToCsv(outputPath, timedCoordinatesList);
    }
}
