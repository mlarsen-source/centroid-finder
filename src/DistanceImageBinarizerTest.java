import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

public class DistanceImageBinarizerTest {

  // ---------- toBinaryArray tests ----------

  @Test
  void testToBinaryArrayThrowsExceptionWhenImageIsNull() {
    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), 0x000000, 10);
    assertThrows(NullPointerException.class, () -> {
      b.toBinaryArray(null);
    });
  }

  @Test
  void testToBinaryArraySinglePixelExactlyTargetBecomesWhite() {
    int target = 0xFF0000;
    BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    img.setRGB(0, 0, target);

    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), target, 1);
    int[][] actual = b.toBinaryArray(img);

    assertEquals(1, actual.length);
    assertEquals(1, actual[0].length);
    assertEquals(1, actual[0][0]);
  }

  @Test
  void testToBinaryArraySinglePixelFarFromTargetBecomesBlack() {
    int target = 0xFFFFFF;
    BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    img.setRGB(0, 0, 0x000000);

    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), target, 10);
    int[][] actual = b.toBinaryArray(img);

    assertEquals(0, actual[0][0]);
  }

  @Test
  void testToBinaryArrayBoundaryEqualToThresholdIsBlack() {
    int c1 = 0x000000;
    int c2 = 0x0000FF; // distance = 255 from black
    int threshold = 255; // code uses strictly less-than

    BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    img.setRGB(0, 0, c2);

    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), c1, threshold);
    int[][] actual = b.toBinaryArray(img);

    assertEquals(0, actual[0][0]);
  }

  @Test
  void testToBinaryArrayRectangularImageHeightGreaterThanWidthIndicesCorrect() {
    // width=2, height=3; set (x=1, y=2) near target
    int target = 0x00FF00;
    BufferedImage img = new BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 2; x++) {
        img.setRGB(x, y, 0x000000);
      }
    }
    img.setRGB(1, 2, target);

    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), target, 2);
    int[][] actual = b.toBinaryArray(img);

    assertEquals(3, actual.length);
    assertEquals(2, actual[0].length);
    assertEquals(1, actual[2][1]);
    assertEquals(0, actual[0][0]);
  }

  @Test
  void testToBinaryArrayMatchesProvidedSampleOutput() throws Exception {
    // Reproduce README example: target FFA200, threshold 164
    BufferedImage input = ImageIO.read(new File("sampleInput/squares.jpg"));
    BufferedImage expectedBinarized = ImageIO.read(new File("sampleOutput/binarized.png"));

    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), 0xFFA200, 164);
    int[][] actual = b.toBinaryArray(input);

    assertEquals(expectedBinarized.getHeight(), actual.length);
    assertEquals(expectedBinarized.getWidth(), actual[0].length);

    for (int y = 0; y < expectedBinarized.getHeight(); y++) {
      for (int x = 0; x < expectedBinarized.getWidth(); x++) {
        int rgb = expectedBinarized.getRGB(x, y) & 0x00FFFFFF;
        int expected = (rgb == 0xFFFFFF) ? 1 : 0;
        assertEquals("Mismatch at (" + x + "," + y + ")", expected, actual[y][x]);
      }
    }
  }

  // ---------- toBufferedImage tests ----------

  @Test
  void testToBufferedImageThrowsExceptionWhenImageIsNull() {
    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), 0x000000, 1);
    assertThrows(NullPointerException.class, () -> {
      b.toBufferedImage(null);
    });
  }

  @Test
  void testToBufferedImageThrowsExceptionWhenImageIsEmpty() {
    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), 0x000000, 1);
    int[][] empty = new int[0][0];
    assertThrows(IllegalArgumentException.class, () -> {
      b.toBufferedImage(empty);
    });
  }

  @Test
  void testToBufferedImageThrowsExceptionWhenSubarrayIsNull() {
    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), 0x000000, 1);
    int[][] invalid = new int[][] { null };
    assertThrows(NullPointerException.class, () -> {
      b.toBufferedImage(invalid);
    });
  }

  @Test
  void testToBufferedImageThrowsExceptionWhenJaggedRowLengths() {
    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), 0x000000, 1);
    int[][] jagged = new int[][] { {1, 0, 1}, {1, 0} };
    assertThrows(IllegalArgumentException.class, () -> {
      b.toBufferedImage(jagged);
    });
  }

  @Test
  void testToBufferedImageThrowsExceptionWhenValuesNotBinary() {
    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), 0x000000, 1);
    int[][] invalid = new int[][] { { -1, 0, 2 } };
    assertThrows(IllegalArgumentException.class, () -> {
      b.toBufferedImage(invalid);
    });
  }

  @Test
  void testToBufferedImageProducesCorrectDimensionsAndColors() {
    ImageBinarizer b = new DistanceImageBinarizer(new EuclideanColorDistance(), 0x000000, 1);
    int[][] binary = new int[][] {
      {1, 0, 1},
      {0, 1, 0}
    };
    BufferedImage img = b.toBufferedImage(binary);

    // Expect 3x2 (width x height)
    assertNotNull(img);
    assertEquals(3, img.getWidth());
    assertEquals(2, img.getHeight());

    // Check a few pixels for exact RGB values
    assertEquals(0x00FFFFFF, img.getRGB(0, 0) & 0x00FFFFFF); // white
    assertEquals(0x00000000, img.getRGB(1, 0) & 0x00FFFFFF); // black
    assertEquals(0x00FFFFFF, img.getRGB(2, 1) & 0x00FFFFFF); // white
  }
}


