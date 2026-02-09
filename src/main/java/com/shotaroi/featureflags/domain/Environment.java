package com.shotaroi.featureflags.domain;

/**
 * Deployment environment. The same feature key can have different configuration
 * per environment (e.g. "new-checkout" on in dev, off in prod).
 */
public enum Environment {
    DEV,
    STAGING,
    PROD
}
