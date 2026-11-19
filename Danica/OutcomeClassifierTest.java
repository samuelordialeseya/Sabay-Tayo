package com.sabaytayo;

public class OutcomeClassifierTest {

    public static void main(String[] args) {

        RelationshipStats stats = new RelationshipStats();
        OutcomeClassifier classifier = new OutcomeClassifier();

        // Test the default relationship values.
        Outcome result = classifier.classify(stats);

        System.out.println("Ending: " +
                result.getClass().getSimpleName());
    }
}

