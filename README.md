# JARVIS 0.1 🤖

Assistente Android pessoal em Kotlin, focado em comandos locais e preparado para evoluir.

## Recursos atuais

- Reconhecimento de voz em português
- Respostas por voz em português
- Hora e data
- Bateria
- Calculadora
- YouTube
- Câmera
- Galeria
- Navegador
- Telefone
- Mensagens
- Wi-Fi
- Controle de volume
- Configurações
- Gerenciador de aplicativos
- Seleção de aplicativos autorizados
- Busca de aplicativos
- Comandos "abrir [app]" e "abre [app]"
- Nomes com ou sem acentos
- Comando de ajuda
- Comando para parar a fala
- Execução local dos comandos básicos, sem API de IA

## APK automático

Cada push na branch `main` dispara o GitHub Actions e gera um APK de debug como artefato.

No GitHub:
**Actions → Build JARVIS APK → jarvis-debug-apk**

Também dá para iniciar manualmente em **Actions → Build JARVIS APK → Run workflow**.

## Como executar localmente

1. Abra o projeto no Android Studio.
2. Aguarde o Gradle sincronizar.
3. Use um emulador ou um celular Android por USB/depuração sem fio.
4. Execute o app.
5. Conceda a permissão de microfone.
6. Toque em "Falar com JARVIS" e diga um comando.

## Arquitetura

- Kotlin: lógica do aplicativo.
- XML: interface.
- Android SDK: voz, texto para fala e integração com aplicativos/configurações.
- Sem chave de API para os comandos básicos.

## Próximas evoluções

- Wake word "Ei, JARVIS"
- Serviço de escuta em segundo plano
- Comandos mais naturais
- Integração opcional com IA online
- Mais automações do Android
- Suporte e testes com fones Bluetooth
