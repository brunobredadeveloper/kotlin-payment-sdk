package dev.breda.payments.sample

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
private fun money(cents:Long)=NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(BigDecimal.valueOf(cents,2))

@Composable fun ProductWorkspace(account:AccountSession) {
 val vm:PaymentViewModel=viewModel(key="lab-${account.session}");val ui by vm.ui.collectAsStateWithLifecycle()
 var amount by rememberSaveable {mutableStateOf("25,90")};var mode by rememberSaveable {mutableStateOf(DemoMode.APPROVE)};var selected by rememberSaveable {mutableStateOf("")}
 Workspace(account,listOf("Overview","Eventos","Config","Conta")){route,go->
  when(route){
   "home"->Page("Integration Lab") {Hero("SDK / SANDBOX","Resultados explícitos","Teste o contrato. Inspecione a execução. Repita a mesma chave.");Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Metric("Execuções",vm.history.size.toString(),Modifier.weight(1f));Metric("Última latência",(vm.history.firstOrNull()?.elapsed ?: 0).toString()+" ms",Modifier.weight(1f))};ActionTile("Executar um cenário","Aprovação, recusa, falha e timeout",Icons.Default.Science){go("operation")};ActionTile("Event inspector","Correlation ID, resultado e duração",Icons.Default.Terminal){go("activity")};ActionTile("Configurar runtime","Latência do gateway e deadline",Icons.Default.Tune){go("tools")};Text("Biblioteca Kotlin independente do Firebase. A autenticação pertence apenas ao aplicativo sample.")}
   "operation"->Page("Novo experimento",{go("home")}) {OutlinedTextField(amount,{amount=it},label={Text("Valor em reais")},enabled=!ui.busy,modifier=Modifier.fillMaxWidth());DemoMode.entries.forEach {option->FilterChip(selected=mode==option,onClick={mode=option},enabled=!ui.busy,label={Text(option.name)})};Button(onClick={vm.pay(amount,mode);go("result")},enabled=!ui.busy,modifier=Modifier.fillMaxWidth()){Text("Executar gateway")};Text("Timeout: ${vm.timeoutMillis} ms · latência: ${vm.latencyMillis} ms")}
   "result"->Page("Trace da execução",{go("home")}) {StatusPanel(ui.status.takeIf {it!="idle"} ?: "loading",if(ui.busy)"Gateway em execução" else "Resultado recebido",ui.message);if(ui.key.isNotEmpty())Text("Correlation ID: ${ui.key}");if(ui.busy)OutlinedButton(vm::cancel){Text("Cancelar espera")};if(!ui.busy&&ui.key.isNotEmpty()){Button(vm::retry,Modifier.fillMaxWidth()){Text("Repetir com a mesma chave")};OutlinedButton(vm::conflict,Modifier.fillMaxWidth()){Text("Testar conflito de idempotência")}};Text("Cancelar ou perder a resposta não comprova cancelamento financeiro.")}
   "activity"->Page("Event inspector") {if(vm.history.isEmpty())StatusPanel("loading","Nenhum evento","Execute um cenário para iniciar o trace.");vm.history.forEachIndexed {i,event->ActionTile("#${vm.history.size-i} · ${money(event.amount)}",event.result.take(75)+" · ${event.elapsed} ms",Icons.Default.DataObject){selected=event.at.toString()+event.key;go("details")}}}
   "details"->Page("Contrato e telemetria",{go("activity")}) {val e=vm.history.find {it.at.toString()+it.key==selected};if(e!=null){Hero("EXECUTION",money(e.amount),"Duração observada: ${e.elapsed} ms");Text("request.idempotencyKey\n${e.key}");Text("result\n${e.result}");Text("Resultado registrado em ${java.util.Date(e.at)}");Text("Valores e identificadores fictícios. O trace é local e limitado à sessão.")}}
   "tools"->Page("Runtime settings") {Hero("CONFIGURATION","Controle a execução","Timeout e latência são entradas reais do PaymentClient e simulador.");Text("Gateway latency: ${vm.latencyMillis} ms");Slider(vm.latencyMillis.toFloat(),{vm.latencyMillis=it.toLong()},enabled=!ui.busy,valueRange=100f..4000f);Text("Client deadline: ${vm.timeoutMillis} ms");Slider(vm.timeoutMillis.toFloat(),{vm.timeoutMillis=it.toLong()},enabled=!ui.busy,valueRange=500f..5000f);ActionTile("Ver comportamento na prática","Iniciar um novo experimento",Icons.Default.PlayArrow){go("operation")};Text("Retries usam o cliente original e a requisição anterior. Novas configurações se aplicam aos próximos pagamentos.")}
  }
 }
}
