package com.nibonn.decision.tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Hello world!
 */
public class DecisionTree {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    private Node root;

    private DecisionTree() {
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
            DecisionTree tree = new DecisionTree();
            Node n = new Node(0);
            n.data = Arrays.asList(data);
            tree.root = n;
            ForkJoinPool pool = new ForkJoinPool();
            pool.invoke(n);
            pool.shutdown();
            return tree;
        }

    }

    private static class Node extends RecursiveAction {

        boolean isLeaf = false;
        Node lSon;
        Node rSon;
        List<Double[]> data;
        Map<Double, Integer> count;
        double type;
        double threshold;
        final static int MIN_SIZE = 1;
        int depth;

        Node(int depth) {
            this.depth = depth;
        }

        double gini() {
            if (count == null) {
                count();
            }
            double ret = 0;
            for (double d : count.values()) {
                ret += d * d;
            }
            double size = data.size();
            return 1.0 - ret / (size * size);
        }

        private void count() {
            count = new HashMap<>();
            data.stream().forEach(d -> {
                if (count.containsKey(d[d.length - 1])) {
                    count.put(d[d.length - 1], count.get(d[d.length - 1]) + 1);
                } else {
                    count.put(d[d.length - 1], 1);
                }
            });
        }

        private void split() {
            count();
            if (count.keySet().size() == 1 ||
                    depth >= data.get(0).length - 1 || data.size() <= MIN_SIZE) {
                int max = 0;
                for (double k : count.keySet()) {
                    if (count.get(k) > max) {
                        max = count.get(k);
                        type = k;
                    }
                }
                isLeaf = true;
                return;
            }
            int pos = findMinGiniSplitPos();
            lSon.data.clear();
            lSon.count.clear();
            rSon.data.clear();
            rSon.count.clear();
            ListIterator<Double[]> li = data.listIterator();
            for (int i = 0; i < pos; ++i) {
                lSon.data.add(li.next());
            }
            for (int i = pos, j = data.size(); i < j; ++i) {
                rSon.data.add(li.next());
            }
            invokeAll(lSon, rSon);
        }

        int findMinGiniSplitPos() {
            sort(depth);
            lSon = new Node(depth + 1);
            rSon = new Node(depth + 1);
            lSon.data = new LinkedList<>();
            rSon.data = new LinkedList<>(data);
            ListIterator<Double[]> li = rSon.data.listIterator();
            rSon.count();
            lSon.count();
            double giniSplit = gini();
            double lastDDepth = 0;
            int pos = 0;
            boolean flag = false;
            for (int i = 0, j = data.size(); i < j; ++i) {
                Double[] d = li.next();
                lSon.data.add(d);
                li.remove();
                if (lSon.count.containsKey(d[d.length - 1])) {
                    lSon.count.put(d[d.length - 1], lSon.count.get(d[d.length - 1]) + 1);
                } else {
                    lSon.count.put(d[d.length - 1], 1);
                }
                rSon.count.put(d[d.length - 1], rSon.count.get(d[d.length - 1]) - 1);
                if (rSon.count.get(d[d.length - 1]) == 0) {
                    rSon.count.remove(d[d.length - 1]);
                }
                double tmp = lSon.gini() * lSon.data.size() + rSon.gini() * rSon.data.size();
                tmp /= data.size();
                if (flag) {
                    threshold = (lastDDepth + d[depth]) / 2.0;
                    flag = false;
                }
                if (tmp < giniSplit) {
                    giniSplit = tmp;
                    pos = i + 1;    // [start, pos), [pos, end)
                    lastDDepth = d[depth];
                    flag = true;
                }
            }
            return pos;
        }

        void sort(int depth) {
            Object[] tmpData = data.toArray();
            Arrays.parallelSort(tmpData, (d1, d2) -> ((Double[]) d1)[depth].compareTo(((Double[]) d2)[depth]));
            ListIterator<Double[]> i = data.listIterator();
            for (int j = 0; j < tmpData.length; ++j) {
                i.next();
                i.set((Double[]) tmpData[j]);
            }
        }

        @Override
        protected void compute() {
            split();
        }
    }

}
