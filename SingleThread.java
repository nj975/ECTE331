package ProjectA;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SingleThread {

    private static int[][] sourceGray; // 2D array to hold the grayscale values of the source image
    private static int[][] templateGray; // 2D array to hold the grayscale values of the template image
    private static int width; // Width of the image
    private static int height; // Height of the image

    public static void main(String[] args) {
        try {
            // Paths to the template and source images
            String templatePath = "C:\\Users\\admin\\OneDrive\\Desktop\\Template.jpg";
            String sourcePath = "C:\\Users\\admin\\OneDrive\\Desktop\\TenCardG.jpg";

            // Read the images from the specified paths
            BufferedImage templateImage = ImageIO.read(new File(templatePath));
            BufferedImage sourceImage = ImageIO.read(new File(sourcePath));

            // Perform single-threaded template matching
            measureExecutionTimeSingleThread(templateImage, sourceImage);

        } catch (IOException e) {
            System.err.println("Error reading the image files. Please check the paths.");
            e.printStackTrace();
        }
    }

    // Measure the execution time of the single-threaded template matching
    public static void measureExecutionTimeSingleThread(BufferedImage templateImage, BufferedImage sourceImage) {
        long startTime = System.currentTimeMillis(); // Start time
        try {
            BufferedImage resultImage = templateMatchingSingleThread(templateImage, sourceImage);
            // Save the resulting image to the specified path
            saveImage(resultImage, "C:\\Users\\admin\\OneDrive\\Desktop\\result_single_thread.jpg");
        } catch (IOException e) {
            System.err.println("Error during template matching (single-threaded).");
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis(); // End time

        System.out.println("Single Thread execution time: " + (endTime - startTime) + " milliseconds");
    }

    // Perform template matching using a single thread
    public static BufferedImage templateMatchingSingleThread(BufferedImage templateImage, BufferedImage sourceImage) throws IOException {
        // Convert the images to grayscale
        sourceGray = convertToGrayscale(sourceImage);
        templateGray = convertToGrayscale(templateImage);

        int r1 = sourceGray.length; // Height of the source image
        int c1 = sourceGray[0].length; // Width of the source image
        int r2 = templateGray.length; // Height of the template image
        int c2 = templateGray[0].length; // Width of the template image
        int tempSize = r2 * c2; // Total number of pixels in the template
        double minimum = Double.MAX_VALUE; // Initialize minimum difference
        double ratio = 10; // Calibration ratio

        double[][] absDiffMat = new double[r1 - r2 + 1][c1 - c2 + 1]; // Matrix to hold the absolute differences
        Graphics2D g2d = sourceImage.createGraphics(); // Graphics object to draw on the source image
        g2d.setColor(Color.BLUE); // Set the color for drawing

        // Loop through each possible position of the template on the source image
        for (int i = 0; i <= r1 - r2; i++) {
            for (int j = 0; j <= c1 - c2; j++) {
                // Calculate the average absolute difference for the current position
                double absDiff = calculateAbsDiff(sourceGray, templateGray, i, j, r2, c2) / tempSize;
                absDiffMat[i][j] = absDiff;

                // Update the minimum difference if the current difference is smaller
                if (absDiff < minimum) {
                    minimum = absDiff;
                }
            }
        }

        double threshold = ratio * minimum; // Set the threshold for matching

        // Loop through each possible position of the template on the source image
        for (int i = 0; i <= r1 - r2; i++) {
            for (int j = 0; j <= c1 - c2; j++) {
                // If the difference at the current position is below the threshold, draw a rectangle
                if (absDiffMat[i][j] <= threshold) {
                    g2d.drawRect(j, i, c2, r2);
                }
            }
        }

        g2d.dispose(); // Dispose the graphics object
        return sourceImage; // Return the modified source image
    }

    // Calculate the absolute difference between the template and the source image at a given position
    private static double calculateAbsDiff(int[][] sourceImage, int[][] templateImage, int startX, int startY, int height, int width) {
        double sum = 0; // Sum of absolute differences
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                sum += Math.abs(sourceImage[startX + i][startY + j] - templateImage[i][j]);
            }
        }
        return sum; // Return the sum of absolute differences
    }

    // Convert a BufferedImage to grayscale
    public static int[][] convertToGrayscale(BufferedImage image) {
        width = image.getWidth(); // Get the width of the image
        height = image.getHeight(); // Get the height of the image

        int[][] grayImage = new int[height][width]; // Array to hold the grayscale values
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData(); // Get the pixel data

        int coord, pr, pg, pb;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                coord = 3 * (i * width + j); // Calculate the coordinate
                pr = pixels[coord] & 0xff; // Get the red value
                pg = pixels[coord + 1] & 0xff; // Get the green value
                pb = pixels[coord + 2] & 0xff; // Get the blue value
                grayImage[i][j] = (int) Math.round(0.299 * pr + 0.587 * pg + 0.114 * pb); // Convert to grayscale
            }
        }
        return grayImage; // Return the grayscale image
    }

    // Save the resulting image
    public static void saveImage(BufferedImage img, String path) {
        try {
            ImageIO.write(img, "jpg", new File(path)); // Write the image to the specified path
        } catch (IOException e) {
            System.err.println("Error saving the image.");
            e.printStackTrace();
        }
    }
}
