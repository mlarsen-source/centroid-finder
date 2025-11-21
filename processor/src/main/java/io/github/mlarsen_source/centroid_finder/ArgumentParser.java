package io.github.mlarsen_source.centroid_finder;

/**
 * Interface for parsing and validating command-line arguments.
 */
public interface ArgumentParser {

  /**
   * Returns the input video path.
   * 
   * @return the path to the input video file
   */
  String getVideoPath();

  /**
   * Returns the output CSV file path.
   * 
   * @return the path where the CSV file will be written
   */
  String getOutputPath();

  /**
   * Returns the target color as an integer.
   * 
   * @return the color value
   */
  int getTargetColor();

  /**
   * Returns the numeric threshold.
   * 
   * @return the threshold value
   */
  int getThreshold();
}
