package io.github.mlarsen_source.centroid_finder;

import java.io.PrintWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;

/**
 * Writes the generated list of TimedCoordinate records to a CSV file.
 */
public class CsvWriter {

  /**
   * Writes a list of TimedCoordinate objects to a CSV file.
   * 
   * @param outputPath the file path where the CSV should be written
   * @param timedCoordinatesList the list of TimedCoordinate objects to write
   * @throws IOException if the file cannot be created or written to
   */
  public void writeToCsv(String outputPath, List<TimedCoordinate> timedCoordinatesList) throws IOException {
    LocalTime startTime = LocalTime.now();

    try (PrintWriter writer = new PrintWriter(outputPath)) {
      for (TimedCoordinate tc : timedCoordinatesList) {
        writer.println(tc.toCsvRow());
      }

      System.out.println("TimedCoordinate results saved at " + outputPath);

      LocalTime endTime = LocalTime.now();
      System.out.println("Duration: " + Duration.between(startTime, endTime));
    } 
    
    catch (Exception e) {
      System.err.println("Error writing " + outputPath);
      e.printStackTrace();
    }
  }
}