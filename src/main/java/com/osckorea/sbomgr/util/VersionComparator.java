package com.osckorea.sbomgr.util;

public class VersionComparator implements Comparable<String> {
    private final String[] parts;

    public VersionComparator(String version) {
        this.parts = version.split("[\\.\\-]");
    }

    @Override
    public int compareTo(String other) {
        String[] otherParts = other.split("[\\.\\-]");
        int maxLength = Math.max(parts.length, otherParts.length);
        for (int i = 0; i < maxLength; i++) {
            int thisVal = (i < parts.length) ? parse(parts[i]) : 0;
            int otherVal = (i < otherParts.length) ? parse(otherParts[i]) : 0;
            if (thisVal != otherVal)
                return Integer.compare(thisVal, otherVal);
        }
        return 0;
    }

    private int parse(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return part.equalsIgnoreCase("x") ? Integer.MAX_VALUE : part.hashCode();
        }
    }
}