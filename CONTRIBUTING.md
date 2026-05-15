# Contribuindo

Obrigado pelo interesse em contribuir com o Sentinela Cam TV.

## Prioridades do projeto

- Android TV / Google TV primeiro.
- Controle remoto e foco por D-pad como caminho principal de uso.
- Desempenho aceitavel em dispositivos modestos, especialmente Android TV com 1 GB de RAM.
- Sem anuncios, sem rastreio e sem dependencias desnecessarias.
- Mudancas pequenas, testaveis e alinhadas com o objetivo local-first do app.

## Antes de enviar codigo

- Nao inclua credenciais, IPs internos, URLs RTSP reais, chaves, keystores ou arquivos de assinatura.
- Rode `testDebugUnitTest` quando alterar logica.
- Rode `assembleDebug` quando alterar Gradle, Manifest, player ou UI principal.
- Teste em dispositivo Android TV/Google TV real quando mexer em foco, player ou mosaico.

## Estilo tecnico

- Prefira Kotlin simples e legivel.
- Mantenha responsabilidades separadas entre `domain`, `player`, `config` e `ui`.
- Evite adicionar bibliotecas sem necessidade clara.
- Para UI de TV, priorize componentes e padroes adequados a Android TV/Google TV.
- Otimize primeiro para estabilidade e uso repetido, depois para recursos extras.

## Licenca

Ao contribuir, voce concorda que sua contribuicao sera distribuida sob `GPL-3.0-or-later`.
