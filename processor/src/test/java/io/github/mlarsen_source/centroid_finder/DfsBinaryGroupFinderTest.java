package io.github.mlarsen_source.centroid_finder;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


public class DfsBinaryGroupFinderTest {

  @Test
  void testFindConnectedGroupsThrowsExceptionWhenImageIsNull() {
    BinaryGroupFinder finder = new DfsBinaryGroupFinder();
    assertThrows(NullPointerException.class, () -> {
      finder.findConnectedGroups(null);
    });
  }

  @Test
  void testFindConnectedGroupsThrowsExceptionWhenImageIsEmpty() {
    BinaryGroupFinder finder = new DfsBinaryGroupFinder();
    int[][] empty = new int[0][0];
    assertThrows(IllegalArgumentException.class, () -> {
      finder.findConnectedGroups(empty);
    });
  }

  @Test
  void testFindConnectedGroupsThrowsExceptionWhenSubarrayIsNull() {
    BinaryGroupFinder finder = new DfsBinaryGroupFinder();
    int[][] invalid = {
      {1, 0, 1},
        null
    };
    assertThrows(NullPointerException.class, () -> {
      finder.findConnectedGroups(invalid);
    });
  }

  @Test
  void testFindConnectedGroupsCorrectGroupCountWhenZeroGroups() {
    int[][] testImg = {
      {0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0}
    };

    BinaryGroupFinder testFinder = new DfsBinaryGroupFinder();
    List<Group> testGroups = testFinder.findConnectedGroups(testImg);
    assertEquals(0, testGroups.size());
  }

  @Test
  void testFindConnectedGroupsCorrectGroupCountWhenOneGroup() {
    int[][] testImg = {
      {1, 1, 1, 1, 1},
      {0, 1, 1, 1, 1},
      {0, 0, 1, 1, 1},
      {0, 0, 0, 1, 1},
      {0, 0, 0, 0, 1}
    };

    BinaryGroupFinder testFinder = new DfsBinaryGroupFinder();
    List<Group> testGroups = testFinder.findConnectedGroups(testImg);
    assertEquals(1, testGroups.size());
  }

  @Test
  void testFindConnectedGroupsCorrectGroupCountWhenTwoGroups() {
    int[][] testImg = {
      {1, 1, 1, 1, 0},
      {1, 1, 1, 1, 0},
      {0, 0, 0, 0, 0},
      {0, 1, 1, 1, 1},
      {0, 1, 1, 1, 1}
    };

    BinaryGroupFinder testFinder = new DfsBinaryGroupFinder();
    List<Group> testGroups = testFinder.findConnectedGroups(testImg);
    assertEquals(2, testGroups.size());
  }

  @Test
  void testFindConnectedGroupsCorrectGroupCountFoundWhenTenGroups() {
    int[][] testImg = {
      {0, 0, 1, 0, 1},
      {0, 1, 0, 1, 0},
      {1, 0, 0, 0, 1},
      {0, 1, 0, 1, 0},
      {1, 0, 1, 0, 0},
    };

    BinaryGroupFinder testFinder = new DfsBinaryGroupFinder();
    List<Group> testGroups = testFinder.findConnectedGroups(testImg);
    assertEquals(10, testGroups.size());
  }

  @Test
  void testFindConnectedGroupsCorrectGroupCorrectSizeAndCentroidSinglePixelGroup() {
    List<int[]> pixels = new ArrayList<>();
    pixels.add(new int[]{0, 0});

    Group g = DfsBinaryGroupFinder.createGroup(pixels);

    assertEquals(1, g.size());
    assertEquals(0, g.centroid().x());
    assertEquals(0, g.centroid().y());
  }

  @Test
  void testFindConnectedGroupsCorrectGroupCorrectSizeAndCentroidSquareGroup(){
    List<int[]> pixels = new ArrayList<>();
    pixels.add(new int[]{0, 0});
    pixels.add(new int[]{0, 1});
    pixels.add(new int[]{1, 0});
    pixels.add(new int[]{1, 1});

    Group g = DfsBinaryGroupFinder.createGroup(pixels);

    assertEquals(4, g.size());
    assertEquals(0, g.centroid().x());
    assertEquals(0, g.centroid().y());
  }

  @Test
  void testFindConnectedGroupsCorrectGroupCorrectSizeAndCentroidHorizontalLineGroup() {
    List<int[]> pixels = new ArrayList<>();
    pixels.add(new int[]{0, 0});
    pixels.add(new int[]{0, 1});
    pixels.add(new int[]{0, 2});

    Group g = DfsBinaryGroupFinder.createGroup(pixels);

    assertEquals(3, g.size());
    assertEquals(1, g.centroid().x());
    assertEquals(0, g.centroid().y());
  }

  @Test
  void testFindConnectedGroupsCorrectGroupCorrectSizeAndCentroidVerticalLineGroup() {
    List<int[]> pixels = new ArrayList<>();
    pixels.add(new int[]{0, 0});
    pixels.add(new int[]{1, 0});
    pixels.add(new int[]{2, 0});
    pixels.add(new int[]{3, 0});

    Group g = DfsBinaryGroupFinder.createGroup(pixels);

    assertEquals(4, g.size());
    assertEquals(0, g.centroid().x());
    assertEquals(1, g.centroid().y());
  }
}
