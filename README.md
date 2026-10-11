# Kotlin Payment SDK

A fictional payment SDK portfolio project by **Bruno Breda**, with a platform-independent Kotlin library and a Jetpack Compose Android integration sample.

## Why this project
Demonstrate public API design, typed payment outcomes, structured concurrency and explicit treatment of payment uncertainty. There are no real payments or proprietary integrations.

## Quick start
```kotlin
val client = PaymentClient(SimulatedPaymentGateway())
val request = PaymentRequest(amountInCents = 2590, idempotencyKey = "order-123")
val result = client.pay(request)
when (result) {
    is PaymentResult.Approved -> println(result.receipt.transactionId)
    is PaymentResult.Declined -> println(result.reason)
    PaymentResult.OutcomeUnknown -> println("Reconcile using the same request key")
    else -> println(result)
}
```

Money is BRL integer cents. Retain a client instance across retries. Retain the same key for the same logical payment; a different key means a different payment.

## Modules
| Module | Responsibility |
| --- | --- |
| `sdk` | Public contracts, PaymentClient, session idempotency and deterministic simulator |
| `sample-android` | Amount input, four scenarios, cancellation and exact request replay |

## Build and test
JDK 17 is required. Core builds do not require an Android SDK:
```sh
./gradlew :sdk:test :sdk:publishToMavenLocal
```
The Maven Local coordinates are `dev.breda:kotlin-payment-sdk:0.1.0`. No artifact has been published to Maven Central.

For the Android sample, install Android SDK 35 and open with Android Studio. Add `includeAndroid=true` to your local Gradle user properties or run:
```sh
./gradlew -PincludeAndroid=true :sample-android:lintDebug :sample-android:assembleDebug
```
Use `gradlew.bat` on Windows. Device minimum: API 26. Android Studio must sync with `includeAndroid=true` to display the sample module.

## Behavior contract
| Situation | Result |
| --- | --- |
| Positive amount and approved simulator | Receipt with transaction ID and exact cents |
| Declined simulator | Typed decline reason |
| Same key and same payload | Replay stored terminal result |
| Same key and different payload | IdempotencyConflict |
| Nonpositive amount / blank or oversized key | InvalidRequest, gateway not invoked |
| Gateway execution exceeds timeout | OutcomeUnknown, cached for exact retries |
| Caller cancels coroutine | Cancellation propagates; no cached terminal result |
| Gateway throws an ordinary exception | OutcomeUnknown; financial outcome cannot be inferred |
| Session cache reaches its limit | New keys rejected; old keys remain replayable |

A payment timeout or cancellation of the coroutine is not proof that a real payment was cancelled. Remote reconciliation and durable server idempotency are required for a real provider. [ADR 0001](docs/adr/0001-session-idempotency.md) records these tradeoffs.

## Demo walkthrough
1. Approve `25,90` and observe the receipt.
2. Repeat the same request and observe the same transaction ID.
3. Start a new payment in Decline or Unavailable mode.
4. Start Timeout mode and observe OutcomeUnknown.
5. Cancel an in-flight wait; repeat the same request to observe cancellation semantics in the simulator.

## Portfolio roadmap
- Persistent request store and provider status reconciliation.
- HTTP fake backend with durable idempotency and contract tests.
- Android UI and accessibility tests; device screenshots and demo recording.
- Binary API compatibility checks and release signing for a public package.

## Validation
Core compiled using Kotlin 2.1.20 with Coroutines 1.10.1; **12 JUnit tests passed** using the JUnit Platform runner. See `docs/validation.md`. Gradle `:sdk:test` and `:sdk:publishToMavenLocal` also completed successfully. The Android sample has not been compiled or device-tested. GitHub Actions defines independent core and Android jobs.
