package io.github.mlarsen_source.centroid_finder;

import java.io.IOException;
import java.util.List;

/**
 * Interface for writing data to an output destination.
 */
public interface DataWriter {

    /**
     * Writes a list of TimedCoordinate objects to an output destination.
     * 
     * @param outputPath the path where the data should be written
     * @param timedCoordinatesList the list of TimedCoordinate objects to write
     * @throws IOException if the data cannot be written
     */
    void writeToCsv(String outputPath, List<TimedCoordinate> timedCoordinatesList) throws IOException;
}
