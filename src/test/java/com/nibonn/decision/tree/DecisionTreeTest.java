package com.nibonn.decision.tree;

import org.junit.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Unit test for simple App.
 */
public class DecisionTreeTest {

    private static DecisionTree.DecisionTreeBuilder builder;
    private static File testFile;
    private static Double[][] testData;
    private static Object node;
    private static Class c;

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
    public static void setup() throws FileNotFoundException, ClassNotFoundException {
        builder = new DecisionTree.DecisionTreeBuilder();
        testFile = new File("testdata.txt");
        generateTestData();
        c = Class.forName("com.nibonn.decision.tree.DecisionTree$Node");
    }

    @Before
    public void setupNode() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor cons = c.getDeclaredConstructor(int.class);
        cons.setAccessible(true);
        node = cons.newInstance(0);
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
    public void testNodeGini() throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        List<Double[]> l = new LinkedList<>();
        l.add(new Double[]{2.5});
        l.add(new Double[]{5.0});
        l.add(new Double[]{5.0});
        c.getDeclaredField("data").set(node, l);
        Assert.assertEquals(1 - 1 / 9.0 - 4 / 9.0, (double) c.getDeclaredMethod("gini").invoke(node), 0);
    }

    @Test
    public void testNodeFindMinGiniSplitPos() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<Double[]> l = new LinkedList<>();
        l.add(new Double[]{8.0, 1.0});
        l.add(new Double[]{5.0, 1.0});
        l.add(new Double[]{2.5, 0.0});
        l.add(new Double[]{6.5, 1.0});
        l.add(new Double[]{3.5, 0.0});
        l.add(new Double[]{0.5, 0.0});
        l.add(new Double[]{9.5, 1.0});
        l.add(new Double[]{1.5, 0.0});
        l.add(new Double[]{4.5, 0.0});
        l.add(new Double[]{7.5, 1.0});
        c.getDeclaredField("data").set(node, l);
        Assert.assertEquals(5, (int) c.getDeclaredMethod("findMinGiniSplitPos").invoke(node));
    }

    @Test
    public void testNodeSplit() throws NoSuchFieldException, IllegalAccessException {
        List<Double[]> l = new LinkedList<>();
        l.add(new Double[]{8.0, 0.0});
        l.add(new Double[]{5.0, 0.0});
        l.add(new Double[]{2.5, 1.0});
        l.add(new Double[]{6.5, 0.0});
        l.add(new Double[]{3.5, 0.0});
        l.add(new Double[]{0.5, 1.0});
        l.add(new Double[]{9.5, 0.0});
        l.add(new Double[]{1.5, 1.0});
        l.add(new Double[]{4.5, 1.0});
        l.add(new Double[]{7.5, 0.0});
        c.getDeclaredField("data").set(node, l);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke((RecursiveAction) node);
        Object lSon = c.getDeclaredField("lSon").get(node);
        Object rSon = c.getDeclaredField("rSon").get(node);
        Assert.assertTrue((boolean) c.getDeclaredField("isLeaf").get(lSon));
        Assert.assertTrue((boolean) c.getDeclaredField("isLeaf").get(rSon));
        Assert.assertEquals(1.0, (double) c.getDeclaredField("type").get(lSon), 0);
        Assert.assertEquals(0.0, (double) c.getDeclaredField("type").get(rSon), 0);
    }

    @AfterClass
    public static void clean() {
        testFile.delete();
    }

}
