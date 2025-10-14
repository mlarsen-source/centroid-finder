import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;

public class DfsBinaryGroupFinder implements BinaryGroupFinder {
   /**
    * Finds connected pixel groups of 1s in an integer array representing a binary image.
    * 
    * The input is a non-empty rectangular 2D array containing only 1s and 0s.
    * If the array or any of its subarrays are null, a NullPointerException
    * is thrown. If the array is otherwise invalid, an IllegalArgumentException
    * is thrown.
    *
    * Pixels are considered connected vertically and horizontally, NOT diagonally.
    * The top-left cell of the array (row:0, column:0) is considered to be coordinate
    * (x:0, y:0). Y increases downward and X increases to the right. For example,
    * (row:4, column:7) corresponds to (x:7, y:4).
    *
    * The method returns a list of sorted groups. The group's size is the number 
    * of pixels in the group. The centroid of the group
    * is computed as the average of each of the pixel locations across each dimension.
    * For example, the x coordinate of the centroid is the sum of all the x
    * coordinates of the pixels in the group divided by the number of pixels in that group.
    * Similarly, the y coordinate of the centroid is the sum of all the y
    * coordinates of the pixels in the group divided by the number of pixels in that group.
    * The division should be done as INTEGER DIVISION.
    *
    * The groups are sorted in DESCENDING order according to Group's compareTo method
    * (size first, then x, then y). That is, the largest group will be first, the 
    * smallest group will be last, and ties will be broken first by descending 
    * y value, then descending x value.
    * 
    * @param image a rectangular 2D array containing only 1s and 0s
    * @return the found groups of connected pixels in descending order
    */
    @Override
    public List<Group> findConnectedGroups(int[][] image) {
        if(image == null) throw new NullPointerException("array cannot be null");
        if (image.length == 0 || image[0].length == 0) throw new IllegalArgumentException("array cannot be empty");
        for (int[] subarray: image) {
            if (subarray == null) throw new NullPointerException("subarray cannot be null");
            for (int num : subarray) {
                if (num != 0 && num != 1) throw new IllegalArgumentException("array can only contain values of 1 or 0");
            }
        }
        boolean[][] visited = new boolean[image.length][image[0].length];
        List<Group> groups = new ArrayList<>();
      
        for (int row = 0; row < image.length; row++) {
            for (int col = 0; col < image[0].length; col++) {
                if (image[row][col] == 1 && !visited[row][col]) {
                    List<int[]> pixels = new ArrayList<>();
                    findConnectedGroups(row, col, image, visited, pixels);
                    groups.add(createGroup(pixels));
                }
            }
        }
        Collections.sort(groups, Collections.reverseOrder());
        return groups;
    }

    public static void findConnectedGroups(int row, int col, int[][] image, boolean[][] visited, List<int[]> pixels) {
        if (visited[row][col]) return;

        ArrayDeque<int[]> queue = new ArrayDeque<>();
        visited[row][col] = true;
        queue.addLast(new int[] { row, col });

        while (!queue.isEmpty()) {
            int[] current = queue.removeFirst();
            int r = current[0];
            int c = current[1];
            pixels.add(new int[] { r, c });

            // Up
            if (r - 1 >= 0 && image[r - 1][c] == 1 && !visited[r - 1][c]) {
                visited[r - 1][c] = true;
                queue.addLast(new int[] { r - 1, c });
            }
            // Down
            if (r + 1 < image.length && image[r + 1][c] == 1 && !visited[r + 1][c]) {
                visited[r + 1][c] = true;
                queue.addLast(new int[] { r + 1, c });
            }
            // Left
            if (c - 1 >= 0 && image[r][c - 1] == 1 && !visited[r][c - 1]) {
                visited[r][c - 1] = true;
                queue.addLast(new int[] { r, c - 1 });
            }
            // Right
            if (c + 1 < image[0].length && image[r][c + 1] == 1 && !visited[r][c + 1]) {
                visited[r][c + 1] = true;
                queue.addLast(new int[] { r, c + 1 });
            }
        }
    }

    public static List<int[]> validNeighbors(int row, int col, int[][] image, boolean[][] visited) {
        int[][] moves = {
            {-1, 0}, 
            {1, 0},  
            {0, 1}, 
            {0, -1}  
        };

        List<int[]> neighbors = new ArrayList<>();

        for (int[] move : moves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (newRow >= 0 &&
                newRow < image.length &&
                newCol >= 0 &&
                newCol < image[0].length &&
                image[newRow][newCol] == 1 &&
                !visited[newRow][newCol]) {
                neighbors.add(new int[] { newRow, newCol });
            }
        }
        return neighbors;
    }
    
    public static Group createGroup(List<int[]> pixels) {
        int size = pixels.size();
        int rowSum = 0;
        int colSum = 0;
        
        for(int[]pix : pixels) {
            rowSum += pix[0];
            colSum += pix[1];
        }
        int rowCord = rowSum / size;
        int colCord = colSum / size;

        Coordinate coord = new Coordinate(colCord, rowCord);
        return new Group(size, coord);
    }
}
