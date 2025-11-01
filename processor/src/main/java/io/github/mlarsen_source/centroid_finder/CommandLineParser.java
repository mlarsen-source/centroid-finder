package io.github.mlarsen_source.centroid_finder;

import java.io.File;

/**
 * Parses and validates command-line arguments for the VideoSummaryApp.
 * 
 * This class checks the input video file, output file path, hex color value,
 * and threshold value to ensure all arguments are valid before the program runs.
 */
public class CommandLineParser {

    /** The path to the input video file. */
    private final String videoPath;

    /** The path where the CSV file will be written. */
    private final String outputPath;

    /** The target color value converted from a hex string. */
    private final int targetColor;

    /** The numeric threshold value used for image processing. */
    private final int threshold;

    /**
     * Creates a CommandLineParser object and validates the arguments.
     * 
     * @param args an array containing four arguments: input video, output CSV, hex target color, and threshold value
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public CommandLineParser(String[] args) {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                "Usage: java VideoSummaryApp <input_video> <output_csv> <hex_target_color> <threshold>"
            );
        }

        this.videoPath = args[0];
        this.outputPath = args[1];
        this.targetColor = checkHexTargetColor(args[2]);
        this.threshold = checkThreshold(args[3]);

        checkArguments();
    }

    /**
     * Checks that the provided file paths and extensions are valid.
     * 
     * @throws IllegalArgumentException if any validation check fails
     */
    private void checkArguments() {
        File video = new File(videoPath);
        if (!video.exists()) {
            throw new IllegalArgumentException("No such file path exists: " + videoPath);
        }

        String[] extension = videoPath.split("\\.");
        String ext = extension[extension.length - 1].toLowerCase();
        if (!ext.equals("mp4")) {
            throw new IllegalArgumentException("Video type must be mp4: " + videoPath);
        }

        File outFile = new File(outputPath);
        File parentDir = outFile.getParentFile();

        if (parentDir == null) {
            parentDir = new File(System.getProperty("user.dir"));
        }

        if (!parentDir.exists()) {
            throw new IllegalArgumentException("Output directory does not exist: " + parentDir.getAbsolutePath());
        }

        if (!outputPath.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Output file path must end with .csv: " + outputPath);
        }
    }

    /**
     * Validates and converts a 6-character hex color string into an integer.
     * 
     * @param hexTargetColor the color value in six-digit hexadecimal format
     * @return the integer representation of the color
     * @throws IllegalArgumentException if the hex string is not valid
     */
    private int checkHexTargetColor(String hexTargetColor) {
        if (hexTargetColor.length() != 6) {
            throw new IllegalArgumentException("Hex color must have exactly 6 characters (e.g., FFA500).");
        }

        char[] charList = hexTargetColor.toCharArray();

        for (int i = 0; i < charList.length; i++) {
            boolean isValidHexChar = false;

            if (charList[i] >= '0' && charList[i] <= '9') {
                isValidHexChar = true;
            }

            if (charList[i] >= 'A' && charList[i] <= 'F') {
                isValidHexChar = true;
            }

            if (charList[i] >= 'a' && charList[i] <= 'f') {
                isValidHexChar = true;
            }

            if (isValidHexChar == false) {
                throw new IllegalArgumentException("Invalid target hex color entered");
            }
        }

        return Integer.parseInt(hexTargetColor, 16);
    }

    /**
     * Converts the threshold value from string to integer and checks that it is non-negative.
     * 
     * @param value the threshold value provided as a string
     * @return the threshold value as an integer
     * @throws IllegalArgumentException if the value is not an integer or is negative
     */
    private int checkThreshold(String value) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0) {
                throw new IllegalArgumentException("Threshold must be a non-negative integer.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Threshold must be an integer: " + value);
        }
    }

    /**
     * Returns the input video path.
     * 
     * @return the path to the input video file
     */
    public String getVideoPath() {
        return videoPath;
    }

    /**
     * Returns the output CSV file path.
     * 
     * @return the path where the CSV file will be written
     */
    public String getOutputPath() {
        return outputPath;
    }

    /**
     * Returns the target color as an integer.
     * 
     * @return the color value
     */
    public int getTargetColor() {
        return targetColor;
    }

    /**
     * Returns the numeric threshold.
     * 
     * @return the threshold value
     */
    public int getThreshold() {
        return threshold;
    }
}
