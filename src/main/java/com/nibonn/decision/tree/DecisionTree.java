package com.nibonn.decision.tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Hello world!
 */
public class DecisionTree {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    private Node root;

    private DecisionTree() {
        root = new Node();
    }

    public static class DecisionTreeBuilder {
        private Double[][] data;

        public void loadData(String filename, int dimension, int size) throws FileNotFoundException {
            if (dimension <= 0 || size <= 0) {
                throw new IllegalArgumentException("dimension and size must greater than zero.");
            }
            data = new Double[size][dimension];
            Scanner in = new Scanner(new File(filename));
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < dimension; ++j) {
                    data[i][j] = in.nextDouble();
                }
                in.nextLine();
            }
            in.close();
        }

        public DecisionTree build() {
            // TODO build tree
            return null;
        }

    }

    private class Node {
        boolean isLeaf = false;
        Node lSon;
        Node rSon;
        List<Double[]> data;
        Map<Double, Integer> count;

        double gini() {
            if (count == null) {
                count();
            }
            return 1.0 - count.values().stream().reduce((a, b) -> a * a + b * b).get() / (double) (data.size() * data.size());
        }

        void count() {
            count = new HashMap<>();
            data.parallelStream().forEach(d -> {
                if (count.containsKey(d[d.length - 1])) {
                    count.put(d[d.length - 1], count.get(d[d.length - 1]) + 1);
                } else {
                    count.put(d[d.length - 1], 1);
                }
            });
        }
    }
}
