package com.shotaroi.featureflags.domain;

/**
 * Type of change recorded in the flag audit log.
 */
public enum FlagChangeType {
    FLAG_CREATED,
    FLAG_UPDATED,
    TARGET_ADDED,
    TARGET_REMOVED
}
