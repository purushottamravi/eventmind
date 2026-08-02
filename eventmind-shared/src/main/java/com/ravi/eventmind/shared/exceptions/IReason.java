package com.ravi.eventmind.shared.exceptions;

/**
 * The deal every implementation has to keep: here's what a reason must be able to tell us.
 */
public interface IReason {
    String getKey();

    Integer getMaximumCharacter();

    String name();

    String getErrorCode();

}
