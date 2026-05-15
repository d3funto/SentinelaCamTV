# Privacidade

Sentinela Cam TV foi pensado para uso local e privado.

## O que o app nao faz

- Nao exibe anuncios.
- Nao usa analytics.
- Nao possui rastreadores.
- Nao cria conta de usuario.
- Nao envia telemetria para servidores do projeto.
- Nao depende de nuvem para visualizar cameras locais.

## Dados usados pelo app

Durante o desenvolvimento, o app usa dados locais de conexao com DVR/cameras para montar URLs RTSP. Esses dados podem incluir host/IP, porta, usuario e senha.

Essas informacoes devem ficar apenas no dispositivo do usuario ou no ambiente local de desenvolvimento. Elas nao devem ser enviadas para o repositorio.

## Rede

O app precisa da permissao de internet/rede para conectar ao DVR ou as cameras RTSP na rede local. Essa permissao nao implica envio de dados para terceiros.

## Observacao sobre versoes de desenvolvimento

Builds de desenvolvimento podem incluir configuracoes locais injetadas via `BuildConfig` para facilitar testes. Nao distribua APKs gerados com credenciais reais.
