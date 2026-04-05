package com.tfg.cryptoosint.service;

import com.tfg.cryptoosint.exception.BlockstreamApiException;
import com.tfg.cryptoosint.util.JsonParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
public class BlockClient {

    private static final Logger logger = LoggerFactory.getLogger(BlockClient.class);

    private final RestTemplate restTemplate;

    public BlockClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAddress(String address) {
        String url = buildBlockstreamUrl(address);
        logger.info("Llamando a Blockstream para wallet={}", address);

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (!JsonParserUtil.hasRelatedAddresses(response, address)) {
                logger.info("Blockstream no devolvio direcciones relacionadas utiles para wallet={}", address);
            }
            return response;
        } catch (RestClientResponseException ex) {
            logger.error("Error llamando a Blockstream para wallet={}. Estado HTTP={}", address, ex.getStatusCode().value());
            throw new BlockstreamApiException(
                    "No se pudo consultar Blockstream para la wallet " + address
                            + ". Estado HTTP=" + ex.getStatusCode().value()
            );
        } catch (RuntimeException ex) {
            logger.error("Error no controlado llamando a Blockstream para wallet={}", address, ex);
            throw new BlockstreamApiException("No se pudo consultar Blockstream para la wallet " + address + ".");
        }
    }

    private String buildBlockstreamUrl(String address) {
        return UriComponentsBuilder
                .fromHttpUrl("https://blockstream.info/api/address/{address}/txs")
                .buildAndExpand(address)
                .toUriString();
    }
}