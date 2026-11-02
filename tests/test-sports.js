/* Testes do módulo de esportes: leitura da fonte, filtro de amistosos e casamento do jogo com a programação. */
var assert = require('assert');
var Sports = require('../app/js/sports.js');
var n = 0;
function ok(c, m) { assert.ok(c, m); n++; }

function ev(date, state, h, a, hs, as) {
  return { id: '1', date: date, status: { displayClock: "10'", type: { state: state, name: 'X', shortDetail: '', completed: state === 'post' } },
    competitions: [{ competitors: [{ homeAway: 'home', team: h, score: hs }, { homeAway: 'away', team: a, score: as }] }] };
}
function t(id, name, ab) { return { id: id, displayName: name, shortDisplayName: name, abbreviation: ab, logo: 'http://x/l.png' }; }

var g = Sports.parseEvent(ev('2026-09-20T19:00Z', 'in', t('874', 'Corinthians', 'COR'), t('3445', 'Fluminense', 'FLU'), '1', '3'), 'bra.1');
ok(g && g.home.name === 'Corinthians' && g.away.score === '3' && g.state === 'in' && g.leagueName === 'Brasileirão', 'lê o jogo');
ok(Sports.statusText(g) === "AO VIVO 10'", 'texto do estado ao vivo');
ok(Sports.parseEvent({ id: '9', date: 'lixo', competitions: [] }, 'bra.1') === null, 'ignora jogo sem data/times');

// amistosos: só do Brasil ou de times seguidos
var bra = Sports.parseEvent(ev('2026-09-20T19:00Z', 'pre', t('205', 'Brazil', 'BRA'), t('654', 'Senegal', 'SEN'), '0', '0'), 'fifa.friendly');
var fra = Sports.parseEvent(ev('2026-09-20T19:00Z', 'pre', t('160', 'France', 'FRA'), t('164', 'Spain', 'ESP'), '0', '0'), 'fifa.friendly');
ok(Sports.visible(bra, {}) && !Sports.visible(fra, {}) && Sports.visible(fra, { '160': 1 }), 'filtro de amistosos');
ok(Sports.visible(g, {}), 'ligas normais sempre aparecem');

// casamento com a programação (acentos, nomes em português, confronto)
var pg = Sports.parseEvent(ev('2026-09-20T19:00Z', 'pre', t('2029', 'Palmeiras', 'PAL'), t('6273', 'Grêmio', 'GRE'), '0', '0'), 'bra.1');
ok(Sports.matchText(pg, 'Brasileirão 2026: Palmeiras x Gremio - Ao vivo') === 2, 'dois times, sem acento');
ok(Sports.matchText(pg, 'Futebol: Palmeiras x Bahia') === 1, 'um time com confronto');
ok(Sports.matchText(pg, 'Jornal do Palmeiras') === 0, 'um time sem confronto não vale');
ok(Sports.matchText(pg, 'Novela das nove') === 0, 'sem relação');
ok(Sports.matchText(bra, 'Amistoso: Brasil x Senegal') === 2, 'Brazil -> Brasil');
var atl = Sports.parseEvent(ev('2026-09-20T19:00Z', 'pre', t('1', 'Atlético-MG', 'CAM'), t('2', 'Athletico-PR', 'CAP'), '0', '0'), 'bra.1');
ok(Sports.matchText(atl, 'Cruzeiro x Atlético-MG') === 1 && Sports.matchText(atl, 'Atlético-MG x Athletico-PR') === 2, 'nomes parecidos de clubes');

// escolha de canais e ligação
var channels = [
  { id: '1', name: 'SporTV 2', epgId: 'a', cat: '9' }, { id: '2', name: 'Canal de Filmes', epgId: 'b', cat: '3' },
  { id: '3', name: 'Globo SP', epgId: 'c', cat: '4' }, { id: '4', name: 'Sem guia', epgId: '', cat: '9' }
];
var cats = [{ id: '9', name: 'Canais | Esportes' }, { id: '3', name: 'Filmes 24h' }, { id: '4', name: 'Abertos' }];
var cand = Sports.candidateChannels(channels, cats, 10);
ok(cand.map(function (c) { return c.id; }).join() === '1,3', 'só canais de esporte/abertos com guia: ' + cand.map(function (c) { return c.id; }));
var now = Date.now(), live = Sports.parseEvent(ev(new Date(now - 1800000).toISOString(), 'in', t('2029', 'Palmeiras', 'PAL'), t('6273', 'Grêmio', 'GRE'), '1', '0'), 'bra.1');
var res = Sports.assign([live], cand, {
  '1': [{ title: 'Palmeiras x Grêmio', desc: '', start: now - 3600000, stop: now + 3600000 }],
  '3': [{ title: 'Jornal Nacional', desc: '', start: now - 3600000, stop: now + 3600000 }]
});
var hits = res[live.league + ':' + live.id];
ok(hits && hits.length === 1 && hits[0].ch.id === '1', 'liga o jogo ao canal certo');
var late = Sports.assign([live], cand, { '1': [{ title: 'Palmeiras x Grêmio', desc: '', start: now + 9 * 3600000, stop: now + 11 * 3600000 }] });
ok(!late[live.league + ':' + live.id], 'programa de outro horário não vale');

// busca: uma liga que falha não derruba as outras
var calls = [];
function fakeHttp(url) {
  calls.push(url);
  if (/uefa\.champions/.test(url)) { return Promise.reject(new Error('fora')); }
  if (/bra\.1/.test(url)) { return Promise.resolve(JSON.stringify({ events: [ev('2026-09-20T19:00Z', 'pre', t('1', 'A', 'A'), t('2', 'B', 'B'), '0', '0')] })); }
  return Promise.resolve('{"events":[]}');
}
Sports.fetch(fakeHttp, { from: new Date(2026, 8, 19), to: new Date(2026, 8, 26) }).then(function (r) {
  ok(r.games.length === 1 && r.failed.length === 1 && r.failed[0] === 'uefa.champions', 'falha isolada por liga');
  ok(calls.length === Sports.LEAGUES.length && /dates=20260919-20260926/.test(calls[0]), 'uma consulta por liga, com o intervalo de datas');
  console.log('todos os testes passaram (' + n + ')');
}).catch(function (e) { console.error('  FAIL ' + (e && e.stack || e)); process.exit(1); });
