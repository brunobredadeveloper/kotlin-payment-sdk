package dev.breda.payments.sample

import dev.breda.payments.*
import org.junit.Assert.*
import org.junit.Test

class PrepareExperimentTest {
    @Test
    fun rejectsInputBeforeGatewayConstructionAndPassesStrategyOptions() {
        var constructed = 0
        val factory = PaymentClientFactory { mode, latency, timeout ->
            assertEquals(DemoMode.DECLINE, mode)
            assertEquals(400L, latency)
            assertEquals(1000L, timeout)
            constructed++
            PaymentClient(PaymentGateway { PaymentResult.GatewayUnavailable })
        }
        val prepare = PrepareExperiment(factory) { "injected-key" }
        assertNull(prepare("1.001", DemoMode.DECLINE, 400, 1000))
        assertEquals(0, constructed)
        val prepared = requireNotNull(prepare("12,34", DemoMode.DECLINE, 400, 1000))
        assertEquals(PaymentRequest(1234, "injected-key"), prepared.second)
        assertEquals(1, constructed)
    }
}
