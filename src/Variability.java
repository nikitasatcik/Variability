import javafx.scene.chart.XYChart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Variability {
    private static final float sqrt2Pi = 2.5066282f;
    private static final float e = 2.71828182f;
    private float stdDeviation;
    private float variance;
    private float meanVal;
    private float coeffVariation;
    private float[][] arr;
    private float[][] normalDistArray;
    private float xMin;
    private float xMax;
    private float yMin;
    private float yMax;


    Variability(File file, boolean abs) {
        arr = getArray(file, abs);
        stdDeviation = computeStdDeviation(arr);
        variance = getVariance();
        coeffVariation = (stdDeviation / getMeanVal()) * 100;
    }

    Variability(File file) {
        arr = extractBytes(file);
        stdDeviation = computeStdDeviation(arr);
        variance = getVariance();
        coeffVariation = (stdDeviation / getMeanVal()) * 100;
    }

    private float computeStdDeviation(float[][] arr) {
        float n = arr.length * arr.length;
        float meanSum = 0;
        float sum = 0;
        float result;
        for (float[] anArr : arr) {
            for (int j = 0; j < arr.length; j++) {
                meanSum += anArr[j];
            }
        }
        meanSum /= n;
        meanVal = meanSum;
        for (float[] anArr : arr) {
            for (int j = 0; j < arr.length; j++) {
                sum += (anArr[j] - meanSum) * (anArr[j] - meanSum);
            }
        }
        variance = sum / (n - 1);
        result = (float) Math.sqrt(variance);
        return result;
    }

    private double normalDistribution(float[][] arr, int i, int j) {
        return (1 / (sqrt2Pi * stdDeviation)) * Math.pow(e, -(((arr[i][j] - meanVal) * (arr[i][j] - meanVal)) /
                (2 * stdDeviation * stdDeviation)));
    }

    private float[][] getArray(File file, boolean absolute) {
        int size = ImageSize(file);
        String[] strings = new String[size];
        List<String[]> list = new ArrayList<>();
        float[][] array = new float[strings.length][strings.length];
        int i = 0;
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                strings[i] = scanner.nextLine();
                i++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (i = 0; i < strings.length; i++) {
            list.add(strings[i].split("\\t"));
        }
        // fill Aij matrix
        for (i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if (absolute) {
                    array[i][j] = Math.abs(Float.parseFloat(list.get(i)[j]));
                } else {
                    array[i][j] = Float.parseFloat(list.get(i)[j]);
                }
            }
        }
        return array;
    }

    void saveNormalDistribution(File file, float[][] array) {
        String legend = String.format("Mean Value(µ) = %1$.6f \nStandard Deviation(σ) = %2$.6f\nVariance =  %3$.2f \n",
                meanVal, stdDeviation, coeffVariation);
        try {
            FileWriter writer = new FileWriter(file);
            normalDistArray = new float[array.length][array.length];
            String s;
            writer.write(legend);
            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array.length; j++) {
                    double normalVal = normalDistribution(array, i, j);
                    s = array[i][j] + " " + normalVal + "\n";
                    normalDistArray[i][j] = (float) normalVal;
                    writer.write(s);
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int ImageSize(File file) {
        int size = 0;
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                scanner.nextLine();
                size++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * method to convert jpg image to Aij matrix
     */
    private float[][] extractBytes(File file) {
        float[][] img = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            // get image size
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            // get DataBufferBytes from Raster
            WritableRaster raster = bufferedImage.getRaster();
            DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
            byte[] arr = data.getData();
            int[] array = new int[arr.length];
            //convert to Unsigned
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] < 0) {
                    array[i] = arr[i] + 256;
                } else if (arr[i] > 127) {
                    array[i] = arr[i] + 127;
                } else {
                    array[i] = arr[i];
                }
            }
            // fill image matrix
            img = new float[width][height];
            int counter = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    img[i][j] = array[counter];
                    counter++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    public void findExtremes() {
        xMin = 0;
        xMax = arr[0][0];
        yMin = 0;
        yMax = normalDistArray[0][0];

        for (float[] anArr : arr) {
            for (int j = 0; j < arr.length; j++) {
                if (anArr[j] < xMin) {
                    xMin = anArr[j];
                }
            }
        }

        for (float[] anArr : arr) {
            for (int j = 0; j < arr.length; j++) {
                if (anArr[j] > xMax) {
                    xMax = anArr[j];
                }
            }
        }

        for (float[] aNormalDistArray : normalDistArray) {
            for (int j = 0; j < normalDistArray.length; j++) {
                if (aNormalDistArray[j] < xMin) {
                    yMin = aNormalDistArray[j];
                }

            }
        }

        for (float[] aNormalDistArray : normalDistArray) {
            for (int j = 0; j < normalDistArray.length; j++) {
                if (aNormalDistArray[j] > yMax) {
                    yMax = aNormalDistArray[j];
                }

            }
        }
    }

    float getStdDeviation() {
        return stdDeviation;
    }

    private float getVariance() {
        return variance;
    }

    float getMeanVal() {
        return meanVal;
    }

    float getCoeffVariation() {
        return coeffVariation;
    }

    float[][] getArr() {
        return arr;
    }

    float[][] getNormalDistArray() {
        return normalDistArray;
    }

    float getxMin() {
        return xMin;
    }

    float getxMax() {
        return xMax;
    }

    float getyMin() {
        return yMin;
    }

    float getyMax() {
        return yMax;
    }
}