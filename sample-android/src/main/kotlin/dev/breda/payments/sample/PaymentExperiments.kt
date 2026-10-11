package dev.breda.payments.sample

import dev.breda.payments.*
import java.util.UUID

fun interface PaymentClientFactory {
    fun create(mode: DemoMode, latencyMillis: Long, timeoutMillis: Long): PaymentClient
}

class SimulatedClientFactory : PaymentClientFactory {
    override fun create(mode: DemoMode, latencyMillis: Long, timeoutMillis: Long): PaymentClient {
        val scenario =
            when (mode) {
                DemoMode.DECLINE -> SimulationScenario.DECLINE
                DemoMode.UNAVAILABLE -> SimulationScenario.UNAVAILABLE
                else -> SimulationScenario.APPROVE
            }
        val latency = if (mode == DemoMode.TIMEOUT) timeoutMillis + 1500 else latencyMillis
        return PaymentClient(
            SimulatedPaymentGateway(scenario, latency),
            timeoutMillis = timeoutMillis,
        )
    }
}

class PrepareExperiment(
    private val clients: PaymentClientFactory,
    private val newKey: () -> String = { UUID.randomUUID().toString() },
) {
    operator fun invoke(
        raw: String,
        mode: DemoMode,
        latency: Long,
        timeout: Long,
    ): Pair<PaymentClient, PaymentRequest>? {
        val cents = AmountInput.cents(raw) ?: return null
        return clients.create(mode, latency, timeout) to PaymentRequest(cents, newKey())
    }
}
