package com.nibonn.decision.tree;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Unit test for simple App.
 */
public class DecisionTreeTest {

    private static DecisionTree.DecisionTreeBuilder builder;
    private static File testFile;
    private static Double[][] testData;

    public static void generateTestData() throws FileNotFoundException {
        Random r = new Random();
        testData = new Double[10 + r.nextInt(90)][10 + r.nextInt(90)];
        PrintWriter pw = new PrintWriter(testFile);
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
    public static void setup() throws FileNotFoundException {
        builder = new DecisionTree.DecisionTreeBuilder();
        testFile = new File("testdata.txt");
        generateTestData();
    }

    @Test
    public void testLoadData() throws FileNotFoundException, NoSuchFieldException, IllegalAccessException {
        builder.loadData("testdata.txt", testData[0].length, testData.length);
        Double[][] data;
        Field f = builder.getClass().getDeclaredField("data");
        f.setAccessible(true);
        data = (Double[][]) f.get(builder);
        for (int i = 0; i < testData.length; ++i) {
            Assert.assertArrayEquals(testData[i], data[i]);
        }
    }

    @Test
    public void testNodeGini() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        Class c = Class.forName("com.nibonn.decision.tree.DecisionTree$Node");
        Constructor cons = c.getDeclaredConstructor(DecisionTree.class);
        cons.setAccessible(true);
        Constructor out = DecisionTree.class.getDeclaredConstructor();
        out.setAccessible(true);
        Object node = cons.newInstance(out.newInstance());
        List<Double[]> l = new LinkedList<>();
        l.add(new Double[]{2.5});
        l.add(new Double[]{5.0});
        l.add(new Double[]{5.0});
        c.getDeclaredField("data").set(node, l);
        Assert.assertEquals(1 - 1 / 9.0 - 4 / 9.0, (double) c.getDeclaredMethod("gini").invoke(node), 0.01);
    }

    @AfterClass
    public static void clean() {
        testFile.delete();
    }

}
