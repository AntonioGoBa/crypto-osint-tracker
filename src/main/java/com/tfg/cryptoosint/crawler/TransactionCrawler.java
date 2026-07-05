package com.tfg.cryptoosint.crawler;

import com.tfg.cryptoosint.dto.GraphDTO;
import com.tfg.cryptoosint.dto.TransactionDTO;
import com.tfg.cryptoosint.dto.WalletNodeDTO;
import com.tfg.cryptoosint.service.BlockClient;
import com.tfg.cryptoosint.util.JsonParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransactionCrawler {

    private static final Logger logger = LoggerFactory.getLogger(TransactionCrawler.class);

    private final BlockClient api;

    public TransactionCrawler(BlockClient api) {
        this.api = api;
    }

    public GraphDTO traceWallet(String wallet, int depth) {

        logger.info("Iniciando trazado para wallet={} con depth={}", wallet, depth);

        Set<String> visitedAddresses = new HashSet<>();
        Map<String, Long> nodesWithAmount = new LinkedHashMap<>();
        List<TransactionDTO> transfers = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        int apiCalls = 0;
        long initialAmount = 0L;

        queue.add(wallet);

        int currentDepth = 0;

        while (!queue.isEmpty() && currentDepth < depth) {

            int nodesAtCurrentDepth = queue.size();
            logger.info(
                    "Procesando nivel {} de {}. Nodos en cola para este nivel={}, visitados hasta ahora={}",
                    currentDepth + 1,
                    depth,
                    nodesAtCurrentDepth,
                    visitedAddresses.size()
            );

            for (int i = 0; i < nodesAtCurrentDepth; i++) {

                String current = queue.poll();

                if (current == null || visitedAddresses.contains(current)) {
                    logger.debug("Saltando wallet nula o ya visitada: {}", current);
                    continue;
                }

                visitedAddresses.add(current);
                apiCalls++;
                logger.info(
                        "Consultando wallet={} en nivel {}. Llamada API #{}",
                        current,
                        currentDepth + 1,
                        apiCalls
                );

                String data = api.getAddress(current);

                //REGION DE CODIGO OCULTADA A PETICION DEL DIRECTOR DEL TFG
                //En el apartado "5.5. Módulo de análisis" del documento podras encontrar el contenido de esta clase.

                List<TransactionDTO> extractedTransfers = JsonParserUtil.extractOutgoingTransfers(data, current);
                transfers.addAll(extractedTransfers);

                List<String> extractedAddresses = JsonParserUtil.extractAddresses(data);
                int newAddresses = 0;

                //REGION DE CODIGO OCULTADA A PETICION DEL DIRECTOR DEL TFG
                //En el apartado "5.5. Módulo de análisis" del documento podras encontrar el contenido de esta clase.

                logger.info(
                        "Wallet={} procesada. Direcciones extraidas={}, transferencias salientes={}, nuevas encoladas={}, cola actual={}",
                        current,
                        extractedAddresses.size(),
                        extractedTransfers.size(),
                        newAddresses,
                        queue.size()
                );
            }

            currentDepth++;
        }

        logger.info(
                "Trazado finalizado para wallet={}. Profundidad recorrida={}, wallets visitadas={}, llamadas API realizadas={}",
                wallet,
                currentDepth,
                visitedAddresses.size(),
                apiCalls
        );

        List<WalletNodeDTO> nodes = new ArrayList<>();
        for (Map.Entry<String, Long> entry : nodesWithAmount.entrySet()) {
            nodes.add(new WalletNodeDTO(
                    entry.getKey(),
                    entry.getValue(),
                    JsonParserUtil.satoshisToBtc(entry.getValue())
            ));
        }

        Map<String, TransactionDTO> aggregatedTransfers = new LinkedHashMap<>();

        //REGION DE CODIGO OCULTADA A PETICION DEL DIRECTOR DEL TFG
        //En el apartado "5.5. Módulo de análisis" del documento podras encontrar el contenido de esta clase.

        return new GraphDTO(
                wallet,
                initialAmount,
                JsonParserUtil.satoshisToBtc(initialAmount),
                nodes,
                new ArrayList<>(aggregatedTransfers.values())
        );
    }
}