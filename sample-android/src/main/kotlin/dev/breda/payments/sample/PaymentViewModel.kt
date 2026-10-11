package dev.breda.payments.sample
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.breda.payments.*
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

enum class DemoMode { APPROVE, DECLINE, UNAVAILABLE, TIMEOUT }
data class Experiment(val key:String,val amount:Long,val result:String,val elapsed:Long,val at:Long=System.currentTimeMillis())
data class DemoState(val busy: Boolean = false, val message: String = "Selecione um cenário e simule um pagamento.", val key: String = "", val status:String="idle")
class PaymentViewModel : ViewModel() {
 private val state = MutableStateFlow(DemoState())
 val ui = state.asStateFlow()
 var history by androidx.compose.runtime.mutableStateOf(listOf<Experiment>());private set
 var latencyMillis by androidx.compose.runtime.mutableStateOf(800L)
 var timeoutMillis by androidx.compose.runtime.mutableStateOf(1500L)
 private var job: Job? = null
 private var last: Pair<PaymentClient, PaymentRequest>? = null
 fun pay(raw: String, mode: DemoMode) {
  if (state.value.busy) return
  val cents = runCatching { BigDecimal(raw.trim().replace(',', '.')).movePointRight(2).longValueExact() }.getOrNull()
  if (cents == null || cents <= 0) { state.value = DemoState(message = "Informe um valor positivo, com até duas casas decimais.",status="error"); return }
  val scenario = when(mode) { DemoMode.DECLINE -> SimulationScenario.DECLINE; DemoMode.UNAVAILABLE -> SimulationScenario.UNAVAILABLE; else -> SimulationScenario.APPROVE }
  val client = PaymentClient(SimulatedPaymentGateway(scenario, if(mode == DemoMode.TIMEOUT) timeoutMillis+1500 else latencyMillis), timeoutMillis = timeoutMillis)
  val request = PaymentRequest(cents, UUID.randomUUID().toString())
  last = client to request
  execute(client, request)
 }
 fun retry() { if (!state.value.busy) last?.let { (client, request) -> execute(client, request) } }
 fun conflict() { if(!state.value.busy) last?.let { (client,request)->execute(client,request.copy(amountInCents=if(request.amountInCents==Long.MAX_VALUE)request.amountInCents-1 else request.amountInCents+1)) } }
 fun cancel() { job?.cancel() }
 private fun execute(client: PaymentClient, request: PaymentRequest) {
  state.value = DemoState(true, "Processando…", request.idempotencyKey,"loading")
  job = viewModelScope.launch {
   var started=System.nanoTime()
   try {
    delay(1400) // visible preparation in the sample; excluded from gateway latency
    started=System.nanoTime()
    val result=client.pay(request)
    val message = when(result) {
     is PaymentResult.Approved -> "Aprovado · ${result.receipt.amountInCents} centavos · ${result.receipt.transactionId}"
     is PaymentResult.Declined -> "Recusado · ${result.reason}"
     PaymentResult.OutcomeUnknown -> "Timeout: resultado desconhecido. Repetir a chave mantém esse resultado."
     PaymentResult.GatewayUnavailable -> "Simulador indisponível."
     else -> "Resultado: $result"
    }
    history=listOf(Experiment(request.idempotencyKey,request.amountInCents,message,(System.nanoTime()-started)/1_000_000))+history
    state.value = state.value.copy(message = message,status=if(result is PaymentResult.Approved)"success" else "error")
   } catch (cancelled: CancellationException) {
    state.value = state.value.copy(status="error",message = "Espera cancelada. Em uma integração real, consulte o resultado usando a mesma chave.")
    throw cancelled
   } finally { state.value = state.value.copy(busy = false) }
  }
 }
}
