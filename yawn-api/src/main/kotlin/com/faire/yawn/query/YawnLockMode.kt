package com.faire.yawn.query

/**
 * Lock modes for Yawn queries, abstracting over SQL's locking mechanisms.
 *
 * These lock modes control how the database handles concurrent access to selected rows.
 * When a lock mode is set, the generated SQL will include the appropriate locking clause
 * (e.g., `FOR UPDATE` or `FOR SHARE`).
 *
 * @see org.hibernate.LockMode
 */
enum class YawnLockMode {
    /**
     * No lock. This is the default behavior.
     */
    NONE,

    /**
     * A shared lock (SQL: `FOR SHARE`).
     *
     * Use for "find or create" patterns where you need to prevent concurrent creates
     * but allow concurrent reads. This lock allows other transactions to read the rows
     * but prevents them from acquiring an exclusive lock until this transaction completes.
     *
     * Example use case: Multiple execution paths (e.g., event consumption, multiple jobs)
     * are calling a "find or create" path. Using this lock ensures the entity is only
     * created once by making concurrent transactions wait.
     */
    PESSIMISTIC_READ,

    /**
     * An exclusive lock (SQL: `FOR UPDATE`).
     *
     * Use when you intend to update the selected rows and want to prevent
     * concurrent modifications. This is the strongest lock mode and will block
     * other transactions from reading (with locks) or writing to the locked rows.
     *
     * Example use case: Event consumers that replicate data use this lock to prevent
     * concurrent modifications during batch updates.
     */
    PESSIMISTIC_WRITE,
}
