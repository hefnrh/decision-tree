package com.nibonn.decision.tree;

import java.io.File;
import java.io.FileNotFoundException;
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

        public Double[][] getData() {
            return data;
        }
    }

    private class Node {
        boolean isLeaf = false;
        Node lSon;
        Node rSon;
    }
}
