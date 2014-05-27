package com.nibonn.decision.tree;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Random;

/**
 * Unit test for simple App.
 */
public class DecisionTreeTest {

    private static DecisionTree.DecisionTreeBuilder builder;
    private static File testFile;
    private static Double[][] testData;

    public static void generateTestData() {
        Random r = new Random();
        testData = new Double[100 + r.nextInt(900)][100 + r.nextInt(900)];
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(testFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < testData.length; ++i) {
            for (int j = 0; j < testData[i].length; ++j) {
                testData[i][j] = r.nextDouble() * r.nextInt();
                pw.print(testData[i][j] + " ");
            }
            pw.println();
        }
        pw.close();
    }
    @BeforeClass
    public static void setup() {
        builder = new DecisionTree.DecisionTreeBuilder();
        testFile = new File("testdata.txt");
        generateTestData();
    }

    @Test
    public void testLoadData() {
        try {
            builder.loadData("testdata.txt", testData[0].length, testData.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Double[][] data = null;
        try {
            Field f = builder.getClass().getDeclaredField("data");
            f.setAccessible(true);
            data = (Double[][]) f.get(builder);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < testData.length; ++i) {
            Assert.assertArrayEquals(testData[i], data[i]);
        }
    }

    @AfterClass
    public static void clean() {
        testFile.delete();
    }

}
