package io.github.mlarsen_source.centroid_finder;

public class VideoProcessingApp {

    public static void main(String[] args) {

        try {
            ArgumentParser parser = new CommandLineParser(args);

            String videoPath = parser.getVideoPath();
            String outputPath = parser.getOutputPath();
            int targetColor = parser.getTargetColor();
            int threshold = parser.getThreshold();

            if (videoPath == null || videoPath.isBlank()) {
                System.err.println("ERROR: Missing or invalid video path.");
                System.exit(1);
            }

            if (outputPath == null || outputPath.isBlank()) {
                System.err.println("ERROR: Missing or invalid output path.");
                System.exit(1);
            }

            VideoProcessingAppRunner runner = new VideoProcessingAppRunner();
            runner.processVideo(
                videoPath,      
                outputPath,    
                targetColor,    
                threshold      
            );

           
            System.out.println("Video processing complete.");
            System.exit(0);

        } catch (Exception ex) {
            
            System.err.println("ERROR: Video processing failed: " + ex.getMessage());
            System.exit(1);
        }
    }
}
