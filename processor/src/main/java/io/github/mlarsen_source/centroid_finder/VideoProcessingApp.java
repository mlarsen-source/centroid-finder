package io.github.mlarsen_source.centroid_finder;

public class VideoProcessingApp {

    public static void main(String[] args) throws Exception {

        CommandLineParser parser = new CommandLineParser(args);

        VideoProcessingAppRunner runner = new VideoProcessingAppRunner();
        runner.processVideo(
            parser.getVideoPath(),
            parser.getOutputPath(),
            parser.getTargetColor(),
            parser.getThreshold()
        );

        System.out.println("Video processing complete.");
    }
}