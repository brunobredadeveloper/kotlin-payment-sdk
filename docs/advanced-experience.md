# Roteiro da experiência avançada

Execute aprovação, repita a mesma chave, provoque conflito e confira o resultado tipado. Execute timeout e cancelamento. Ajuste os sliders e observe duração e deadline nos eventos.

## Autenticação e estados

- Inicie sem configuração e confirme identificação de demo local.
- Com Firebase configurado: cadastre conta, saia, teste senha incorreta, entre, recupere senha e envie verificação.
- Reinicie para verificar restauração da sessão.
- Teste sem rede e confira mensagem de erro e liberação do formulário.
- Teste voltar, rotação, texto grande, teclado aberto, carregamento e navegação pelas quatro abas.
- Troque de usuário e confirme ausência dos dados do anterior.

## Limites

O SDK continua Kotlin puro, sem Firebase ou Android. O sample permite configurar latência/deadline, aprovação, recusa, erro, timeout, cancelamento, replay de chave e conflito de payload. Eventos ficam em memória durante a sessão; o sample não é um servidor de pagamentos.

Firebase e interação visual aguardam teste manual com configuração pessoal e celular. Não existe certificação para aceitação de cartões, pagamento real ou armazenamento de dados bancários.

## Rodada visual 0.3

- Logo e ícone adaptativo breda_dev, mesmo monograma em todos os apps.
- Splash nativo + Lottie contextual; abertura dura 3,2 s no lançamento.
- Feedback de processamento/sucesso/erro em tela inteira; os erros oferecem recuperação.
- Testes instrumentados de navegação e renderização Lottie no emulador, com capturas no CI.
- Confira animações também no celular: opções do Android para remover animações ou economia de energia podem reduzir o movimento.
