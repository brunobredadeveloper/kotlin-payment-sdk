# ADR 0001 — Session idempotency and uncertainty
Status: Accepted

Keep the core independent of Android so host applications own lifecycle and UI. A suspend API participates in structured concurrency; caller cancellation is rethrown instead of converted to an error.

A Mutex serializes authorizations per client. Concurrent duplicate keys share the cached terminal result. Different payloads using the same key conflict. This simple policy limits throughput, but avoids background scopes, leaked work and complicated coalescing. Queue time is excluded from the gateway execution timeout.

The session cache has a configurable hard limit and never evicts keys: eviction could permit duplicate authorization. A production backend must enforce durable idempotency and expose reconciliation. This cache is not a distributed payment guarantee.

A timeout returns OutcomeUnknown, never a definitive financial cancellation. Exact retries replay this result; the integrator must reconcile remotely. Caller cancellation leaves no result in the cache, so a retry can invoke the gateway again with the SAME key. A real adapter must send that key unchanged to a durable backend.

The simulator has no external side effects. Approved/declined/unavailable outcomes are deterministic. An actual provider adapter, status query, persistent request store, reversal and refund operations are future work.
