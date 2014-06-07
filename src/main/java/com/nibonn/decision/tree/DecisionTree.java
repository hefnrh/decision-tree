package com.nibonn.decision.tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class DecisionTree {
    public static void main(String[] args) throws FileNotFoundException {
        DecisionTreeBuilder builder = new DecisionTreeBuilder();
        long startTime = System.currentTimeMillis();
        builder.loadData(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        System.out.println("load data time: " + (System.currentTimeMillis() - startTime) + "ms.");
        startTime = System.currentTimeMillis();
        DecisionTree tree = builder.build();
        System.out.println("build time: " + (System.currentTimeMillis() - startTime) + "ms.");
        startTime = System.currentTimeMillis();
        tree.testData(args[3], Integer.parseInt(args[4]));
        System.out.println("classify time: " + (System.currentTimeMillis() - startTime) + "ms.");
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
            if (data == null) {
                return null;
            }
            random();
            DecisionTree tree = new DecisionTree();
            Node n = new Node(0);
            n.data = Arrays.asList(data);
            tree.root = n;
            ForkJoinPool pool = new ForkJoinPool();
            pool.invoke(n);
            pool.shutdown();
            data = null;
            return tree;
        }

        private void random() {
            Random r = new Random();
            for (int i = 0, j; i < data.length; ++i) {
                j = r.nextInt(data.length - i) + i;
                Double[] tmp = data[i];
                data[i] = data[j];
                data[j] = tmp;
            }
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

        /**
         * count every class has how many instances
         */
        private void count() {
            if (count == null) {
                count = new HashMap<>();
            } else {
                count.clear();
            }
            data.stream().forEach(d -> {
                if (count.containsKey(d[d.length - 1])) {
                    count.put(d[d.length - 1], count.get(d[d.length - 1]) + 1);
                } else {
                    count.put(d[d.length - 1], 1);
                }
            });
        }

        /**
         * make this node a leaf node
         */
        private void toLeaf() {
            int max = 0;
            for (double k : count.keySet()) {
                if (count.get(k) > max) {
                    max = count.get(k);
                    type = k;
                }
            }
            isLeaf = true;
        }

        /**
         * make two son node
         */
        private void split() {
            count();
            /*
             early stop for nodes containing only one class or
             nodes containing less instances than minimum data set size
             and all attributes have been tested
              */
            if (count.keySet().size() == 1 ||
                    depth >= data.get(0).length - 1 || data.size() <= MIN_SIZE) {
                toLeaf();
                return;
            }
            int pos = findMinGiniSplitPos();
            /*
            early stop if all instances has similar attribute
             */
            if (pos == 0 || pos == data.size()) {
                toLeaf();
                return;
            }
            // clear data used in finding minimum gini split position
            lSon.data.clear();
            rSon.data.clear();
            ListIterator<Double[]> li = data.listIterator();
            // add instances to son nodes
            for (int i = 0; i < pos; ++i) {
                lSon.data.add(li.next());
            }
            for (int i = pos, j = data.size(); i < j; ++i) {
                rSon.data.add(li.next());
            }
            // recursively split son nodes
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
            // iterate sorted data set to find minimum gini split
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
                // calculate threshold
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

        /**
         * sort data set by the depth attribute
         *
         * @param depth which attribute used to sort
         */
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

        /**
         * estimate which son node the record belongs to
         * @param d the record to classify
         * @return the node the record belongs to
         */
        Node belongTo(double[] d) {
            return d[depth] <= threshold ? lSon : rSon;
        }
    }

    public void testData(String filename, int num) throws FileNotFoundException {
        Scanner in = new Scanner(new File(filename));
        final int DIMEN = root.data.get(0).length;
        double[][] testData = new double[num][DIMEN];
        for (int i = 0; i < num; ++i) {
            for (int j = 0; j < DIMEN; ++j) {
                testData[i][j] = in.nextDouble();
            }
            in.nextLine();
        }
        in.close();

        int errCount = 0;
        Map<Double, Integer> map = new HashMap<>();
        for (int i = 0; i < num; ++i) {
            double res = classify(testData[i]);
            if (testData[i][DIMEN - 1] != res) {
                ++errCount;
            }
            if (map.containsKey(res)) {
                map.put(res, map.get(res) + 1);
            } else {
                map.put(res, 1);
            }
        }
        System.out.println("error: " + errCount + " / " + num);
        map.forEach((k, v) -> System.out.println(k + " : " + v));
    }

    public double classify(double[] record) {
        Node now = root;
        while (!now.isLeaf) {
            now = now.belongTo(record);
        }
        return now.type;
    }
}
