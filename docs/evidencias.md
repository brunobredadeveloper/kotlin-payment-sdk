# Evidências e distribuição — Kotlin Payment SDK · Integration Lab

Consulta realizada em **14/10/2026**. Esta página separa o que existe no código, o que foi executado e o que ainda precisa de validação física.

## Execução consultada

- [GitHub Actions #38172931037](https://github.com/brunobredadeveloper/kotlin-payment-sdk/actions/runs/38172931037)
- Revisão executada: `20118bf44ea4477394bc4db0b361d6d9cba209a4`.
- Workflow: **Tests and APK**.
- Conclusão retornada pelo GitHub: **success**.

Alterações documentais posteriores não equivalem a uma nova compilação do aplicativo.

## Artefatos encontrados

- `kotlin-payment-sdk-reports` — ID `11715165490`; disponível na consulta; sujeito à expiração pelo GitHub.
- `kotlin-payment-sdk-ux-evidence` — ID `11714682680`; disponível na consulta; sujeito à expiração pelo GitHub.

Para baixar: abra a execução, vá a **Artifacts**, baixe o ZIP e extraia o conteúdo. O GitHub pode exigir login. APK é para Android; bundle JavaScript, relatório e ZIP de fontes não são instaladores. Builds iOS de simulador ou sem assinatura não instalam diretamente no iPhone.

## Capturas incorporadas

- [03-home.png](imagens/03-home.png) — artefato de testes de interface do aplicativo.
- [05-success.png](imagens/05-success.png) — artefato de testes de interface do aplicativo.
- [06-error.png](imagens/06-error.png) — artefato de testes de interface do aplicativo.

Origem: https://github.com/brunobredadeveloper/kotlin-payment-sdk/actions/runs/38172931037. Os dados exibidos são de demonstração. As capturas foram mantidas sem retoques.

## Avaliação física pendente

Testes automatizados e compilação não substituem avaliação de acessibilidade, interrupções, recursos do aparelho e instalação/distribuição pelo titular. Não é um gateway de produção nem SDK certificado de adquirência. A idempotência da sessão não substitui garantia remota. Sem publicação declarada no Maven Central; publishToMavenLocal é validação local.

[Fluxos e checklist](fluxos.md) · [README](../README.md)
