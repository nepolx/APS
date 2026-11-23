package org;

public class Main {

    private static final boolean STEP_MODE = true;

    public static void main(String[] args) {
        ExperimentRunner runner = new ExperimentRunner(
                2,
                0.5,
                1.0,
                2,
                0.5
        );

        if (STEP_MODE) {
            runner.build(10, true).start();
        } else {
            runner.runToAccuracy();
        }
    }
}