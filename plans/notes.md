Image Summary Application
  * The main application
  * Checks if the user provided at least three arguments or prints instructions and returns
  * Stores first 2 arguments as strings, checks the 3rd argument is a valid integer
  * Uses ImageIO.read() to create a BufferedImage object stored in inputImage variable 
  * Converts hex color saved as a string into an actual integer
  * Creates a new EuclideanColorDistance() object
  * Creates new DistanceImageBinarizer object passing in EuclideanColorDistance() object, target color integer, and threshold
  * Binarize and store the image
  * Create an new BinarizingImageGroupFinder
  * Outputs a binarized image and a CSV list of group centroids


ImageGroupFinder
  * Interface
  * Implemented by BinarizingImageGroupFinder


ImageBinarizer
  * Interface
  * Implemented by DistanceImageBinarizer
 

BinaryGroupFinder
  * Interface
  * Implemented by DfsBinaryGroupFinder


Group
  * Record
  * Holds an int representing a group's size and a Coordinate Record


Coordinate
  * Record
  * Holds (X , Y) location information


EuclideanColorDistance
  * Takes in two hex colors and returns a double representing how far apart the colors are
  * helper class to do the math and determine the distance between two colors

  Need to do:
  * Complete the distance method:
    * Extract the red, green, and blue components from each hex integer / mask & shift the unwanted colors
    * Apply the Euclidean distance formula to the extracted values.
    * Return the calculated distance.


DistanceImageBinarizer
* helper class to convert an image into a binary array

  Need to do:
  * Complete the toBinaryArray method:
    * Convert the given BufferedImage into a binary 2D array using color distance and a threshold.
    * return the binary 2D array

  * Complete toBufferedImage method:
    * convert binary 2D array into a BufferedImage
    * return the new BufferedImage


DfsBinaryGroupFinder
* helper class to find connected pixel groups

  Need to do:
  * Complete findConnectedGroups method:
    * take in 2d array and perform DFS to identify groups in the array
    * determine each group size and calculate the centroid
    * store group in ArrayList
    * use compareTo method to sort ArrayList in descending order
    * return ArrayList of groups


BinarizingImageGroupFinder
* helper class to coordinate other helper classes 

  Need to do:
  * Complete findConnectedGroups method:
    * take in a BufferedImage
    * convert image to 2D array (ImageBinarizer)
    * locate connected groups (BinaryGroupFinder)
    * return ArrayList of groups