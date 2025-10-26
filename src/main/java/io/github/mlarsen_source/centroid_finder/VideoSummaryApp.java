package io.github.mlarsen_source.centroid_finder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.imageio.ImageIO;

import org.jcodec.api.JCodecException;

import java.time.LocalTime;
import java.time.Duration;

/**
 * The Image Summary Application.
 * 
 * This application takes three command-line arguments:
 * 1. The path to an input image file (for example, "image.png").
 * 2. A target hex color in the format RRGGBB (for example, "FF0000" for red).
 * 3. An integer threshold for binarization.
 * 
 * The application performs the following steps:
 * 
 * 1. Loads the input image.
 * 2. Parses the target color from the hex string into a 24-bit integer.
 * 3. Binarizes the image by comparing each pixel's Euclidean color distance to the target color.
 *    A pixel is marked white (1) if its distance is less than the threshold; otherwise, it is marked black (0).
 * 4. Converts the binary array back to a BufferedImage and writes the binarized image to disk as "binarized.png".
 * 5. Finds connected groups of white pixels in the binary image.
 *    Pixels are connected vertically and horizontally (not diagonally).
 *    For each group, the size (number of pixels) and the centroid (calculated using integer division) are computed.
 * 6. Writes a CSV file named "groups.csv" containing one row per group in the format "size,x,y".
 *    Coordinates follow the convention: (x:0, y:0) is the top-left, with x increasing to the right and y increasing downward.
 * 
 * Usage:
 *   java ImageSummaryApp <input_image> <hex_target_color> <threshold>
 */
public class VideoSummaryApp {
    public static void main(String[] args) throws IOException, JCodecException {
        LocalTime startTime = LocalTime.now();
        if (args.length < 4) {
            System.out.println("Usage: java ImageSummaryApp <input_image> <output_path> <hex_target_color> <threshold>");
            return;
        }
        
        String videoPath = args[0];
        String outputPath = args[1];
        String hexTargetColor = args[2];
        int threshold = 0;
        try {
            threshold = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.err.println("Threshold must be an integer.");
            return;
        }
        
        String extension = videoPath.split(".")[videoPath.split(".").length - 1].toLowerCase();
        if (!extension.equals("mp4")) {
            throw new IllegalArgumentException("Video type must be mp4");
        }

        File video = new File(videoPath);
        
        if (!video.exists()) {
            throw new IllegalArgumentException("No such file path exists");
        }
        
        // Parse the target color from a hex string (format RRGGBB) into a 24-bit integer (0xRRGGBB)
        int targetColor = 0;
        try {
            targetColor = Integer.parseInt(hexTargetColor, 16);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex target color. Please provide a color in RRGGBB format.");
            return;
        }
        
        // Create the DistanceImageBinarizer with a EuclideanColorDistance instance.
        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);
        
        
        // Create an ImageGroupFinder using a BinarizingImageGroupFinder with a DFS-based BinaryGroupFinder.
        ImageGroupFinder groupFinder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());
        
        

        VideoProcessor videoProcessor = new VideoProcessor(video);
        VideoGroupFinder videoGroupFinder = new VideoGroupFinder(videoProcessor, binarizer, groupFinder);
        List<TimedCoordinate> groups = videoGroupFinder.getTimeGroups();
        
        // Write the groups information to a CSV file "groups.csv".
        try (PrintWriter writer = new PrintWriter(outputPath)) {
            for (TimedCoordinate group : groups) {
                writer.println(group.toCsvRow());
            }
            System.out.println("Groups summary saved as groups.csv");
            LocalTime endTime = LocalTime.now();

            System.out.println("Duration: " + Duration.between(startTime, endTime));
        } catch (Exception e) {
            System.err.println("Error writing groups.csv");
            e.printStackTrace();
        }
    }
}
