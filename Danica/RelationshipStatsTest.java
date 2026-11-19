package com.sabaytayo;

public class RelationshipStatsTest {

    public static void main(String[] args) {

        RelationshipStats stats = new RelationshipStats();

        // Test the upper limit.
        stats.adjustTrust(1000);
        System.out.println("Trust upper limit: " +
                stats.get(RelationshipStats.TRUST));

        // Test the lower limit.
        stats.adjustTrust(-1000);
        System.out.println("Trust lower limit: " +
                stats.get(RelationshipStats.TRUST));

        // Test the other stats.
        stats.adjustAttraction(1000);
        stats.adjustRespect(-1000);
        stats.adjustAnnoyance(1000);

        System.out.println("Attraction: " +
                stats.get(RelationshipStats.ATTRACTION));

        System.out.println("Respect: " +
                stats.get(RelationshipStats.RESPECT));

        System.out.println("Annoyance: " +
                stats.get(RelationshipStats.ANNOYANCE));
    }
}
