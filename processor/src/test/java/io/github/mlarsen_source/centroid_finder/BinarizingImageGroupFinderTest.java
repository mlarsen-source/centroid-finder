package io.github.mlarsen_source.centroid_finder;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


public class BinarizingImageGroupFinderTest {

  private static class MockImageBinarizer implements ImageBinarizer {
    BufferedImage receivedImage;
    int[][] toReturn;
    RuntimeException toThrow;
    int toBinaryCalls;

    @Override
    public int[][] toBinaryArray(BufferedImage image) {
      this.toBinaryCalls++;
      this.receivedImage = image;
      if (toThrow != null) throw toThrow;
      return toReturn;
    }
  }

  private static class MockBinaryGroupFinder implements BinaryGroupFinder {
    int[][] receivedBinary;
    List<Group> toReturn = new ArrayList<>();
    RuntimeException toThrow;
    int calls;

    @Override
    public List<Group> findConnectedGroups(int[][] image) {
      this.calls++;
      this.receivedBinary = image;
      if (toThrow != null) throw toThrow;
      return toReturn;
    }
  }

  @Test
  void findConnectedGroups_delegatesToDependencies_andReturnsGroups() {
    MockImageBinarizer mockBinarizer = new MockImageBinarizer();
    MockBinaryGroupFinder mockGroupFinder = new MockBinaryGroupFinder();

    int[][] binary = new int[][] { {1, 0}, {0, 1} };
    mockBinarizer.toReturn = binary;

    List<Group> expected = new ArrayList<>();
    expected.add(new Group(3, new Coordinate(1, 2)));
    mockGroupFinder.toReturn = expected;

    BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(mockBinarizer, mockGroupFinder);

    BufferedImage input = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
    List<Group> actual = finder.findConnectedGroups(input);

    assertSame(expected, actual, "Should return exactly the list produced by groupFinder");
    assertEquals(1, mockBinarizer.toBinaryCalls);
    assertSame(input, mockBinarizer.receivedImage);
    assertEquals(1, mockGroupFinder.calls);
    assertSame(binary, mockGroupFinder.receivedBinary);
  }

  @Test
  void findConnectedGroups_propagatesExceptionFromBinarizer() {
    MockImageBinarizer mockBinarizer = new MockImageBinarizer();
    MockBinaryGroupFinder mockGroupFinder = new MockBinaryGroupFinder();

    RuntimeException boom = new NullPointerException("image is null");
    mockBinarizer.toThrow = boom;

    BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(mockBinarizer, mockGroupFinder);

    assertThrows(NullPointerException.class, () -> {
      finder.findConnectedGroups(null);
    });
    assertEquals(1, mockBinarizer.toBinaryCalls);
    assertEquals(0, mockGroupFinder.calls);
  }

  @Test
  void findConnectedGroups_propagatesExceptionFromGroupFinder() {
    MockImageBinarizer mockBinarizer = new MockImageBinarizer();
    MockBinaryGroupFinder mockGroupFinder = new MockBinaryGroupFinder();

    mockBinarizer.toReturn = new int[][] { {1} };
    RuntimeException boom = new IllegalArgumentException("bad binary image");
    mockGroupFinder.toThrow = boom;

    BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(mockBinarizer, mockGroupFinder);

    BufferedImage input = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    assertThrows(IllegalArgumentException.class, () -> {
      finder.findConnectedGroups(input);
    });
    assertEquals(1, mockBinarizer.toBinaryCalls);
    assertEquals(1, mockGroupFinder.calls);
  }
}


