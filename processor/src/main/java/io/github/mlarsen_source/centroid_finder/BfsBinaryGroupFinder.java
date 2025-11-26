package io.github.mlarsen_source.centroid_finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Implementation of BinaryGroupFinder that uses Breadth-First Search (BFS)
 * to identify connected groups in a binary 2D array.
 */
public class BfsBinaryGroupFinder implements BinaryGroupFinder {
    /**
     * Finds connected groups of 1's in a binary 2D array using BFS.
     * Pixels are connected horizontally or vertically (4-connectivity).
     * Coordinate system: top-left is (0,0), x increases right, y increases down.
     * Centroid calculated using integer division of summed coordinates.
     * 
     * @param image rectangular 2D array containing only 1s and 0s
     * @return groups of connected pixels in descending order by Group's compareTo
     * @throws NullPointerException if image or any subarray is null
     * @throws IllegalArgumentException if array is empty or contains values other than 0 or 1
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
        if (!groups.isEmpty()) {
            groups.sort(Collections.reverseOrder());
        }

        return groups;
    }

    /**
     * Performs a breadth-first search starting from the given position to find
     * all connected pixels with value 1.
     * 
     * This method uses BFS to traverse the image and identify all pixels that
     * are horizontally or vertically connected to the starting position.
     * Visited pixels are marked in the visited array to avoid reprocessing.
     * 
     * @param row the starting row position
     * @param col the starting column position
     * @param image the binary 2D array to search
     * @param visited boolean array tracking which pixels have been visited
     * @param pixels output list that will contain all connected pixel coordinates as [row, col] pairs
     */
    public static void findConnectedGroups(int row, int col, int[][] image, boolean[][] visited, List<int[]> pixels) {
        if (visited[row][col]) return;

        Queue<int[]> queue = new LinkedList<>();
        visited[row][col] = true;
        queue.offer(new int[] { row, col });

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];
            pixels.add(new int[] { r, c });
            
            List<int[]> neighbors = validNeighbors(r, c, image, visited);

            for (int[] n : neighbors) {
               int nR = n[0];
               int nC = n[1];
               visited[nR][nC] = true;
               queue.offer(n); 
            } 
        }
    }

    /**
     * Finds all valid neighboring pixels that are horizontally or vertically adjacent
     * to the given position.
     * 
     * A neighbor is considered valid if it:
     * - Is within the bounds of the image array
     * - Has a value of 1 in the image
     * - Has not yet been visited
     * 
     * @param row the current row position
     * @param col the current column position
     * @param image the binary 2D array
     * @param visited boolean array tracking which pixels have been visited
     * @return list of valid neighbor coordinates as [row, col] pairs
     */
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
    
    /**
     * Creates a Group object from a list of pixel coordinates.
     * 
     * The group's size is the number of pixels. The centroid is calculated
     * by averaging all pixel coordinates using integer division.
     * Note: Row coordinates map to y values, and column coordinates map to x values.
     * 
     * @param pixels list of pixel coordinates as [row, col] pairs
     * @return a Group object with the calculated size and centroid
     */
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
