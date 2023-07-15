package org.pytorch.demo.objectdetection;

import java.util.ArrayList;

public class OutputHolder {
    private static final OutputHolder instance = new OutputHolder();
    private static int classIndex;

    private OutputHolder() {}

    public static OutputHolder getInstance() {
        return instance;
    }

    public int getResults() {
        return classIndex;
    }

    public static void setResults(int resultss) {
        classIndex = resultss;
    }
}
