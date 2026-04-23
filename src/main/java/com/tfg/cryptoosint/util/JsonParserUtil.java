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

        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            if (!root.isArray()) {
                return Collections.emptyList();
            }

            for (JsonNode tx : root) {
                int occurrences = countWalletOccurrences(tx, wallet);
                if (occurrences == 0) {
                    continue;
                }

                String txid = tx.path("txid").asText("").trim();
                JsonNode status = tx.path("status");
                boolean confirmed = status.path("confirmed").asBoolean(false);
                Integer blockHeight = status.path("block_height").isMissingNode()
                        || status.path("block_height").isNull()
                        ? null
                        : status.path("block_height").asInt();
                Long blockTime = status.path("block_time").isMissingNode()
                        || status.path("block_time").isNull()
                        ? null
                        : status.path("block_time").asLong();

                results.add(new WalletBlockTxDTO(txid, confirmed, blockHeight, blockTime, occurrences));
            }
        } catch (Exception ignored) {
            return Collections.emptyList();
        }

        return results;
    }

    public static long extractWalletAmount(String json, String wallet) {

        if (json == null || json.isBlank() || wallet == null || wallet.isBlank()) {
            return 0L;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);

            long blockstreamStatsBalance = extractBlockstreamStatsBalance(root);
            if (blockstreamStatsBalance != Long.MIN_VALUE) {
                return blockstreamStatsBalance;
            }

            if (root.isArray()) {
                return extractNetAmountFromBlockstreamTxs(root, wallet);
            }
        } catch (Exception ignored) {
            return 0L;
        }

        return 0L;
    }

    public static List<TransactionDTO> extractOutgoingTransfers(String json, String sourceWallet) {

        if (json == null || json.isBlank() || sourceWallet == null || sourceWallet.isBlank()) {
            return Collections.emptyList();
        }

        Map<String, Long> amountsByDestination = new LinkedHashMap<>();
        Map<String, Set<String>> txidsByDestination = new LinkedHashMap<>();

        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            if (!root.isArray()) {
                return Collections.emptyList();
            }

            for (JsonNode tx : root) {
                if (!isOutgoingTx(tx, sourceWallet)) {
                    continue;
                }

                String txid = tx.path("txid").asText("").trim();

                JsonNode vout = tx.path("vout");
                if (!vout.isArray()) {
                    continue;
                }

                for (JsonNode output : vout) {
                    String destination = output.path("scriptpubkey_address").asText("").trim();
                    if (destination.isEmpty() || sourceWallet.equals(destination)) {
                        continue;
                    }

                    long value = output.path("value").asLong(0L);
                    if (value <= 0) {
                        continue;
                    }

                    amountsByDestination.merge(destination, value, Long::sum);

                    if (!txid.isEmpty()) {
                        txidsByDestination
                                .computeIfAbsent(destination, key -> new LinkedHashSet<>())
                                .add(txid);
                    }
                }
            }
        } catch (Exception ignored) {
            return Collections.emptyList();
        }

        List<TransactionDTO> transfers = new ArrayList<>();
        for (Map.Entry<String, Long> entry : amountsByDestination.entrySet()) {
            List<String> txids = new ArrayList<>(
                    txidsByDestination.getOrDefault(entry.getKey(), Collections.emptySet())
            );

            transfers.add(new TransactionDTO(
                    sourceWallet,
                    entry.getKey(),
                    entry.getValue(),
                    satoshisToBtc(entry.getValue()),
                    txids.size(),
                    txids
            ));
        }

        return transfers;
    }

    public static String satoshisToBtc(long satoshis) {
        return BigDecimal.valueOf(satoshis)
                .divide(BigDecimal.valueOf(100_000_000L), 8, RoundingMode.DOWN)
                .toPlainString();
    }


    private static long extractBlockstreamStatsBalance(JsonNode root) {

        JsonNode chainStats = root.path("chain_stats");
        JsonNode mempoolStats = root.path("mempool_stats");

        if (!chainStats.isObject() || !mempoolStats.isObject()) {
            return Long.MIN_VALUE;
        }

        long chainFunded = chainStats.path("funded_txo_sum").asLong(0L);
        long chainSpent = chainStats.path("spent_txo_sum").asLong(0L);
        long mempoolFunded = mempoolStats.path("funded_txo_sum").asLong(0L);
        long mempoolSpent = mempoolStats.path("spent_txo_sum").asLong(0L);

        return (chainFunded - chainSpent) + (mempoolFunded - mempoolSpent);
    }

    private static long extractNetAmountFromBlockstreamTxs(JsonNode txs, String wallet) {

        long net = 0L;

        for (JsonNode tx : txs) {
            JsonNode vin = tx.path("vin");
            if (vin.isArray()) {
                for (JsonNode input : vin) {
                    JsonNode prevout = input.path("prevout");
                    String inputAddress = prevout.path("scriptpubkey_address").asText("");
                    if (wallet.equals(inputAddress)) {
                        net -= prevout.path("value").asLong(0L);
                    }
                }
            }

            JsonNode vout = tx.path("vout");
            if (vout.isArray()) {
                for (JsonNode output : vout) {
                    String outputAddress = output.path("scriptpubkey_address").asText("");
                    if (wallet.equals(outputAddress)) {
                        net += output.path("value").asLong(0L);
                    }
                }
            }
        }

        return net;
    }

    private static boolean isOutgoingTx(JsonNode tx, String wallet) {

        JsonNode vin = tx.path("vin");
        if (!vin.isArray()) {
            return false;
        }

        for (JsonNode input : vin) {
            JsonNode prevout = input.path("prevout");
            String inputAddress = prevout.path("scriptpubkey_address").asText("");
            if (wallet.equals(inputAddress)) {
                return true;
            }
        }

        return false;
    }

    private static int countWalletOccurrences(JsonNode tx, String wallet) {

        int count = 0;

        JsonNode vin = tx.path("vin");
        if (vin.isArray()) {
            for (JsonNode input : vin) {
                String inputAddress = input.path("prevout").path("scriptpubkey_address").asText("").trim();
                if (wallet.equals(inputAddress)) {
                    count++;
                }
            }
        }

        JsonNode vout = tx.path("vout");
        if (vout.isArray()) {
            for (JsonNode output : vout) {
                String outputAddress = output.path("scriptpubkey_address").asText("").trim();
                if (wallet.equals(outputAddress)) {
                    count++;
                }
            }
        }

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