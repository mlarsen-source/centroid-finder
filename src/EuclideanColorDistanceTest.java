import static org.junit.Assert.*;

import org.junit.jupiter.api.Test;

public class EuclideanColorDistanceTest {

  @Test
  void testDistanceIdenticalColorsBlackIsZero() {
    EuclideanColorDistance d = new EuclideanColorDistance();
    assertEquals(0.0, d.distance(0x000000, 0x000000), 1e-9);
  }

  @Test
  void testDistanceIdenticalColorsRandomIsZero() {
    EuclideanColorDistance d = new EuclideanColorDistance();
    assertEquals(0.0, d.distance(0x7FA2C3, 0x7FA2C3), 1e-9);
  }

  @Test
  void testDistanceBlackToWhite() {
    EuclideanColorDistance d = new EuclideanColorDistance();
    double expected = Math.sqrt(3 * 255.0 * 255.0);
    assertEquals(expected, d.distance(0x000000, 0xFFFFFF), 1e-9);
  }

  @Test
  void testDistancePrimaryPairs() {
    EuclideanColorDistance d = new EuclideanColorDistance();
    double expected = Math.sqrt(2 * 255.0 * 255.0);
    assertEquals(expected, d.distance(0xFF0000, 0x00FF00), 1e-9); // red vs green
    assertEquals(expected, d.distance(0xFF0000, 0x0000FF), 1e-9); // red vs blue
    assertEquals(expected, d.distance(0x00FF00, 0x0000FF), 1e-9); // green vs blue
  }

  @Test
  void testDistanceSingleChannelDifferences() {
    EuclideanColorDistance d = new EuclideanColorDistance();
    assertEquals(255.0, d.distance(0x000000, 0xFF0000), 1e-9); // red only
    assertEquals(255.0, d.distance(0x000000, 0x00FF00), 1e-9); // green only
    assertEquals(255.0, d.distance(0x000000, 0x0000FF), 1e-9); // blue only
  }

  @Test
  void testSymmetryCommutativity() {
    EuclideanColorDistance d = new EuclideanColorDistance();
    int a = 0x123456;
    int b = 0xABCDEF & 0xFFFFFF; // ensure 24-bit
    double ab = d.distance(a, b);
    double ba = d.distance(b, a);
    assertEquals(ab, ba, 1e-12);
  }

  @Test
  void testIgnoresAlphaChannelBitsIfPresent() {
    EuclideanColorDistance d = new EuclideanColorDistance();
    int rgb = 0x00FFFFFF;   // white without alpha
    int argb = 0x80FFFFFF;  // white with alpha in high byte
    double baseToRgb = d.distance(0x000000, rgb);
    double baseToArgb = d.distance(0x000000, argb);
    assertEquals(baseToRgb, baseToArgb, 1e-12);
  }

  @Test
  void testNegativeIntRepresentsWhite() {
    EuclideanColorDistance d = new EuclideanColorDistance();
    int negativeWhite = -1; // 0xFFFFFFFF -> RGB 0xFFFFFF
    assertEquals(0.0, d.distance(negativeWhite, 0xFFFFFF), 1e-9);
  }

  @Test
  void testKnownMixedColorPair() {
    EuclideanColorDistance d = new EuclideanColorDistance();
    int c1 = 0x123456; // r1=18, g1=52, b1=86
    int c2 = 0x654321; // r2=101, g2=67, b2=33
    int dr = 18 - 101; // -83
    int dg = 52 - 67;  // -15
    int db = 86 - 33;  // 53
    double expected = Math.sqrt(dr * dr + dg * dg + db * db);
    assertEquals(expected, d.distance(c1, c2), 1e-9);
  }
}


