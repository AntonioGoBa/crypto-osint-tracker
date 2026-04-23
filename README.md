# Crypto OSINT Tracker

Proyecto centrado unicamente en Blockstream para trazado de wallets BTC.

## Proveedor de datos

La API consulta directamente:

- `https://blockstream.info/api/address/{address}/txs`

No se usan API keys asociadas.

## Arrancar la aplicacion

```powershell
mvn spring-boot:run
```

## Logs separados para trafico HTTP saliente

La aplicacion escribe dos logs:

- log general: `logs/crypto-osint-tracker.log`
- log HTTP saliente (`RestTemplate`): `logs/http-traffic.log`

En el log HTTP se registran request/response completos con tiempo de ejecucion. El body se trunca segun `http.logging.max-body-length` en `application.yml`.

## Verificacion rapida

```powershell
Invoke-RestMethod "http://localhost:8080/api/trace/1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa?depth=2"

Invoke-RestMethod "http://localhost:8080/api/trace/1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa/blocks"
```

El endpoint `/api/trace/{wallet}/blocks` devuelve:

- `firstSeenBlock`: primer bloque confirmado donde aparece la wallet
- `lastSeenBlock`: ultimo bloque confirmado donde aparece la wallet
- `confirmedTxCount` y `unconfirmedTxCount`
- `transactions`: detalle por transaccion (`txid`, bloque, timestamp y ocurrencias de la wallet)

## Visualizacion con D3.js

Se incluye una vista web para explorar el grafo de forma visual:

- URL: `http://localhost:8080/trace.html`

Uso rapido:

1. Introduce la `Wallet inicial`.
2. Ajusta `Depth` y filtros (`Filtro minimo`, `Top N`, `Maximo de aristas`).
3. Pulsa `Cargar grafo`.
4. Navega el grafo con zoom (rueda) y arrastre (drag de nodos).

Controles disponibles:

- `Wallet inicial`
- `Depth`
- `Filtro minimo de arista (satoshis)`
- `Maximo de aristas a dibujar`
- `Aplicar Top N por importe`
- `Ocultar nodos sin saldo`
- `Exportar JSON`
- `Exportar CSV`
- `Exportar PNG`

La vista dibuja:

- nodos con color por balance (positivo/negativo/cero)
- wallet inicial resaltada
- aristas con grosor proporcional al importe
- tooltip con valores en satoshis y BTC

Al seleccionar un nodo (clic), se muestra un panel con:

- wallet completa
- balance del nodo en satoshis y BTC
- numero y total de aristas entrantes/salientes
- top de origenes y destinos por importe

Al seleccionar una arista (clic en la linea), el panel muestra:

- origen y destino
- importe en satoshis y BTC
- txCount y lista de txids (muestra inicial + resumen)
- peso relativo sobre el total de aristas visibles
- enlaces directos a Blockstream por txid
- boton para copiar txids al portapapeles

La visualizacion usa D3.js (force-directed graph), con zoom por rueda y arrastre de nodos para inspeccionar relaciones.

Las aristas se agregan por par `origen -> destino` para evitar duplicados visuales.

### Exportacion desde la vista

La vista permite exportar el grafo visible (con filtros aplicados) con:

- `Exportar JSON`: genera `trace_<wallet>.json`
- `Exportar CSV`: genera `trace_<wallet>.csv`
- `Exportar PNG`: genera `trace_<wallet>.png`

Las exportaciones JSON/CSV incluyen metadatos de arista (`txCount`, `txids`) cuando estan disponibles.

El CSV incluye tres bloques:

- `summary` (wallet inicial y balance)
- `node` (nodos y balances)
- `edge` (origen, destino e importes)

## Formato de respuesta

El endpoint devuelve:

- monedero inicial y su cantidad (`initialWallet`, `initialAmountSatoshis`, `initialAmountBtc`)
- nodos visitados con su cantidad (`nodes`)
- transferencias con origen, destino e importe (`edges`)

Ejemplo simplificado:

```json
{
  "initialWallet": "1abc...",
  "initialAmountSatoshis": 900970,
  "initialAmountBtc": "0.00900970",
  "nodes": [
	{
	  "wallet": "1abc...",
	  "amountSatoshis": 900970,
	  "amountBtc": "0.00900970"
	}
  ],
  "edges": [
	{
	  "from": "1abc...",
	  "to": "1dest...",
	  "amount": 50000,
	  "amountBtc": "0.00050000"
	}
  ]
}
```


