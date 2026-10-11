package dev.breda.payments

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentClientTest {
 private val request = PaymentRequest(2590, "order-123")
 @Test fun approvalContainsAmountAndTransactionId() = runTest {
  assertEquals(PaymentResult.Approved(Receipt("sim-order-123", 2590)), PaymentClient(SimulatedPaymentGateway()).pay(request))
 }
 @Test fun declineIsTyped() = runTest {
  assertEquals(PaymentResult.Declined(DeclineReason.REJECTED_BY_ISSUER), PaymentClient(SimulatedPaymentGateway(SimulationScenario.DECLINE)).pay(request))
 }
 @Test fun concurrentRetriesInvokeGatewayOnce() = runTest {
  var calls = 0
  val client = PaymentClient(PaymentGateway { calls++; delay(100); PaymentResult.Approved(Receipt("tx", it.amountInCents)) })
  val results = (1..30).map { async { client.pay(request) } }.awaitAll()
  assertEquals(1, calls)
  assertTrue(results.all { it == results.first() })
 }
 @Test fun changedPayloadConflictsWithoutAuthorizing() = runTest {
  var calls = 0
  val client = PaymentClient(PaymentGateway { calls++; PaymentResult.Declined(DeclineReason.INSUFFICIENT_FUNDS) })
  client.pay(request)
  assertEquals(PaymentResult.IdempotencyConflict, client.pay(request.copy(amountInCents = 999)))
  assertEquals(1, calls)
 }
 @Test fun invalidInputNeverInvokesGateway() = runTest {
  val client = PaymentClient(PaymentGateway { error("Must not be called") })
  for (invalid in listOf(request.copy(amountInCents = 0), request.copy(amountInCents = -1), request.copy(idempotencyKey = " "), request.copy(idempotencyKey = "x".repeat(129)))) {
   assertEquals(PaymentResult.InvalidRequest, client.pay(invalid))
  }
 }
 @Test fun timeoutIsUnknownAndIsReplayed() = runTest {
  var calls = 0
  val client = PaymentClient(PaymentGateway { calls++; delay(200); PaymentResult.GatewayUnavailable }, timeoutMillis = 100)
  assertEquals(PaymentResult.OutcomeUnknown, client.pay(request))
  assertEquals(PaymentResult.OutcomeUnknown, client.pay(request))
  assertEquals(1, calls)
 }
 @Test fun callerCancellationPropagatesAndReleasesLock() = runTest {
  var calls = 0
  val client = PaymentClient(PaymentGateway { calls++; delay(1000); PaymentResult.Approved(Receipt("tx", it.amountInCents)) })
  val pending = async { client.pay(request) }
  runCurrent()
  pending.cancelAndJoin()
  assertTrue(pending.isCancelled)
  assertIs<PaymentResult.Approved>(client.pay(request))
  assertEquals(2, calls)
 }
 @Test fun cancellingQueuedRequestDoesNotAuthorizeIt() = runTest {
  var calls = 0
  val client = PaymentClient(PaymentGateway { calls++; delay(100); PaymentResult.GatewayUnavailable })
  val first = async { client.pay(request) }; runCurrent()
  val queued = async { client.pay(request.copy(idempotencyKey = "other")) }; runCurrent()
  queued.cancelAndJoin(); first.await()
  assertEquals(1, calls)
 }
 @Test fun gatewayExceptionHasUnknownFinancialOutcome() = runTest {
  assertEquals(PaymentResult.OutcomeUnknown, PaymentClient(PaymentGateway { throw IllegalStateException("offline") }).pay(request))
 }
 @Test fun capacityProtectsExistingKeysWithoutEvictingThem() = runTest {
  val client = PaymentClient(SimulatedPaymentGateway(latencyMillis = 0), maxSessionRequests = 1)
  val original = client.pay(request)
  assertEquals(PaymentResult.SessionCapacityReached, client.pay(request.copy(idempotencyKey = "new")))
  assertEquals(original, client.pay(request))
 }
 @Test fun differentKeysAreSeparatePayments() = runTest {
  var calls = 0
  val client = PaymentClient(PaymentGateway { calls++; PaymentResult.Approved(Receipt("tx-$calls", it.amountInCents)) })
  assertNotEquals(client.pay(request), client.pay(request.copy(idempotencyKey = "new")))
  assertEquals(2, calls)
 }
 @Test fun configurationRejectsInvalidLimits() {
  assertFailsWith<IllegalArgumentException> { PaymentClient(SimulatedPaymentGateway(), timeoutMillis = 0) }
  assertFailsWith<IllegalArgumentException> { PaymentClient(SimulatedPaymentGateway(), maxSessionRequests = 0) }
 }
}
