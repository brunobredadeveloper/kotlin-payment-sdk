# Engenharia — Kotlin Payment SDK · Integration Lab

## Problema técnico

Separar um protocolo de pagamento simulado da interface Android e tornar os cenários de falha reproduzíveis para quem integra um SDK.

## Responsabilidades e código

| Fronteira | Responsabilidade real | Onde inspecionar |
|---|---|---|
| API pública | PaymentRequest, PaymentResult e PaymentGateway | [PaymentApi.kt](../sdk/src/main/kotlin/dev/breda/payments/PaymentApi.kt) |
| Orquestração | PaymentClient e idempotência da sessão | [PaymentClient.kt](../sdk/src/main/kotlin/dev/breda/payments/PaymentClient.kt) |
| Infraestrutura de demonstração | Gateway simulado | [SimulatedPaymentGateway.kt](../sdk/src/main/kotlin/dev/breda/payments/SimulatedPaymentGateway.kt) |
| Sample Android | PrepareExperiment, ViewModel e factory | [PaymentExperiments.kt](../sample-android/src/main/kotlin/dev/breda/payments/sample/PaymentExperiments.kt) |

## Invariantes e decisões

- Cancelamento de coroutine precisa ser propagado.
- Timeout é incerteza: um gateway real pode ter concluído a autorização.
- Eventos do sample não constituem histórico persistente de servidor.

## Como examinar os testes

Os arquivos abaixo fazem parte do código versionado. A existência de uma suíte não comprova sua aprovação; o resultado remoto está em [evidências](evidencias.md).

- [sample-android/src/androidTest/kotlin/dev/breda/payments/sample/ExperienceJourneyTest.kt](../sample-android/src/androidTest/kotlin/dev/breda/payments/sample/ExperienceJourneyTest.kt)
- [sample-android/src/test/kotlin/dev/breda/payments/sample/ArchitectureContractTest.kt](../sample-android/src/test/kotlin/dev/breda/payments/sample/ArchitectureContractTest.kt)
- [sample-android/src/test/kotlin/dev/breda/payments/sample/PrepareExperimentTest.kt](../sample-android/src/test/kotlin/dev/breda/payments/sample/PrepareExperimentTest.kt)
- [sample-android/src/test/kotlin/dev/breda/payments/sample/SessionIsolationTest.kt](../sample-android/src/test/kotlin/dev/breda/payments/sample/SessionIsolationTest.kt)
- [sdk/src/test/kotlin/dev/breda/payments/PaymentClientTest.kt](../sdk/src/test/kotlin/dev/breda/payments/PaymentClientTest.kt)

## Limites e compromissos

Não é um gateway de produção nem SDK certificado de adquirência. A idempotência da sessão não substitui garantia remota. Sem publicação declarada no Maven Central; publishToMavenLocal é validação local.

A separação de responsabilidades documentada é a encontrada no código. Não se afirma certificação SOLID, paridade com produtos comerciais, auditoria de segurança ou operação em produção.

## Reproduzir a validação

```sh
./gradlew :sdk:test :sdk:publishToMavenLocal
./gradlew -PincludeAndroid=true :sample-android:testDebugUnitTest :sample-android:lintDebug :sample-android:assembleDebug
```

[Voltar ao README](../README.md) · [Fluxos](fluxos.md)
