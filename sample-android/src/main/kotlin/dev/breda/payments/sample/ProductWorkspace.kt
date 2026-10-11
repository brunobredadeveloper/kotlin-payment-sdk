package dev.breda.payments.sample

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private fun money(cents: Long) =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        .format(BigDecimal.valueOf(cents, 2))

@Composable
fun ProductWorkspace(account: AccountSession) {
    val vm: PaymentViewModel = paymentViewModel(account.session)
    val ui by vm.ui.collectAsStateWithLifecycle()
    var amount by rememberSaveable { mutableStateOf("25,90") }
    var mode by rememberSaveable { mutableStateOf(DemoMode.APPROVE) }
    var selected by rememberSaveable { mutableStateOf("") }
    Workspace(account, listOf("Overview", "Eventos", "Config", "Conta")) { route, go ->
        when (route) {
            "home" ->
                Page("Integration Lab") {
                    LabConsole(ui.key, ui.status)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Metric("Execuções", vm.history.size.toString(), Modifier.weight(1f))
                        Metric(
                            "Última latência",
                            (vm.history.firstOrNull()?.elapsed ?: 0).toString() + " ms",
                            Modifier.weight(1f),
                        )
                    }
                    ActionTile(
                        "Executar um cenário",
                        "Aprovação, recusa, falha e timeout",
                        Icons.Default.Science,
                    ) {
                        go("operation")
                    }
                    ActionTile(
                        "Event inspector",
                        "Correlation ID, resultado e duração",
                        Icons.Default.Terminal,
                    ) {
                        go("activity")
                    }
                    ActionTile(
                        "Configurar runtime",
                        "Latência do gateway e deadline",
                        Icons.Default.Tune,
                    ) {
                        go("tools")
                    }
                    Text(
                        "Biblioteca Kotlin independente do Firebase. A autenticação pertence apenas ao aplicativo sample."
                    )
                }
            "operation" ->
                Page("Novo experimento", { go("home") }) {
                    OutlinedTextField(
                        amount,
                        { amount = it },
                        label = { Text("Valor em reais") },
                        enabled = !ui.busy,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DemoMode.entries.forEach { option ->
                        FilterChip(
                            selected = mode == option,
                            onClick = { mode = option },
                            enabled = !ui.busy,
                            label = {
                                Text(
                                    when (option) {
                                        DemoMode.APPROVE -> "Aprovação"
                                        DemoMode.DECLINE -> "Recusa"
                                        DemoMode.UNAVAILABLE -> "Indisponível"
                                        DemoMode.TIMEOUT -> "Timeout"
                                    }
                                )
                            },
                        )
                    }
                    Button(
                        onClick = {
                            vm.pay(amount, mode)
                            go("result")
                        },
                        enabled = !ui.busy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Executar gateway")
                    }
                    Text("Timeout: ${vm.timeoutMillis} ms · latência: ${vm.latencyMillis} ms")
                }
            "result" ->
                ResultExperience(
                    if (ui.busy) "loading" else ui.status.takeIf { it != "idle" } ?: "error",
                    if (ui.busy) "Executando seu cenário"
                    else if (ui.status == "success") "Pagamento aprovado!"
                    else "O cenário retornou um erro",
                    if (ui.busy) "O gateway está processando sua requisição." else ui.message,
                    reference = ui.key,
                    primaryLabel = "Ver eventos da execução",
                    onPrimary = { go("activity") },
                    secondaryLabel = "Executar outro cenário",
                    onSecondary = { go("operation") },
                ) {
                    if (ui.busy) TextButton(vm::cancel) { Text("Cancelar espera") }
                    if (!ui.busy && ui.key.isNotEmpty()) {
                        OutlinedButton(vm::retry, Modifier.fillMaxWidth()) {
                            Text("Repetir com a mesma chave")
                        }
                        OutlinedButton(vm::conflict, Modifier.fillMaxWidth()) {
                            Text("Testar conflito de idempotência")
                        }
                    }
                }
            "activity" ->
                Page("Event inspector") {
                    if (vm.history.isEmpty())
                        Text("Seu histórico começa no primeiro cenário executado.")
                    vm.history.forEachIndexed { i, event ->
                        ActionTile(
                            "#${vm.history.size-i} · ${money(event.amount)}",
                            event.result.take(75) + " · ${event.elapsed} ms",
                            Icons.Default.DataObject,
                        ) {
                            selected = event.at.toString() + event.key
                            go("details")
                        }
                    }
                }
            "details" ->
                Page("Contrato e telemetria", { go("activity") }) {
                    val e = vm.history.find { it.at.toString() + it.key == selected }
                    if (e != null) {
                        Hero("EXECUTION", money(e.amount), "Duração observada: ${e.elapsed} ms")
                        Text("request.idempotencyKey\n${e.key}")
                        Text("result\n${e.result}")
                        Text("Resultado registrado em ${java.util.Date(e.at)}")
                        Text(
                            "Valores e identificadores fictícios. O trace é local e limitado à sessão."
                        )
                    }
                }
            "tools" ->
                Page("Runtime settings") {
                    Hero(
                        "CONFIGURATION",
                        "Controle a execução",
                        "Timeout e latência são entradas reais do PaymentClient e simulador.",
                    )
                    Text("Gateway latency: ${vm.latencyMillis} ms")
                    Slider(
                        vm.latencyMillis.toFloat(),
                        { vm.latencyMillis = it.toLong() },
                        enabled = !ui.busy,
                        valueRange = 100f..4000f,
                    )
                    Text("Client deadline: ${vm.timeoutMillis} ms")
                    Slider(
                        vm.timeoutMillis.toFloat(),
                        { vm.timeoutMillis = it.toLong() },
                        enabled = !ui.busy,
                        valueRange = 500f..5000f,
                    )
                    ActionTile(
                        "Ver comportamento na prática",
                        "Iniciar um novo experimento",
                        Icons.Default.PlayArrow,
                    ) {
                        go("operation")
                    }
                    Text(
                        "Retries usam o cliente original e a requisição anterior. Novas configurações se aplicam aos próximos pagamentos."
                    )
                }
        }
    }
}

@Composable
private fun LabConsole(key: String, status: String) {
    Surface(
        color = androidx.compose.ui.graphics.Color(0xFF080B14),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "● ● ●   PAYMENT RUNTIME",
                color = Brand,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            )
            Text(
                "> client.connect(sandbox)\n> contract: PaymentResult\n> state: $status",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = androidx.compose.ui.graphics.Color(0xFF80E7AE),
            )
            Text(
                if (key.isBlank()) "awaiting request_" else "> correlation: $key",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
