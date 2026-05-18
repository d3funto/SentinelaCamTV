# Contribuindo

Obrigado pelo interesse em contribuir com o Sentinela Cam TV.

## Prioridades do projeto

- Android TV / Google TV primeiro.
- Controle remoto e foco por D-pad como caminho principal de uso.
- Desempenho aceitável em dispositivos modestos, especialmente Android TV com 1 GB de RAM.
- Sem anúncios, sem rastreio e sem dependências desnecessárias.
- Mudanças pequenas, testáveis e alinhadas com o objetivo local-first do app.

## Antes de enviar código

- Não inclua credenciais, IPs internos, URLs RTSP reais, chaves, keystores ou arquivos de assinatura.
- Rode `testDebugUnitTest` quando alterar lógica.
- Rode `assembleDebug` quando alterar Gradle, Manifest, player ou UI principal.
- Teste em dispositivo Android TV/Google TV real quando mexer em foco, player ou mosaico.

## Estilo técnico

- Prefira Kotlin simples e legível.
- Mantenha responsabilidades separadas entre `domain`, `player`, `config` e `ui`.
- Evite adicionar bibliotecas sem necessidade clara.
- Para UI de TV, priorize componentes e padrões adequados a Android TV/Google TV.
- Otimize primeiro para estabilidade e uso repetido, depois para recursos extras.

## Licença

Ao contribuir, você concorda que sua contribuição será distribuída sob `GPL-3.0-or-later`.
