package com.tfg.cryptoosint.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.cryptoosint.dto.TransactionDTO;
import com.tfg.cryptoosint.dto.WalletBlockTxDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;

public class JsonParserUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern BITCOIN_ADDRESS_PATTERN = Pattern.compile(
            "^(bc1[ac-hj-np-z02-9]{11,71}|[13][a-km-zA-HJ-NP-Z1-9]{25,34})$"
    );

    public static List<String> extractAddresses(String json) {

        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }

        Set<String> addresses = new LinkedHashSet<>();

        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            collectAddresses(root, null, addresses);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }

        return new ArrayList<>(addresses);
    }

    public static boolean hasRelatedAddresses(String json, String currentAddress) {

        for (String address : extractAddresses(json)) {
            if (!address.equals(currentAddress)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidBitcoinAddress(String wallet) {

        if (wallet == null || wallet.isBlank()) {
            return false;
        }

        return BITCOIN_ADDRESS_PATTERN.matcher(wallet.trim()).matches();
    }

    public static List<WalletBlockTxDTO> extractWalletBlockAppearances(String json, String wallet) {

        if (json == null || json.isBlank() || wallet == null || wallet.isBlank()) {
            return Collections.emptyList();
        }

        List<WalletBlockTxDTO> results = new ArrayList<>();

        //REGION DE CODIGO OCULTADA A PETICION DEL DIRECTOR DEL TFG
        //En el apartado "5.4. Módulo de procesamiento de datos" del documento podras encontrar el contenido de esta clase.

        return results;
    }

    public static long extractWalletAmount(String json, String wallet) {

        if (json == null || json.isBlank() || wallet == null || wallet.isBlank()) {
            return 0L;
        }

        //REGION DE CODIGO OCULTADA A PETICION DEL DIRECTOR DEL TFG
        //En el apartado "5.4. Módulo de procesamiento de datos" del documento podras encontrar el contenido de esta clase.


        return 0L;
    }

    public static List<TransactionDTO> extractOutgoingTransfers(String json, String sourceWallet) {

        if (json == null || json.isBlank() || sourceWallet == null || sourceWallet.isBlank()) {
            return Collections.emptyList();
        }

        //REGION DE CODIGO OCULTADA A PETICION DEL DIRECTOR DEL TFG
        //En el apartado "5.4. Módulo de procesamiento de datos" del documento podras encontrar el contenido de esta clase.

        List<TransactionDTO> transfers = new ArrayList<>();

        return transfers;
    }

    public static String satoshisToBtc(long satoshis) {
        return BigDecimal.valueOf(satoshis)
                .divide(BigDecimal.valueOf(100_000_000L), 8, RoundingMode.DOWN)
                .toPlainString();
    }


    private static long extractBlockstreamStatsBalance(JsonNode root) {

        //REGION DE CODIGO OCULTADA A PETICION DEL DIRECTOR DEL TFG
        //En el apartado "5.4. Módulo de procesamiento de datos" del documento podras encontrar el contenido de esta clase.

        return 0L;
    }

    private static long extractNetAmountFromBlockstreamTxs(JsonNode txs, String wallet) {

        //REGION DE CODIGO OCULTADA A PETICION DEL DIRECTOR DEL TFG
        //En el apartado "5.4. Módulo de procesamiento de datos" del documento podras encontrar el contenido de esta clase.

        return 0L;
    }

    private static boolean isOutgoingTx(JsonNode tx, String wallet) {

        //REGION DE CODIGO OCULTADA A PETICION DEL DIRECTOR DEL TFG
        //En el apartado "5.4. Módulo de procesamiento de datos" del documento podras encontrar el contenido de esta clase.

        return false;
    }

    private static int countWalletOccurrences(JsonNode tx, String wallet) {

        int count = 0;

        //REGION DE CODIGO OCULTADA A PETICION DEL DIRECTOR DEL TFG
        //En el apartado "5.4. Módulo de procesamiento de datos" del documento podras encontrar el contenido de esta clase.

        return count;
    }

    private static void collectAddresses(JsonNode node,
                                         String fieldName,
                                         Set<String> addresses) {

        if (node == null || node.isNull()) {
            return;
        }

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                collectAddresses(entry.getValue(), entry.getKey(), addresses);
            }
            return;
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                collectAddresses(child, fieldName, addresses);
            }
            return;
        }

        if (!node.isTextual()) {
            return;
        }

        String value = node.asText().trim();

        if (value.isEmpty()) {
            return;
        }

        if (looksLikeRelevantAddressField(fieldName) || BITCOIN_ADDRESS_PATTERN.matcher(value).matches()) {
            if (BITCOIN_ADDRESS_PATTERN.matcher(value).matches()) {
                addresses.add(value);
            }
        }
    }

    private static boolean looksLikeRelevantAddressField(String fieldName) {

        if (fieldName == null || fieldName.isBlank()) {
            return false;
        }

        String normalized = fieldName.toLowerCase(Locale.ROOT);

        return normalized.contains("address")
                || normalized.contains("recipient")
                || normalized.contains("sender")
                || normalized.equals("from")
                || normalized.equals("to");
    }
}