# JARVIS 0.1 🤖

Primeira versão do assistente pessoal Android, focada em comandos locais.

## Comandos por voz

- "Que horas são?"
- "Abrir calculadora"
- "Abrir YouTube"
- "Quanto está a bateria?"
- "Abrir configurações"
- "Abrir câmera"
- "Abrir navegador"
- "Abrir galeria"
- "Abrir telefone"
- "Abrir mensagens"
- "Abrir Wi-Fi"
- "Aumentar volume"
- "Diminuir volume"

O JARVIS usa o reconhecimento de voz e a síntese de voz do Android. Os comandos básicos são executados localmente, sem precisar de uma API de IA.

## Como executar

1. Abra o projeto no Android Studio.
2. Aguarde o Gradle sincronizar.
3. Use um emulador Android ou conecte um celular Android por USB ou depuração sem fio.
4. Execute o app.
5. Conceda a permissão de microfone.
6. Toque em "Falar com JARVIS" e diga um dos comandos.

## Arquitetura atual

- Kotlin: lógica do aplicativo.
- XML: interface.
- Android SDK: voz, texto para fala e integração com aplicativos/configurações.
- Sem chave de API e sem dependência de IA externa nesta versão.

## Próximas etapas

- Ativação por voz "Ei, JARVIS".
- Serviço de escuta em segundo plano com consumo controlado.
- Mais comandos do sistema.
- Integração opcional com IA para perguntas complexas.
- Suporte e testes com fones Bluetooth.
