package dev.breda.payments.sample
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.breda.payments.*
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DemoMode { APPROVE, DECLINE, UNAVAILABLE, TIMEOUT }
data class DemoState(val busy: Boolean = false, val message: String = "Selecione um cenário e simule um pagamento.", val key: String = "")
class PaymentViewModel : ViewModel() {
 private val state = MutableStateFlow(DemoState())
 val ui = state.asStateFlow()
 private var job: Job? = null
 private var last: Pair<PaymentClient, PaymentRequest>? = null
 fun pay(raw: String, mode: DemoMode) {
  if (state.value.busy) return
  val cents = runCatching { BigDecimal(raw.trim().replace(',', '.')).movePointRight(2).longValueExact() }.getOrNull()
  if (cents == null || cents <= 0) { state.value = state.value.copy(message = "Informe um valor positivo, com até duas casas decimais."); return }
  val scenario = when(mode) { DemoMode.DECLINE -> SimulationScenario.DECLINE; DemoMode.UNAVAILABLE -> SimulationScenario.UNAVAILABLE; else -> SimulationScenario.APPROVE }
  val client = PaymentClient(SimulatedPaymentGateway(scenario, if(mode == DemoMode.TIMEOUT) 3000 else 800), timeoutMillis = 1500)
  val request = PaymentRequest(cents, UUID.randomUUID().toString())
  last = client to request
  execute(client, request)
 }
 fun retry() { if (!state.value.busy) last?.let { (client, request) -> execute(client, request) } }
 fun cancel() { job?.cancel() }
 private fun execute(client: PaymentClient, request: PaymentRequest) {
  state.value = DemoState(true, "Processando…", request.idempotencyKey)
  job = viewModelScope.launch {
   try {
    val message = when(val result = client.pay(request)) {
     is PaymentResult.Approved -> "Aprovado · ${result.receipt.amountInCents} centavos · ${result.receipt.transactionId}"
     is PaymentResult.Declined -> "Recusado · ${result.reason}"
     PaymentResult.OutcomeUnknown -> "Timeout: resultado desconhecido. Repetir a chave mantém esse resultado."
     PaymentResult.GatewayUnavailable -> "Simulador indisponível."
     else -> "Resultado: $result"
    }
    state.value = state.value.copy(message = message)
   } catch (cancelled: CancellationException) {
    state.value = state.value.copy(message = "Espera cancelada. Em uma integração real, consulte o resultado usando a mesma chave.")
    throw cancelled
   } finally { state.value = state.value.copy(busy = false) }
  }
 }
}
