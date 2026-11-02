# Master IPTV para LG webOS

App web (HTML/JS) com **TV ao vivo, filmes e séries**, navegação por controle remoto (D-pad e Magic Remote).

## Login
Servidor, usuário e senha (Xtream Codes). **http ou https:** se o endereço digitado não conectar, o app tenta sozinho o outro (http↔https, ou https quando você digita sem prefixo) e guarda o que funcionou, avisando "Servidor ajustado". Usuário/senha errados não disparam essa segunda tentativa. Se algo der errado, os campos continuam preenchidos para você só corrigir o que errou.

## Telas
- **Início (estilo Netflix):** no topo, o botão de **três traços** à esquerda, o nome **Master IPTV** no centro e o **perfil** à direita. Logo abaixo, as abas **TV ao vivo**, **Filmes** e **Séries**, e depois as fileiras Continuar assistindo, **Favoritos** (só aparece se você favoritou algo; o último item, "Ver todos", abre a lista completa), Canais recentes e recomendações.
- **Estilo da tela inicial:** nas TVs (webOS e Android TV) a Início abre com **blocos grandes** (TV ao vivo, Filmes, Séries); no celular, com as **abas** do topo. Dá para trocar em Configurações → *Estilo da tela inicial* (vale só para aquele aparelho).
- **Menu lateral (três traços):** mostra o **clima** da sua cidade, o relógio, a validade da conta e o botão **Configurações**. **Voltar** fecha o menu.
- **Cada seção mostra só o seu conteúdo**, com categorias na coluna da esquerda e um botão **Buscar** que pesquisa só dentro dela. **Voltar** sobe um nível (lista → categorias → menu); o botão **Menu** do topo volta direto.
- **TV ao vivo:** lista em duas colunas, com o **programa atual** (horário de início e fim), a barra de progresso e o **próximo programa** em cada canal; canais sem guia mostram "Sem programação". Os nomes das categorias aparecem sem o prefixo repetido (ex.: "Canais | Esportes" vira "Esportes").
- **Configurações** (pelo menu lateral): dados da conta, **editar servidor**, chaves para **reproduzir o último conteúdo ao abrir o app**, **ocultar canais SD** e **ocultar canais HD** (FHD e 4K continuam), e a **cidade do clima** (busca pelo nome).
- **Perfis:** além da cor, cada perfil pode ter um **ícone** de avatar (carinha, gato, patinha, foguete, coroa, joystick…) ou a inicial do nome. Pelo menu de perfil (avatar no canto) → *Gerenciar perfis* → editar.
- **Mini player:** com o controle remoto só de setas (Android TV, sem botões coloridos), navegue para a direita até o mini player: **Ampliar** (à esquerda) e **Fechar** (à direita), e aperte OK. Nos controles com Amarelo/Azul, esses botões continuam valendo.
- **Player:** mostra a **resolução real** do vídeo (ex.: 1920×1080 · FHD).
- **Gestos no player (app Android, celular):** arraste o dedo para cima/baixo na **metade esquerda** para mudar o **brilho** (levando até o mínimo, ele volta ao **automático**, o brilho do sistema) e na **direita** para mudar o **volume**; em filmes e episódios, arraste **para os lados** para avançar ou voltar (a tela mostra quanto). O brilho volta ao normal ao sair do player.

## Esportes (placares)
Na Início, **Esportes** abre a central de jogos de futebol: **Brasileirão, Copa do Brasil, Libertadores, Sul-Americana, Champions, Premier League, La Liga, Ligue 1 e Campeonato Italiano**, e de seleções **Eliminatórias, Copa América, Eurocopa, Nations League e Copa do Mundo**; os **amistosos** aparecem só quando envolvem o Brasil ou um time que você segue.
- **Na Início:** uma fileira **Esportes · jogos de hoje** mostra os jogos ao vivo primeiro e depois os das próximas horas, por horário; o que já terminou sai da lista. OK abre o jogo (o último cartão leva à central de Esportes).
- **Placares:** jogos de ontem a daqui a 6 dias (um dia por consulta, guardado em memória), agrupados por liga, com **Ao vivo agora** no topo (minuto e placar atualizam sozinhos a cada 30 s). OK em um jogo abre gols, estatísticas (posse, finalizações, escanteios, faltas, cartões) e **onde assistir**.
- **Onde assistir:** o app procura, na programação (EPG) dos seus canais de esporte e abertos, qual deles passa o jogo (pelo nome dos times) e mostra o canal no cartão; OK no botão sintoniza.
- **Meus times:** em um jogo, **Seguir** no time; ele ganha uma estrela e a aba mostra só os jogos dele.
- **Nos meus canais:** só os jogos encontrados na programação; OK sintoniza o melhor canal.
- Fonte: API pública de placares da ESPN (sem chave, não oficial). Se ela mudar ou sair do ar, a tela avisa e o resto do app segue normal. O cruzamento com o canal depende de como o seu provedor escreve os títulos.

## Downloads para assistir offline (Android, celular)
Em cada filme há o botão **Baixar**; em séries, um botão por episódio e **Baixar temporada**. O download usa o gerenciador do Android (segue com a tela apagada, mostra o progresso na notificação e retoma se a conexão cair) e, por padrão, **só no Wi-Fi**. Os arquivos ficam dentro do app (somem se o app for desinstalado). A tela **Downloads** (menu de três traços) mostra o andamento, o espaço livre e deixa assistir ou apagar; o que já foi baixado toca direto do celular e o ponto de *continuar assistindo* continua sendo salvo. **Sem internet** ao abrir, o app entra direto nos downloads. Limites: TV ao vivo e itens em .m3u8 não baixam, e cada download usa uma das conexões simultâneas do servidor IPTV.

## Sincronizar TV e celular
Configurações → **Sincronização** mantém *continuar assistindo*, favoritos e histórico iguais nos aparelhos, usando um servidor gratuito no Cloudflare Workers (`worker/sync-worker.mjs`). Passo a passo em [docs/sincronizacao.md](docs/sincronizacao.md).

## Controles
| Tecla | Ao vivo | Filmes/séries |
|---|---|---|
| ▲ ▼ / CH+ CH- | trocar canal | ▲ ou ▼ abre o menu de baixo (com *Áudio e legenda*) |
| 0–9 | ir ao canal pelo número | – |
| ◀ ▶ | mostra barra | ±10 s (acelera segurando) |
| OK | mostra barra | pausar/continuar |
| ⏪ ⏩ | – | ±30 s |
| Verde | áudio e legenda | áudio e legenda |
| Vermelho | favoritar canal | – (na lista: favoritar item) |
| Voltar | sai do player | sai do player (salva o ponto) |

Filmes e episódios retomam de onde parou; ao terminar um episódio, o próximo toca sozinho.

## Áudio e legenda
No player, em filme/série aperte ▲ ou ▼ para abrir o menu de baixo (título e barra de progresso): a opção **Áudio e legenda** aparece destacada, e **OK** abre o painel (na TV ao vivo, **OK** mostra o menu e **OK** de novo abre o painel; ▲ ▼ trocam de canal). Também vale a tecla **Verde** ou clicar no botão com o Magic Remote abre o painel com as faixas de áudio e as legendas embutidas. O idioma escolhido fica salvo e é aplicado nos próximos conteúdos que tiverem a mesma faixa. A lista vem do que o player da TV (ou o hls.js) expõe; se o conteúdo só tem uma faixa, o painel avisa. O rodapé do painel mostra o motor em uso e quantas faixas foram encontradas, útil para diagnóstico.

## Instalar (TV com root / modo desenvolvedor)
```
npm i -g @webos-tools/cli
ares-setup-device            # adicione a TV (root: porta 22; modo dev: porta 9922)
ares-install -d <nome-da-tv> dist/com.rodrigo.iptv_1.4.0_all.ipk
ares-launch  -d <nome-da-tv> com.rodrigo.iptv
```
Com root + Homebrew Channel também dá para copiar o `.ipk` para a TV e instalar por lá.

## Recompilar
```
npm install
node tests/test-core.js && node tests/test-sync.js && node tests/test-sports.js && node tests/test-dl.js && node tests/test-ui.js
npx ares-package app services --no-minify -o dist
```

## Como funciona a rede
O app tenta o servidor direto. Muitos painéis não enviam CORS; nesse caso ele usa o serviço
`com.rodrigo.iptv.service`, um proxy em `127.0.0.1:18080` (protegido por token) que só repassa
as chamadas de API/lista. O vídeo em si é tocado direto pelo `<video>` da TV
(HLS nativo; `.ts` nativo; hls.js/mpegts.js como reserva).

## Limites conhecidos
- Testado em Node/jsdom (parser, API, proxy e fluxo de telas); **ainda não testado numa TV real**.
- Catálogos enormes em "Todos"/Buscar demoram na primeira carga.
- EPG só no modo Xtream (não lê XMLTV de listas M3U).

## Atualizar pela TV (Homebrew Channel)
O repositório precisa ser **público**. Ao criar uma etiqueta (`git tag v1.4.1 && git push origin v1.4.1`), o workflow roda os testes, monta o IPK e cria a Release com `.ipk`, `repo.json`, descrição e ícone (a etiqueta tem de ser `v` + a versão do `appinfo.json`, ex.: versão 1.4.1 → `v1.4.1`). Na TV: Homebrew Channel → Settings → Add repository →
`https://github.com/SEU_USUARIO/NOME_DO_REPO/releases/latest/download/repo.json`
Lembre de subir `version` em `app/appinfo.json` antes de cada etiqueta.
