(function () {
    const svg = d3.select("#graph");
    const statusEl = document.getElementById("status");
    const metaEl = document.getElementById("meta");
    const nodeDetailsEl = document.getElementById("nodeDetails");

    const walletInput = document.getElementById("wallet");
    const depthInput = document.getElementById("depth");
    const minAmountInput = document.getElementById("minAmount");
    const maxEdgesInput = document.getElementById("maxEdges");
    const topNEdgesInput = document.getElementById("topNEdges");
    const hideZeroBalanceInput = document.getElementById("hideZeroBalance");
    const loadBtn = document.getElementById("loadBtn");
    const exportJsonBtn = document.getElementById("exportJsonBtn");
    const exportCsvBtn = document.getElementById("exportCsvBtn");
    const exportPngBtn = document.getElementById("exportPngBtn");

    let width = window.innerWidth - 320;
    let height = window.innerHeight;
    let lastVisualGraph = null;
    let lastPayload = null;
    let selectedNodeId = null;
    let selectedEdgeKey = null;
    let isLoading = false;

    function setStatus(text, isError) {
        statusEl.textContent = text || "";
        statusEl.style.color = isError ? "#f87171" : "#fcd34d";
    }

    function setLoadingState(loading) {
        isLoading = loading;
        loadBtn.disabled = loading;
        exportJsonBtn.disabled = loading;
        exportCsvBtn.disabled = loading;
        exportPngBtn.disabled = loading;
    }

    function satoshisToBtcString(amountSatoshis) {
        const sign = amountSatoshis < 0 ? "-" : "";
        const abs = Math.abs(amountSatoshis);
        const btc = (abs / 100000000).toFixed(8);
        return sign + btc;
    }

    function clearGraph() {
        svg.selectAll("*").remove();
    }

    function edgeEndpoint(value) {
        return typeof value === "object" && value !== null ? value.id : value;
    }

    function edgeKey(source, target) {
        return edgeEndpoint(source) + "->" + edgeEndpoint(target);
    }

    const EDGE_DIRECTION_LABELS = {
        outgoing: "Salida desde la wallet inicial",
        incoming: "Entrada hacia la wallet inicial",
        neutral: "Relación entre wallets"
    };

    function edgeDirection(link, initialWallet) {
        const from = edgeEndpoint(link.source);
        const to = edgeEndpoint(link.target);
        const initial = typeof initialWallet === "string" ? initialWallet.trim() : "";

        if (initial && from === initial) {
            return "outgoing";
        }
        if (initial && to === initial) {
            return "incoming";
        }
        return "neutral";
    }

    function edgeDirectionLabel(direction) {
        return EDGE_DIRECTION_LABELS[direction] || EDGE_DIRECTION_LABELS.neutral;
    }

    const EDGE_DIRECTION_COLORS = {
        outgoing: "#22c55e",
        incoming: "#ef4444",
        neutral: "#60a5fa",
        selected: "#facc15"
    };

    function edgeAppearance(link, initialWallet, selectedEdgeKey) {
        const selected = edgeKey(link.source, link.target) === selectedEdgeKey;
        const direction = edgeDirection(link, initialWallet);

        return {
            direction: direction,
            color: selected ? EDGE_DIRECTION_COLORS.selected : (EDGE_DIRECTION_COLORS[direction] || EDGE_DIRECTION_COLORS.neutral),
            opacity: selected ? 1 : 0.95,
            markerId: selected ? "arrow-" + direction + "-selected" : "arrow-" + direction
        };
    }

    function linkEndpoints(link, nodeRadius) {
        const sourceX = link.source.x;
        const sourceY = link.source.y;
        const targetX = link.target.x;
        const targetY = link.target.y;

        const dx = targetX - sourceX;
        const dy = targetY - sourceY;
        const distance = Math.sqrt(dx * dx + dy * dy) || 1;

        const sourcePadding = nodeRadius(Math.abs(link.source.amountSatoshis || 0)) + 4;
        const targetPadding = nodeRadius(Math.abs(link.target.amountSatoshis || 0)) + 10;

        return {
            x1: sourceX + (dx * sourcePadding) / distance,
            y1: sourceY + (dy * sourcePadding) / distance,
            x2: targetX - (dx * targetPadding) / distance,
            y2: targetY - (dy * targetPadding) / distance
        };
    }

    function normalizeTxids(value) {
        if (!Array.isArray(value)) {
            return [];
        }
        const unique = new Set();
        for (const txid of value) {
            if (typeof txid === "string" && txid.trim().length > 0) {
                unique.add(txid.trim());
            }
        }
        return Array.from(unique);
    }

    async function copyTextToClipboard(text) {
        if (!text || text.length === 0) {
            return false;
        }

        if (navigator.clipboard && navigator.clipboard.writeText) {
            await navigator.clipboard.writeText(text);
            return true;
        }

        const textarea = document.createElement("textarea");
        textarea.value = text;
        textarea.style.position = "fixed";
        textarea.style.left = "-9999px";
        document.body.appendChild(textarea);
        textarea.select();
        const ok = document.execCommand("copy");
        document.body.removeChild(textarea);
        return ok;
    }

    function setNodeDetailsPlaceholder(text) {
        if (!nodeDetailsEl) {
            return;
        }

        nodeDetailsEl.innerHTML =
            "<h2>Detalle de nodo</h2>" +
            "<div class=\"muted\">" + text + "</div>";
    }

    function renderEdgeDetails(link, graph) {
        if (!nodeDetailsEl || !link || !graph) {
            return;
        }

        const from = edgeEndpoint(link.source);
        const to = edgeEndpoint(link.target);
        const direction = edgeDirection(link, graph.initialWallet);
        const amount = Number(link.amount || 0);
        const amountBtc = link.amountBtc || satoshisToBtcString(amount);
        const txids = normalizeTxids(link.txids);
        const txCount = Number(link.txCount || txids.length || 0);

        const total = graph.links.reduce((sum, l) => sum + Number(l.amount || 0), 0);
        const percent = total > 0 ? ((amount * 100) / total).toFixed(2) : "0.00";

        const shownTxids = txids.slice(0, 10);
        const txidsHtml = shownTxids.length > 0
            ? shownTxids.map((txid) => {
                const shortTxid = txid.slice(0, 10) + "..." + txid.slice(-8);
                const txUrl = "https://blockstream.info/tx/" + encodeURIComponent(txid);
                return "<a href=\"" + txUrl + "\" target=\"_blank\" rel=\"noopener noreferrer\" style=\"color:#93c5fd;\">" + shortTxid + "</a>";
            }).join("<br>")
            : "-";
        const txidsMore = txids.length > shownTxids.length
            ? "<div class=\"muted\" style=\"margin-top:4px;\">... y " + (txids.length - shownTxids.length) + " txids mas</div>"
            : "";

        const copyButtonHtml = txids.length > 0
            ? "<button id=\"copyTxidsBtn\" type=\"button\" style=\"margin-top:6px;width:100%;padding:6px;border:0;border-radius:4px;background:#1e293b;color:#e2e8f0;cursor:pointer;\">Copiar txids</button>"
            : "";

        nodeDetailsEl.innerHTML =
            "<h2>Detalle de arista</h2>" +
            "<div><strong>Origen:</strong> " + from + "</div>" +
            "<div><strong>Destino:</strong> " + to + "</div>" +
            "<div><strong>Dirección:</strong> " + edgeDirectionLabel(direction) + "</div>" +
            "<div><strong>Importe:</strong> " + amount + " sats (" + amountBtc + " BTC)</div>" +
            "<div><strong>Grosor:</strong> mayor importe agregado = línea más gruesa</div>" +
            "<div><strong>Tx count:</strong> " + txCount + "</div>" +
            "<div><strong>Peso relativo:</strong> " + percent + "% del total de aristas visibles</div>" +
            "<div style=\"margin-top:6px;\"><strong>Txids:</strong><br>" + txidsHtml + txidsMore + copyButtonHtml + "</div>";

        if (txids.length > 0) {
            const copyBtn = document.getElementById("copyTxidsBtn");
            if (copyBtn) {
                copyBtn.onclick = async () => {
                    try {
                        const copied = await copyTextToClipboard(txids.join("\n"));
                        if (copied) {
                            setStatus("Txids copiados al portapapeles.", false);
                        } else {
                            setStatus("No se pudo copiar los txids.", true);
                        }
                    } catch (error) {
                        setStatus("Error copiando txids: " + error.message, true);
                    }
                };
            }
        }
    }

    function renderNodeDetails(node, graph) {
        if (!nodeDetailsEl || !node || !graph) {
            return;
        }

        const outgoing = [];
        const incoming = [];

        for (const link of graph.links) {
            const sourceId = edgeEndpoint(link.source);
            const targetId = edgeEndpoint(link.target);

            if (sourceId === node.id) {
                outgoing.push(link);
            }
            if (targetId === node.id) {
                incoming.push(link);
            }
        }

        const outgoingTotal = outgoing.reduce((sum, l) => sum + Number(l.amount || 0), 0);
        const incomingTotal = incoming.reduce((sum, l) => sum + Number(l.amount || 0), 0);

        const topOutgoing = outgoing
            .slice()
            .sort((a, b) => b.amount - a.amount)
            .slice(0, 5)
            .map((l) => {
                const targetId = edgeEndpoint(l.target);
                return targetId + " (" + l.amount + " sats)";
            });

        const topIncoming = incoming
            .slice()
            .sort((a, b) => b.amount - a.amount)
            .slice(0, 5)
            .map((l) => {
                const sourceId = edgeEndpoint(l.source);
                return sourceId + " (" + l.amount + " sats)";
            });

        const outgoingHtml = topOutgoing.length > 0 ? topOutgoing.join("<br>") : "-";
        const incomingHtml = topIncoming.length > 0 ? topIncoming.join("<br>") : "-";

        nodeDetailsEl.innerHTML =
            "<h2>Detalle de nodo</h2>" +
            "<div><strong>Wallet:</strong> " + node.id + "</div>" +
            "<div><strong>Balance:</strong> " + node.amountSatoshis + " sats (" + node.amountBtc + " BTC)</div>" +
            "<div><strong>Aristas salientes:</strong> " + outgoing.length + " | Total: " + outgoingTotal + " sats</div>" +
            "<div><strong>Aristas entrantes:</strong> " + incoming.length + " | Total: " + incomingTotal + " sats</div>" +
            "<div style=\"margin-top:6px;\"><strong>Top destinos:</strong><br>" + outgoingHtml + "</div>" +
            "<div style=\"margin-top:6px;\"><strong>Top origenes:</strong><br>" + incomingHtml + "</div>";
    }

    function safeFileToken(value) {
        return (value || "trace")
            .replace(/[^a-zA-Z0-9_-]/g, "_")
            .slice(0, 60);
    }

    function downloadTextFile(fileName, content, mimeType) {
        const blob = new Blob([content], { type: mimeType });
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    }

    function downloadBlob(fileName, blob) {
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    }

    function csvEscape(value) {
        const str = String(value == null ? "" : value);
        if (str.includes(",") || str.includes("\n") || str.includes("\"")) {
            return "\"" + str.replace(/"/g, "\"\"") + "\"";
        }
        return str;
    }

    function buildCsvFromGraph(payload, graph) {
        const lines = [];

        lines.push("section,initialWallet,initialAmountSatoshis,initialAmountBtc");
        lines.push([
            "summary",
            payload.initialWallet,
            payload.initialAmountSatoshis,
            payload.initialAmountBtc
        ].map(csvEscape).join(","));
        lines.push("");

        lines.push("section,wallet,amountSatoshis,amountBtc,isInitial");
        for (const n of graph.nodes) {
            lines.push([
                "node",
                n.id,
                n.amountSatoshis,
                n.amountBtc,
                n.isInitial
            ].map(csvEscape).join(","));
        }
        lines.push("");

        lines.push("section,from,to,direction,amountSatoshis,amountBtc,txCount,txids");
        for (const e of graph.links) {
            lines.push([
                "edge",
                edgeEndpoint(e.source),
                edgeEndpoint(e.target),
                edgeDirection(e, graph.initialWallet),
                e.amount,
                e.amountBtc,
                e.txCount || 0,
                normalizeTxids(e.txids).join("|")
            ].map(csvEscape).join(","));
        }

        return lines.join("\n");
    }

    function mapGraphPayload(payload, minAmount, maxEdges, applyTopN, hideZeroBalance) {
        const nodeById = new Map();
        const baseNodes = (payload.nodes || []).map((n) => {
            const node = {
                id: n.wallet,
                amountSatoshis: Number(n.amountSatoshis || 0),
                amountBtc: n.amountBtc || satoshisToBtcString(Number(n.amountSatoshis || 0)),
                isInitial: n.wallet === payload.initialWallet
            };
            nodeById.set(node.id, node);
            return node;
        });

        const groupedEdges = new Map();
        for (const e of (payload.edges || [])) {
            const source = e.from;
            const target = e.to;
            const amount = Number(e.amount || 0);
            const txids = normalizeTxids(e.txids);
            const txCount = Number(e.txCount || txids.length || 0);
            const key = source + "->" + target;
            const existing = groupedEdges.get(key);

            if (!existing) {
                groupedEdges.set(key, {
                    source: source,
                    target: target,
                    amount: amount,
                    txids: txids,
                    txCount: txCount
                });
            } else {
                existing.amount += amount;
                const mergedTxids = new Set([...(existing.txids || []), ...txids]);
                existing.txids = Array.from(mergedTxids);
                existing.txCount = existing.txids.length;
            }
        }

        let rawEdges = Array.from(groupedEdges.values())
            .map((e) => ({
                source: e.source,
                target: e.target,
                amount: Number(e.amount || 0),
                amountBtc: satoshisToBtcString(Number(e.amount || 0)),
                txCount: Number(e.txCount || normalizeTxids(e.txids).length || 0),
                txids: normalizeTxids(e.txids),
                direction: edgeDirection({ source: e.source, target: e.target }, payload.initialWallet)
            }))
            .filter((e) => e.amount >= minAmount)
            .sort((a, b) => b.amount - a.amount);

        if (applyTopN) {
            rawEdges = rawEdges.slice(0, maxEdges);
        }

        let resultNodes = baseNodes.slice();

        if (hideZeroBalance) {
            const allowedNodeIds = new Set(
                resultNodes
                    .filter((n) => n.isInitial || Number(n.amountSatoshis || 0) !== 0)
                    .map((n) => n.id)
            );

            rawEdges = rawEdges.filter((e) => allowedNodeIds.has(e.source) && allowedNodeIds.has(e.target));
            resultNodes = resultNodes.filter((n) => allowedNodeIds.has(n.id));
        }

        for (const edge of rawEdges) {
            if (!nodeById.has(edge.source)) {
                const src = {
                    id: edge.source,
                    amountSatoshis: 0,
                    amountBtc: "0.00000000",
                    isInitial: edge.source === payload.initialWallet
                };
                nodeById.set(src.id, src);
                resultNodes.push(src);
            }
            if (!nodeById.has(edge.target)) {
                const trg = {
                    id: edge.target,
                    amountSatoshis: 0,
                    amountBtc: "0.00000000",
                    isInitial: edge.target === payload.initialWallet
                };
                nodeById.set(trg.id, trg);
                resultNodes.push(trg);
            }
        }

        return { initialWallet: payload.initialWallet, nodes: resultNodes, links: rawEdges };
    }

    function colorForNode(n) {
        if (n.isInitial) {
            return "#f59e0b";
        }
        if (n.amountSatoshis > 0) {
            return "#22c55e";
        }
        if (n.amountSatoshis < 0) {
            return "#ef4444";
        }
        return "#94a3b8";
    }

    function drawGraph(graph, payload) {
        clearGraph();

        width = window.innerWidth - 320;
        height = window.innerHeight;

        svg.attr("width", width).attr("height", height);

        const g = svg.append("g");

        const zoom = d3.zoom().scaleExtent([0.2, 6]).on("zoom", (event) => {
            g.attr("transform", event.transform);
        });

        svg.call(zoom);

        const defs = svg.append("defs");
        defs.selectAll("marker")
            .data([
                { id: "arrow-outgoing", color: EDGE_DIRECTION_COLORS.outgoing },
                { id: "arrow-incoming", color: EDGE_DIRECTION_COLORS.incoming },
                { id: "arrow-neutral", color: EDGE_DIRECTION_COLORS.neutral },
                { id: "arrow-outgoing-selected", color: EDGE_DIRECTION_COLORS.selected },
                { id: "arrow-incoming-selected", color: EDGE_DIRECTION_COLORS.selected },
                { id: "arrow-neutral-selected", color: EDGE_DIRECTION_COLORS.selected }
            ])
            .join("marker")
            .attr("id", (d) => d.id)
            .attr("viewBox", "0 0 10 10")
            .attr("refX", 12)
            .attr("refY", 5)
            .attr("markerWidth", 9)
            .attr("markerHeight", 9)
            .attr("orient", "auto")
            .append("path")
            .attr("d", "M 0 0 L 10 5 L 0 10 z")
            .attr("fill", (d) => d.color);

        const maxNodeAmount = d3.max(graph.nodes, (d) => Math.abs(d.amountSatoshis)) || 1;
        const maxEdgeAmount = d3.max(graph.links, (d) => d.amount) || 1;

        const nodeRadius = d3.scaleSqrt()
            .domain([0, maxNodeAmount])
            .range([5, 22]);

        const edgeWidth = d3.scaleSqrt()
            .domain([0, maxEdgeAmount])
            .range([0.8, 4.5]);

        const simulation = d3.forceSimulation(graph.nodes)
            .force("link", d3.forceLink(graph.links).id((d) => d.id).distance(110).strength(0.25))
            .force("charge", d3.forceManyBody().strength(-280))
            .force("center", d3.forceCenter(width / 2, height / 2))
            .force("collide", d3.forceCollide().radius((d) => nodeRadius(Math.abs(d.amountSatoshis)) + 3));

        const links = g.append("g")
            .selectAll("line")
            .data(graph.links)
            .join("line")
            .attr("stroke-width", (d) => edgeWidth(d.amount))
            .attr("stroke-linecap", "round")
            .attr("stroke", (d) => edgeAppearance(d, graph.initialWallet, selectedEdgeKey).color)
            .attr("stroke-opacity", (d) => edgeAppearance(d, graph.initialWallet, selectedEdgeKey).opacity)
            .attr("marker-end", (d) => "url(#" + edgeAppearance(d, graph.initialWallet, selectedEdgeKey).markerId + ")")
            .attr("cursor", "pointer")
            .on("click", (event, d) => {
                event.stopPropagation();
                selectedEdgeKey = edgeKey(d.source, d.target);
                selectedNodeId = null;
                updateNodeStyles();
                updateLinkStyles();
                renderEdgeDetails(d, graph);
            });

        links.append("title")
            .text((d) => edgeDirectionLabel(edgeDirection(d, graph.initialWallet)) + " | " + edgeEndpoint(d.source)
                + " -> " + edgeEndpoint(d.target)
                + " | " + d.amount + " sats (" + d.amountBtc + " BTC)"
                + " | txCount=" + (d.txCount || 0));

        const nodes = g.append("g")
            .selectAll("circle")
            .data(graph.nodes)
            .join("circle")
            .attr("r", (d) => nodeRadius(Math.abs(d.amountSatoshis)))
            .attr("fill", colorForNode)
            .attr("stroke", "#0f172a")
            .attr("stroke-width", 1.5)
            .on("click", (event, d) => {
                event.stopPropagation();
                selectedNodeId = d.id;
                selectedEdgeKey = null;
                updateNodeStyles();
                updateLinkStyles();
                renderNodeDetails(d, graph);
            })
            .call(drag(simulation));

        function updateNodeStyles() {
            nodes
                .attr("stroke", (d) => d.id === selectedNodeId ? "#f8fafc" : "#0f172a")
                .attr("stroke-width", (d) => d.id === selectedNodeId ? 3 : 1.5);
        }

        function updateLinkStyles() {
            links
                .attr("stroke", (d) => edgeAppearance(d, graph.initialWallet, selectedEdgeKey).color)
                .attr("stroke-opacity", (d) => edgeAppearance(d, graph.initialWallet, selectedEdgeKey).opacity)
                .attr("marker-end", (d) => "url(#" + edgeAppearance(d, graph.initialWallet, selectedEdgeKey).markerId + ")");
        }

        svg.on("click", () => {
            selectedNodeId = null;
            selectedEdgeKey = null;
            updateNodeStyles();
            updateLinkStyles();
            setNodeDetailsPlaceholder("Haz clic en un nodo o arista para ver detalle.");
        });

        nodes.append("title")
            .text((d) => d.id + "\n" + d.amountSatoshis + " sats (" + d.amountBtc + " BTC)");

        const labels = g.append("g")
            .selectAll("text")
            .data(graph.nodes)
            .join("text")
            .text((d) => d.id.slice(0, 8) + "..." + d.id.slice(-6))
            .attr("font-size", 10)
            .attr("fill", "#e2e8f0")
            .attr("pointer-events", "none");

        simulation.on("tick", () => {
            links.attr("x1", (d) => linkEndpoints(d, nodeRadius).x1)
                .attr("y1", (d) => linkEndpoints(d, nodeRadius).y1)
                .attr("x2", (d) => linkEndpoints(d, nodeRadius).x2)
                .attr("y2", (d) => linkEndpoints(d, nodeRadius).y2);

            nodes
                .attr("cx", (d) => d.x)
                .attr("cy", (d) => d.y);

            labels
                .attr("x", (d) => d.x + 8)
                .attr("y", (d) => d.y + 3);
        });

        metaEl.textContent =
            "Initial: " + payload.initialWallet +
            " | Balance: " + payload.initialAmountSatoshis + " sats (" + payload.initialAmountBtc + " BTC)" +
            " | Nodes: " + graph.nodes.length +
            " | Edges: " + graph.links.length;

        const initialNode = graph.nodes.find((n) => n.id === payload.initialWallet) || graph.nodes[0];
        if (initialNode) {
            selectedNodeId = initialNode.id;
            selectedEdgeKey = null;
            updateNodeStyles();
            updateLinkStyles();
            renderNodeDetails(initialNode, graph);
        } else {
            setNodeDetailsPlaceholder("No hay nodos para mostrar.");
        }
    }

    function drag(simulation) {
        function dragStarted(event, d) {
            if (!event.active) simulation.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
        }

        function dragged(event, d) {
            d.fx = event.x;
            d.fy = event.y;
        }

        function dragEnded(event, d) {
            if (!event.active) simulation.alphaTarget(0);
            d.fx = null;
            d.fy = null;
        }

        return d3.drag()
            .on("start", dragStarted)
            .on("drag", dragged)
            .on("end", dragEnded);
    }

    async function loadGraph() {
        if (isLoading) {
            return;
        }

        const wallet = walletInput.value.trim();
        const depth = Number(depthInput.value || 2);
        const minAmount = Number(minAmountInput.value || 0);
        const maxEdges = Number(maxEdgesInput.value || 250);
        const applyTopN = topNEdgesInput.checked;
        const hideZeroBalance = hideZeroBalanceInput.checked;

        if (!wallet) {
            setStatus("Debes indicar una wallet.", true);
            return;
        }

        // Limpia la vista inmediatamente al iniciar una nueva carga.
        clearGraph();
        selectedNodeId = null;
        selectedEdgeKey = null;
        lastPayload = null;
        lastVisualGraph = null;
        metaEl.textContent = "Cargando grafo...";
        setNodeDetailsPlaceholder("Cargando grafo...");
        setStatus("Cargando...", false);
        setLoadingState(true);

        try {
            const response = await fetch("/api/trace/" + encodeURIComponent(wallet) + "?depth=" + depth);
            if (!response.ok) {
                const txt = await response.text();
                setStatus("Error: HTTP " + response.status + " - " + txt, true);
                clearGraph();
                metaEl.textContent = "No se pudo cargar el grafo.";
                setNodeDetailsPlaceholder("No se pudo cargar el grafo.");
                return;
            }

            const payload = await response.json();
            const graph = mapGraphPayload(payload, minAmount, maxEdges, applyTopN, hideZeroBalance);
            drawGraph(graph, payload);

            lastPayload = payload;
            lastVisualGraph = graph;

            setStatus("Grafo cargado correctamente.", false);
        } catch (err) {
            setStatus("Error: " + err.message, true);
            clearGraph();
            metaEl.textContent = "No se pudo cargar el grafo.";
            setNodeDetailsPlaceholder("No se pudo cargar el grafo.");
        } finally {
            setLoadingState(false);
        }
    }

    function exportCurrentJson() {
        if (!lastPayload || !lastVisualGraph) {
            setStatus("Primero carga un grafo para exportar.", true);
            return;
        }

        function edgeEndpoint(value) {
            return typeof value === "object" && value !== null ? value.id : value;
        }

        const exportData = {
            initialWallet: lastPayload.initialWallet,
            initialAmountSatoshis: lastPayload.initialAmountSatoshis,
            initialAmountBtc: lastPayload.initialAmountBtc,
            nodes: lastVisualGraph.nodes.map((n) => ({
                wallet: n.id,
                amountSatoshis: n.amountSatoshis,
                amountBtc: n.amountBtc,
                isInitial: n.isInitial
            })),
            edges: lastVisualGraph.links.map((e) => ({
                from: edgeEndpoint(e.source),
                to: edgeEndpoint(e.target),
                direction: edgeDirection(e, lastVisualGraph.initialWallet),
                amount: e.amount,
                amountBtc: e.amountBtc,
                txCount: Number(e.txCount || normalizeTxids(e.txids).length || 0),
                txids: normalizeTxids(e.txids)
            }))
        };

        const walletToken = safeFileToken(lastPayload.initialWallet);
        const fileName = "trace_" + walletToken + ".json";
        downloadTextFile(fileName, JSON.stringify(exportData, null, 2), "application/json;charset=utf-8");
        setStatus("Exportado JSON: " + fileName, false);
    }

    function exportCurrentCsv() {
        if (!lastPayload || !lastVisualGraph) {
            setStatus("Primero carga un grafo para exportar.", true);
            return;
        }

        const walletToken = safeFileToken(lastPayload.initialWallet);
        const fileName = "trace_" + walletToken + ".csv";
        const csv = buildCsvFromGraph(lastPayload, lastVisualGraph);
        downloadTextFile(fileName, csv, "text/csv;charset=utf-8");
        setStatus("Exportado CSV: " + fileName, false);
    }

    async function exportCurrentPng() {
        if (!lastPayload || !lastVisualGraph) {
            setStatus("Primero carga un grafo para exportar.", true);
            return;
        }

        const svgElement = document.getElementById("graph");
        if (!svgElement) {
            setStatus("No se encontro el SVG para exportar.", true);
            return;
        }

        const walletToken = safeFileToken(lastPayload.initialWallet);
        const fileName = "trace_" + walletToken + ".png";
        let svgUrl = null;

        try {
            const serializer = new XMLSerializer();
            const svgText = serializer.serializeToString(svgElement);
            const svgBlob = new Blob([svgText], { type: "image/svg+xml;charset=utf-8" });
            svgUrl = URL.createObjectURL(svgBlob);

            const image = new Image();
            image.decoding = "async";

            await new Promise((resolve, reject) => {
                image.onload = () => resolve();
                image.onerror = () => reject(new Error("No se pudo renderizar el SVG para PNG."));
                image.src = svgUrl;
            });

            const canvas = document.createElement("canvas");
            canvas.width = width;
            canvas.height = height;
            const ctx = canvas.getContext("2d");

            if (!ctx) {
                setStatus("No se pudo crear el contexto de canvas para exportar PNG.", true);
                return;
            }

            const bg = ctx.createRadialGradient(width / 2, height / 2, 0, width / 2, height / 2, Math.max(width, height));
            bg.addColorStop(0, "#0b1220");
            bg.addColorStop(1, "#020617");
            ctx.fillStyle = bg;
            ctx.fillRect(0, 0, width, height);
            ctx.drawImage(image, 0, 0, width, height);

            await new Promise((resolve) => {
                canvas.toBlob((pngBlob) => {
                    if (pngBlob) {
                        downloadBlob(fileName, pngBlob);
                        setStatus("Exportado PNG: " + fileName, false);
                    } else {
                        setStatus("No se pudo generar el PNG.", true);
                    }
                    resolve();
                }, "image/png");
            });
        } catch (error) {
            setStatus("Error exportando PNG: " + error.message, true);
        } finally {
            if (svgUrl) {
                URL.revokeObjectURL(svgUrl);
            }
        }
    }

    loadBtn.addEventListener("click", loadGraph);
    exportJsonBtn.addEventListener("click", exportCurrentJson);
    exportCsvBtn.addEventListener("click", exportCurrentCsv);
    exportPngBtn.addEventListener("click", exportCurrentPng);
    window.addEventListener("resize", () => {
        if (svg.selectAll("circle").size() > 0) {
            loadGraph();
        }
    });

    setNodeDetailsPlaceholder("Haz clic en un nodo o arista para ver detalle.");

    loadGraph();
})();


