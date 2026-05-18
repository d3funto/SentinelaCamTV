# Privacidade

Sentinela Cam TV foi pensado para uso local e privado.

## O que o app não faz

- Não exibe anúncios.
- Não usa analytics.
- Não possui rastreadores.
- Não cria conta de usuário.
- Não envia telemetria para servidores do projeto.
- Não depende de nuvem para visualizar câmeras locais.

## Dados usados pelo app

Durante o desenvolvimento, o app usa dados locais de conexão com DVR/câmeras para montar URLs RTSP. Esses dados podem incluir host/IP, porta, usuário e senha.

Essas informações devem ficar apenas no dispositivo do usuário ou no ambiente local de desenvolvimento. Elas não devem ser enviadas para o repositório.

## Rede

O app precisa da permissão de internet/rede para conectar ao DVR ou às câmeras RTSP na rede local. Essa permissão não implica envio de dados para terceiros.

## Observação sobre versões de desenvolvimento

Builds de desenvolvimento podem incluir configurações locais injetadas via `BuildConfig` para facilitar testes. Não distribua APKs gerados com credenciais reais.
