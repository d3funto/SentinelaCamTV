# Sentinela Cam TV

Sentinela Cam TV é um aplicativo open-source para visualizar câmeras de segurança em Android TV e Google TV.

O foco do projeto é monitoramento local, sem anúncios, sem rastreamento, sem telemetria e sem dependência de nuvem. A prioridade técnica é rodar bem em TVs e TV Boxes modestas, especialmente aparelhos com cerca de 1 GB de RAM.

## Princípios

- Sem anúncios.
- Sem rastreamento.
- Sem analytics.
- Sem conta obrigatória.
- Sem nuvem obrigatória.
- Código aberto sob `GPL-3.0-or-later`.
- Otimizado para controle remoto em Android TV / Google TV.

## Estado atual

- Mosaico de câmeras.
- Tela cheia a partir do mosaico.
- Reprodução RTSP usando AndroidX Media3/ExoPlayer.
- Cadastro por RTSP direto.
- Base inicial para descoberta ONVIF.
- Logs locais para suporte.
- Atualização manual via GitHub Releases.

## Configuração local de desenvolvimento

As credenciais de desenvolvimento não ficam no código-fonte. Para testar localmente, configure o arquivo `local.properties` na raiz do projeto:

```properties
sdk.dir=C\:\\Users\\SEU_USUARIO\\AppData\\Local\\Android\\Sdk

sentinela.dvr.host=192.0.2.10
sentinela.dvr.username=usuario
sentinela.dvr.password=senha
sentinela.dvr.rtspPort=554
```

`local.properties` é ignorado pelo Git. Não envie IPs internos, usuários, senhas ou URLs RTSP reais para o repositório.

## Desenvolvimento

Requisitos recomendados:

- Android Studio estável atual.
- JDK incluído no Android Studio.
- Android SDK Platform 36 instalado.
- Dispositivo Android TV/Google TV real para validação.

Comandos úteis:

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon --max-workers=1
.\gradlew.bat :app:assembleDebug --no-daemon --max-workers=1
```

No Windows, se o terminal não encontrar Java, use temporariamente o JDK do Android Studio:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
```

## Privacidade

Veja [PRIVACY.md](PRIVACY.md).

## Licença

Sentinela Cam TV é software livre licenciado sob `GPL-3.0-or-later`. Veja [LICENSE](LICENSE).
