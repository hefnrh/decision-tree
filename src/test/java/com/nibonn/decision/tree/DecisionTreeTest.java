package com.nibonn.decision.tree;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * Unit test for simple App.
 */
public class DecisionTreeTest {

    private static DecisionTree.DecisionTreeBuilder builder;

    @BeforeClass
    public static void setup() {
        builder = new DecisionTree.DecisionTreeBuilder();
    }

    @Test
    public void testLoadData() {
        try {
            builder.loadData("testdata.txt", 3, 4);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertArrayEquals(new Double[]{1.0, 2.0, 3.0}, builder.getData()[0]);
        Assert.assertArrayEquals(new Double[]{4.0, 5.0, 6.0}, builder.getData()[1]);
        Assert.assertArrayEquals(new Double[]{7.0, 8.0, 9.0}, builder.getData()[2]);
        Assert.assertArrayEquals(new Double[]{10.0, 11.0, 12.0}, builder.getData()[3]);
    }


}
