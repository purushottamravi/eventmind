package com.ravi.eventmind.shared.constants.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum OriginType {

    A(1L, "A"),
    B(1L << 1,  "B"),
    C(1L << 2,  "C"),
    D(1L << 3,  "D"),
    E(1L << 4,  "E"),
   ;

    final Long id;
    final String name;
    OriginType(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public  Long getId(){
        return id;
    }

    public void setName(String name) {
    }

    public static OriginType valueOf(Long targetId) {
        for (OriginType ot : values()) {
            if (ot.getId().equals(targetId)) {
                return ot;
            }
        }
        return null;
    }

    /**
     * Looks up an {@link OriginType} by its symbolic name (case-insensitive),
     * e.g. {@code "A"} or {@code "b"}.
     */
    public static OriginType fromName(String name) {
        for (OriginType ot : values()) {
            if (ot.name().equalsIgnoreCase(name)) {
                return ot;
            }
        }
        return null;
    }

    /**
     * Decodes a cumulated bitmask back into the symbolic origin names, in enum
     * declaration order. For example {@code 3} yields {@code ["A","B"]}.
     */
    public static List<String> toNames(Long value) {
        List<String> names = new ArrayList<>();
        if (value != null) {
            for (OriginType ot : values()) {
                if ((value & ot.getId()) != 0) {
                    names.add(ot.name());
                }
            }
        }
        return names;
    }

    public static long cumulateAll(List<Long> values) {
        long returnValue = 0;
        for (Long flag : values) {
            returnValue |= flag;
        }
        return returnValue;
    }

    public static List<OriginType> allValuesSorted() {
        return new ArrayList<>(Arrays.asList(values()));
    }
}
