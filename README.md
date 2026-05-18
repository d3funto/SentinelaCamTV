# Sentinela Cam TV

Sentinela Cam TV é um aplicativo open-source para visualizar câmeras de segurança em Android TV e Google TV.

O projeto está em desenvolvimento inicial. A prioridade atual é um mosaico leve para dispositivos modestos, especialmente o Izy Play com Android TV 10, 1 GB de RAM e saída 1080p. O app também deve funcionar em Google TV mais recentes, mas o alvo principal de performance é o hardware mais limitado.

## Princípios

- Sem anúncios.
- Sem rastreamento.
- Sem analytics.
- Sem conta obrigatória.
- Sem nuvem obrigatória.
- Código aberto sob `GPL-3.0-or-later`.
- Otimizado primeiro para uso com controle remoto em Android TV / Google TV.

## Estado atual

- Mosaico com 5 câmeras.
- Tela cheia a partir do mosaico.
- Reprodução RTSP usando AndroidX Media3/ExoPlayer.
- Configuração local de DVR Intelbras/MHDX via `local.properties` em builds de desenvolvimento.
- Estrutura inicial com `domain`, `player`, `preferences`, `data`, `ui`, Room, DataStore e um módulo ONVIF isolado.

O ambiente real de teste principal usa um DVR Intelbras MHDX 1004 com cinco câmeras Intelbras. Quatro câmeras são analógicas via DVR e uma câmera é IP/ONVIF exposta pelo sistema.

## Configuração local

As credenciais do DVR não ficam no código-fonte. Para testar localmente, configure o arquivo `local.properties` na raiz do projeto:

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
