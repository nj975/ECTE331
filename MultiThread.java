package ProjectA;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiThread {

    // Global variables for image dimensions and grayscale arrays
    private static int width;
    private static int height;
    private static int[][] sourceGray;
    private static int[][] templateGray;

    public static void main(String[] args) {
        try {
            // Replace these paths with the actual paths to your images
            String templatePath = "C:\\Users\\admin\\OneDrive\\Desktop\\Template.jpg";
            String sourcePath = "C:\\Users\\admin\\OneDrive\\Desktop\\TenCardG.jpg";

            // Read the images from the given file paths
            BufferedImage templateImage = ImageIO.read(new File(templatePath));
            BufferedImage sourceImage = ImageIO.read(new File(sourcePath));

            // Perform multi-threaded template matching
            measureExecutionTimeMultiThread(templateImage, sourceImage);

        } catch (IOException e) {
            System.err.println("Error reading the image files. Please check the paths.");
            e.printStackTrace();
        }
    }

    // Measures and prints the execution time of the multi-threaded template matching
    public static void measureExecutionTimeMultiThread(BufferedImage templateImage, BufferedImage sourceImage) {
        long startTime = System.currentTimeMillis();
        try {
            // Perform template matching
            BufferedImage resultImage = templateMatchingMultiThread(templateImage, sourceImage);
            // Save the result image to the specified path
            saveImage(resultImage, "C:\\Users\\admin\\OneDrive\\Desktop\\result_multi_thread.jpg");
        } catch (IOException e) {
            System.err.println("Error during template matching (multi-threaded).");
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();

        // Print the execution time
        System.out.println("MultiThread execution time: " + (endTime - startTime) + " milliseconds");
    }

    // Performs the template matching using multiple threads
    public static BufferedImage templateMatchingMultiThread(BufferedImage templateImage, BufferedImage sourceImage) throws IOException {
        // Convert the source and template images to grayscale
        sourceGray = convertToGrayscale(sourceImage);
        templateGray = convertToGrayscale(templateImage);

        // Get the dimensions of the source and template images
        int r1 = sourceGray.length;
        int c1 = sourceGray[0].length;
        int r2 = templateGray.length;
        int c2 = templateGray[0].length;
        int tempSize = r2 * c2;
        double ratio = 10; // Calibration ratio

        // Create a matrix to store the absolute differences
        double[][] absDiffMat = new double[r1 - r2 + 1][c1 - c2 + 1];
        Graphics2D g2d = sourceImage.createGraphics();
        g2d.setColor(Color.RED);

        // Use an array to hold the minimum value to allow updates within the lambda expression
        double[] minimum = {Double.MAX_VALUE};

        // Specify the number of threads to use here
        int numberOfThreads = 4; // Change this value to use a different number of threads
        System.out.println("Number of threads: " + numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Iterate over each position in the source image to calculate the absolute difference
        for (int i = 0; i <= r1 - r2; i++) {
            int finalI = i;
            executor.submit(() -> {
                for (int j = 0; j <= c1 - c2; j++) {
                    double absDiff = calculateAbsDiff(sourceGray, templateGray, finalI, j, r2, c2) / tempSize;
                    absDiffMat[finalI][j] = absDiff;

                    synchronized (minimum) {
                        if (absDiff < minimum[0]) {
                            minimum[0] = absDiff;
                        }
                    }
                }
            });
        }

        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Calculate the threshold for drawing rectangles
        double threshold = ratio * minimum[0];

        // Draw rectangles around the matched regions
        for (int i = 0; i <= r1 - r2; i++) {
            for (int j = 0; j <= c1 - c2; j++) {
                if (absDiffMat[i][j] <= threshold) {
                    g2d.drawRect(j, i, c2, r2);
                }
            }
        }

        g2d.dispose();
        return sourceImage;
    }

    // Calculates the absolute difference between the source and template images at the given position
    private static double calculateAbsDiff(int[][] sourceImage, int[][] templateImage, int startX, int startY, int height, int width) {
        double sum = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                sum += Math.abs(sourceImage[startX + i][startY + j] - templateImage[i][j]);
            }
        }
        return sum;
    }

    // Converts the given image to grayscale
    public static int[][] convertToGrayscale(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();

        int[][] grayImage = new int[height][width];
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        int coord, pr, pg, pb;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                coord = 3 * (i * width + j);
                pr = pixels[coord] & 0xff;
                pg = pixels[coord + 1] & 0xff;
                pb = pixels[coord + 2] & 0xff;
                grayImage[i][j] = (int) Math.round(0.299 * pr + 0.587 * pg + 0.114 * pb);
            }
        }
        return grayImage;
    }

    // Save the resulting image to the specified path
    public static void saveImage(BufferedImage img, String path) {
        try {
            ImageIO.write(img, "jpg", new File(path));
        } catch (IOException e) {
            System.err.println("Error saving the image.");
            e.printStackTrace();
        }
    }
}
