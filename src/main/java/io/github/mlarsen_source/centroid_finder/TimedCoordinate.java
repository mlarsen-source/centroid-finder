package io.github.mlarsen_source.centroid_finder;

/**
 * Represents a centroid coordinate captured at a specific time in a video.
 * The top-left cell of the array (row:0, column:0) is considered to be coordinate (x:0, y:0).
 * Y increases downward and X increases to the right. For example, (row:4, column:7)
 * corresponds to (x:7, y:4).
 * 
 * The time field indicates the number of seconds since the beginning of the video
 * when the centroid was captured and calculated.
 * 
 * TimedCoordinate objects are naturally comparable by their {@code time} value.
 */
public record TimedCoordinate(double time, Coordinate centroid) implements Comparable<TimedCoordinate> {

    /**
     * Compares this TimedCoordinate with the specified one for order based on the time field.
     *
     * @param other the TimedCoordinate to be compared with this one
     * @return a negative integer, zero, or a positive integer if this object's time
     *         is less than, equal to, or greater than the specified object's time
     */
    @Override
    public int compareTo(TimedCoordinate other) {
        return Double.compare(this.time(), other.time());   
    }

    /**
     * Returns this TimedCoordinate formatted as a CSV row.
     * The format is "time,x,y", where time has two decimal places
     * and x and y are integer centroid coordinates.
     *
     * @return a CSV row string representing this TimedCoordinate
     */
    public String toCsvRow() {
        return String.format("%.2f,%d,%d", this.time(), this.centroid().x(), this.centroid().y());
    }
}
