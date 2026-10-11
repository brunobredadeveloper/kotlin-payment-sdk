package dev.breda.payments.sample
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent { MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF8BE2D0))) { Surface(Modifier.fillMaxSize()) { DemoScreen() } } }
 }
}
@Composable
fun DemoScreen(vm: PaymentViewModel = viewModel()) {
 val ui by vm.ui.collectAsStateWithLifecycle()
 var amount by rememberSaveable { mutableStateOf("25,90") }
 var mode by rememberSaveable { mutableStateOf(DemoMode.APPROVE) }
 Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
  Text("BREDA · PAYMENT SDK", style = MaterialTheme.typography.labelLarge)
  Text("Uma API. Resultados explícitos.", style = MaterialTheme.typography.headlineMedium)
  Text("Integração de exemplo · pagamentos fictícios")
  OutlinedTextField(amount, { amount = it }, label = { Text("Valor em reais") }, singleLine = true, enabled = !ui.busy, modifier = Modifier.fillMaxWidth())
  DemoMode.entries.forEach { option ->
   FilterChip(selected = mode == option, onClick = { mode = option }, enabled = !ui.busy, label = { Text(when(option) { DemoMode.APPROVE -> "Aprovar"; DemoMode.DECLINE -> "Recusar"; DemoMode.UNAVAILABLE -> "Indisponível"; DemoMode.TIMEOUT -> "Timeout" }) })
  }
  Button(onClick = { vm.pay(amount, mode) }, enabled = !ui.busy, modifier = Modifier.fillMaxWidth()) { Text("Novo pagamento simulado") }
  if (ui.busy) { LinearProgressIndicator(Modifier.fillMaxWidth()); OutlinedButton(onClick = vm::cancel) { Text("Cancelar espera") } }
  OutlinedButton(onClick = vm::retry, enabled = !ui.busy && ui.key.isNotEmpty()) { Text("Repetir mesma requisição") }
  Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(ui.message); if(ui.key.isNotEmpty()) Text("Chave: ${ui.key}", style = MaterialTheme.typography.bodySmall) } }
  Text("Estado em memória. Cancelar a espera não comprova cancelamento financeiro. Sem dados de cartão ou conexão bancária.", style = MaterialTheme.typography.bodySmall)
 }
}
