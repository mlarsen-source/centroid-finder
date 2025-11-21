package io.github.mlarsen_source.centroid_finder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GroupTest {

  @Test
  void compareTo_ordersBySizeThenCoordinates() {
    Group smallest = new Group(1, new Coordinate(9, 0));
    Group secondSmallest = new Group(2, new Coordinate(0, 0));
    Group tieXyFirst = new Group(5, new Coordinate(3, 1));
    Group tieXySecond = new Group(5, new Coordinate(3, 7));
    Group largest = new Group(8, new Coordinate(5, 5));

    List<Group> groups = new ArrayList<>(List.of(largest, tieXySecond, smallest, secondSmallest, tieXyFirst));
    groups.sort(null); // relies on Group.compareTo

    assertEquals(List.of(smallest, secondSmallest, tieXyFirst, tieXySecond, largest), groups);
  }

  @Test
  void toCsvRow_formatsSizeAndCentroid() {
    Group group = new Group(42, new Coordinate(7, 11));
    assertEquals("42,7,11", group.toCsvRow());
  }
}

