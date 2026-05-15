# Sentinela Cam TV

Sentinela Cam TV e um aplicativo open-source para visualizar cameras de seguranca em Android TV e Google TV.

O projeto esta em desenvolvimento inicial. A prioridade atual e um mosaico leve para dispositivos modestos, especialmente o Izy Play com Android TV 10, 1 GB de RAM e saida 1080p. O app tambem deve funcionar em Google TV mais recentes, mas o alvo principal de performance e o hardware mais limitado.

## Principios

- Sem anuncios.
- Sem rastreamento.
- Sem analytics.
- Sem conta obrigatoria.
- Sem nuvem obrigatoria.
- Codigo aberto sob `GPL-3.0-or-later`.
- Otimizado primeiro para uso com controle remoto em Android TV / Google TV.

## Estado atual

- Mosaico com 5 cameras.
- Reprodução RTSP usando AndroidX Media3/ExoPlayer.
- Configuracao local de DVR Intelbras/MHDX via `local.properties`.
- Estrutura inicial separada em `domain`, `player`, `config` e `ui.mosaic`.

O ambiente real de teste principal usa um DVR Intelbras MHDX 1004 com cinco cameras Intelbras. Quatro cameras sao analogicas via DVR e uma camera e IP/ONVIF exposta pelo sistema.

## Configuracao local

As credenciais do DVR nao ficam no codigo-fonte. Para testar localmente, configure o arquivo `local.properties` na raiz do projeto:

```properties
sdk.dir=C\:\\Users\\SEU_USUARIO\\AppData\\Local\\Android\\Sdk

sentinela.dvr.host=192.0.2.10
sentinela.dvr.username=usuario
sentinela.dvr.password=senha
sentinela.dvr.rtspPort=554
```

`local.properties` e ignorado pelo Git. Nao envie IPs internos, usuarios, senhas ou URLs RTSP reais para o repositorio.

## Desenvolvimento

Requisitos recomendados:

- Android Studio estavel atual.
- JDK incluido no Android Studio.
- Android SDK Platform 36 instalado.
- Dispositivo Android TV/Google TV real para validacao.

Comandos uteis:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

No Windows, se o terminal nao encontrar Java, use temporariamente o JDK do Android Studio:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
```

## Privacidade

Veja [PRIVACY.md](PRIVACY.md).

## Licenca

Sentinela Cam TV e software livre licenciado sob `GPL-3.0-or-later`. Veja [LICENSE](LICENSE).
