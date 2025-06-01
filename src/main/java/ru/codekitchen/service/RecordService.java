package ru.codekitchen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.codekitchen.dao.RecordDao;
import ru.codekitchen.entity.Record;
import ru.codekitchen.entity.dto.RecordsContainerDto;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service
public class RecordService {
    private final RecordDao recordDao;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final long CACHE_TTL = 5 * 60 * 1000; // 5 минут в миллисекундах
    private static final int PAGE_SIZE = 20; // Количество записей на странице (для справки)
    private static final String CACHE_DIR = System.getProperty("user.home") + "/.cryptomarket/cache/";
    private static final String CACHE_FILE_PREFIX = "cache_";
    private static final String USD_BYN_RATE_CACHE_FILE = CACHE_DIR + "usd_byn_rate.json";

    @Autowired
    public RecordService(RecordDao recordDao, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.recordDao = recordDao;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        // Создаём директорию для кэша, если она не существует
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            boolean created = cacheDir.mkdirs();
            if (created) {
                System.out.println("Cache directory created: " + CACHE_DIR);
            } else {
                System.err.println("Failed to create cache directory: " + CACHE_DIR);
            }
        } else {
            System.out.println("Cache directory already exists: " + CACHE_DIR);
        }
    }

    // Геттер для PAGE_SIZE
    public static int getPageSize() {
        return PAGE_SIZE;
    }

    // Метод форматирования чисел (кроме рыночной капитализации и объема)
    private String formatPrice(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "Invalid";
        }
        if (value != 0 && Math.abs(value) < 0.01) {
            DecimalFormat df = new DecimalFormat("0.000000"); // 6 знаков после запятой
            return "~" + df.format(value);
        } else {
            DecimalFormat df = new DecimalFormat("0.00"); // 2 знака после запятой
            return df.format(value);
        }
    }

    // Метод форматирования объема с суффиксами M и B
    private String formatVolume(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "Invalid USDT";
        }
        if (value >= 1_000_000_000) {
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(value / 1_000_000_000) + "B USDT";
        } else if (value >= 1_000_000) {
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(value / 1_000_000) + "M USDT";
        } else {
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(value) + " USDT";
        }
    }

    public RecordsContainerDto findAllRecords(String filterMode, boolean forceRefresh) {
        String cacheFile = CACHE_DIR + CACHE_FILE_PREFIX + "cryptoData.json";
        List<Record> records = loadFromCache(cacheFile);

        if (!forceRefresh && records != null && !isCacheExpired(cacheFile)) {
            System.out.println("Returning cached data from: " + cacheFile);
            return new RecordsContainerDto(records);
        }

        System.out.println("Fetching fresh data for findAllRecords...");
        records = fetchCryptoData();
        if (!records.isEmpty()) {
            System.out.println("Saving " + records.size() + " records to cache: " + cacheFile);
            saveToCache(cacheFile, records);
        } else {
            System.err.println("No records fetched for findAllRecords, skipping cache save.");
        }
        return new RecordsContainerDto(records);
    }

    public RecordsContainerDto findExchangeRecords(String exchange, String filterMode, boolean forceRefresh) {
        String cacheFile = CACHE_DIR + CACHE_FILE_PREFIX + exchange + ".json";
        List<Record> records = loadFromCache(cacheFile);

        if (!forceRefresh && records != null && !isCacheExpired(cacheFile)) {
            System.out.println("Returning cached data from: " + cacheFile);
            return new RecordsContainerDto(records);
        }

        System.out.println("Fetching fresh data for exchange: " + exchange);
        records = fetchCryptoDataForExchange(exchange);
        if (!records.isEmpty()) {
            System.out.println("Saving " + records.size() + " records to cache: " + cacheFile);
            saveToCache(cacheFile, records);
        } else {
            System.err.println("No records fetched for exchange " + exchange + ", skipping cache save.");
        }
        return new RecordsContainerDto(records);
    }

    private List<Record> fetchCryptoData() {
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=100&page=1&sparkline=false&price_change_percentage=1h,24h,7d";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-cg-api-key", "CG-WkWLv5AULbRVVACEgZ5RXEu8");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            System.out.println("Fetching data from CoinGecko API...");
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            List<Record> records = new ArrayList<>();

            double usdToBynRate = getUsdToBynRate();

            for (JsonNode coin : jsonNode) {
                String id = coin.get("id").asText();
                String name = coin.get("name").asText();
                String symbol = coin.get("symbol").asText().toUpperCase();
                double currentPrice = coin.get("current_price").asDouble();
                double marketCap = coin.get("market_cap").asDouble();
                double priceChange1h = coin.has("price_change_percentage_1h_in_currency") ? coin.get("price_change_percentage_1h_in_currency").asDouble() : 0.0;
                double priceChange24h = coin.has("price_change_percentage_24h_in_currency") ? coin.get("price_change_percentage_24h_in_currency").asDouble() : 0.0;
                double priceChange7d = coin.has("price_change_percentage_7d_in_currency") ? coin.get("price_change_percentage_7d_in_currency").asDouble() : 0.0;
                double volume = 0.0; // CoinGecko не используется для Huobi, поэтому объем не заполняется здесь

                double currentPriceByn = currentPrice * usdToBynRate;
                double marketCapByn = marketCap * usdToBynRate;

                records.add(new Record(id, name, symbol, currentPrice, currentPriceByn, marketCap, marketCapByn, priceChange1h, priceChange24h, priceChange7d, volume));
                System.out.println("Processed ticker for " + symbol + ": price=" + formatPrice(currentPrice) + " USD, priceBYN=" + formatPrice(currentPriceByn) + " BYN, marketCap=" + marketCap + " USD, change1h=" + formatPrice(priceChange1h) + "%, change24h=" + formatPrice(priceChange24h) + "%, change7d=" + formatPrice(priceChange7d) + "%");
            }
            System.out.println("Successfully fetched " + records.size() + " records from CoinGecko");
            return records;
        } catch (Exception e) {
            System.err.println("Error fetching data from CoinGecko: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    private List<Record> fetchCryptoDataForExchange(String exchange) {
        if (exchange.equals("binance")) {
            String url = "https://api.binance.com/api/v3/ticker/24hr";
            try {
                System.out.println("Fetching data from Binance API...");
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                JsonNode jsonArray = objectMapper.readTree(response.getBody());
                List<Record> records = new ArrayList<>();

                double usdToBynRate = getUsdToBynRate();

                for (JsonNode ticker : jsonArray) {
                    String symbol = ticker.get("symbol").asText();
                    if (!symbol.endsWith("USDT")) {
                        continue;
                    }

                    String coinSymbol = symbol.replace("USDT", "").toUpperCase();
                    String name = coinSymbol;
                    String id = coinSymbol.toLowerCase();
                    double currentPrice = ticker.get("lastPrice").asDouble();
                    double priceChange24h = ticker.get("priceChangePercent").asDouble();
                    double marketCap = 0.0;
                    double priceChange1h = 0.0;
                    double priceChange7d = 0.0;
                    double volume = ticker.get("quoteVolume").asDouble(); // Объем в USDT

                    double currentPriceByn = currentPrice * usdToBynRate;
                    double marketCapByn = marketCap * usdToBynRate;

                    if (currentPrice == 0 || volume == 0) {
                        System.err.println("Skipping " + coinSymbol + " due to zero price or volume");
                        continue;
                    }

                    records.add(new Record(id, name, coinSymbol, currentPrice, currentPriceByn, marketCap, marketCapByn, priceChange1h, priceChange24h, priceChange7d, volume));
                    System.out.println("Processed ticker for " + coinSymbol + ": price=" + formatPrice(currentPrice) + " USD, priceBYN=" + formatPrice(currentPriceByn) + " BYN, change24h=" + formatPrice(priceChange24h) + "%, volume=" + formatVolume(volume));
                }
                System.out.println("Successfully fetched " + records.size() + " records from Binance");
                return records;
            } catch (Exception e) {
                System.err.println("Error fetching data from Binance: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        } else if (exchange.equals("bybit")) {
            String url = "https://api.bybit.com/v5/market/tickers?category=spot";
            try {
                System.out.println("Fetching data from ByBit API...");
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                List<Record> records = new ArrayList<>();

                double usdToBynRate = getUsdToBynRate();

                for (JsonNode ticker : jsonNode.get("result").get("list")) {
                    String symbol = ticker.get("symbol").asText();
                    if (!symbol.endsWith("USDT")) {
                        continue;
                    }

                    String coinSymbol = symbol.replace("USDT", "").toUpperCase();
                    String name = coinSymbol;
                    String id = coinSymbol.toLowerCase();
                    double currentPrice = ticker.get("lastPrice").asDouble();
                    double priceChange24h = ticker.has("price24hPcnt") ? ticker.get("price24hPcnt").asDouble() : 0.0;
                    double marketCap = 0.0;
                    double priceChange1h = 0.0;
                    double priceChange7d = 0.0;
                    double volume = ticker.get("turnover24h").asDouble(); // Объем в USDT

                    double currentPriceByn = currentPrice * usdToBynRate;
                    double marketCapByn = marketCap * usdToBynRate;

                    if (currentPrice == 0 || volume == 0) {
                        System.err.println("Skipping " + coinSymbol + " due to zero price or volume");
                        continue;
                    }

                    records.add(new Record(id, name, coinSymbol, currentPrice, currentPriceByn, marketCap, marketCapByn, priceChange1h, priceChange24h, priceChange7d, volume));
                    System.out.println("Processed ticker for " + coinSymbol + ": price=" + formatPrice(currentPrice) + " USD, priceBYN=" + formatPrice(currentPriceByn) + " BYN, change24h=" + formatPrice(priceChange24h) + "%, volume=" + formatVolume(volume));
                }
                System.out.println("Successfully fetched " + records.size() + " records from ByBit");
                return records;
            } catch (Exception e) {
                System.err.println("Error fetching data from ByBit: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        } else if (exchange.equals("kraken")) {
            String url = "https://api.kraken.com/0/public/Ticker?pair=XBTUSD,ETHUSD,LTCUSD,XRPUSD,ADAUSD,BCHUSD,DASHUSD,DOTUSD,EOSUSD,LINKUSD";
            try {
                System.out.println("Fetching data from Kraken API...");
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                List<Record> records = new ArrayList<>();

                double usdToBynRate = getUsdToBynRate();

                JsonNode resultNode = jsonNode.get("result");
                Iterator<String> pairNames = resultNode.fieldNames();
                while (pairNames.hasNext()) {
                    String pairName = pairNames.next();
                    if (!pairName.endsWith("ZUSD")) {
                        continue;
                    }

                    JsonNode ticker = resultNode.get(pairName);

                    String coinSymbol = pairName.startsWith("X") ? pairName.substring(1, pairName.length() - 4) : pairName.replace("ZUSD", "");
                    if (coinSymbol.equals("XBT")) {
                        coinSymbol = "BTC";
                    }
                    String name = coinSymbol;
                    String id = coinSymbol.toLowerCase();
                    double currentPrice = ticker.get("c").get(0).asDouble();
                    double openPrice = ticker.get("o").asDouble();
                    double priceChange24h = (openPrice != 0) ? ((currentPrice - openPrice) / openPrice) * 100 : 0.0;
                    double marketCap = 0.0;
                    double priceChange1h = 0.0;
                    double priceChange7d = 0.0;
                    double volume = ticker.get("v").get(0).asDouble() * currentPrice; // Объем за 24 часа в USDT

                    double currentPriceByn = currentPrice * usdToBynRate;
                    double marketCapByn = marketCap * usdToBynRate;

                    if (currentPrice == 0 || volume == 0) {
                        System.err.println("Skipping " + coinSymbol + " due to zero price or volume");
                        continue;
                    }

                    records.add(new Record(id, name, coinSymbol, currentPrice, currentPriceByn, marketCap, marketCapByn, priceChange1h, priceChange24h, priceChange7d, volume));
                    System.out.println("Processed ticker for " + coinSymbol + ": price=" + formatPrice(currentPrice) + " USD, priceBYN=" + formatPrice(currentPriceByn) + " BYN, change24h=" + formatPrice(priceChange24h) + "%, volume=" + formatVolume(volume));
                }
                System.out.println("Successfully fetched " + records.size() + " records from Kraken");
                return records;
            } catch (Exception e) {
                System.err.println("Error fetching data from Kraken: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        } else if (exchange.equals("bitfinex")) {
            String url = "https://api-pub.bitfinex.com/v2/tickers?symbols=tBTCUSD,tETHUSD,tLTCUSD,tXRPUSD,tADAUSD,tDOTUSD,tEOSUSD,tLINKUSD";
            try {
                System.out.println("Fetching data from Bitfinex API...");
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                JsonNode jsonArray = objectMapper.readTree(response.getBody());
                List<Record> records = new ArrayList<>();

                if (!jsonArray.isArray() || jsonArray.size() == 0) {
                    System.err.println("Bitfinex API returned an invalid or empty response");
                    return List.of();
                }

                double usdToBynRate = getUsdToBynRate();

                for (JsonNode ticker : jsonArray) {
                    if (!ticker.isArray() || ticker.size() != 11 || !ticker.get(0).isTextual()) {
                        System.err.println("Invalid ticker data for Bitfinex");
                        continue;
                    }

                    String symbol = ticker.get(0).asText().replace("t", "");
                    String coinSymbol = symbol.substring(0, symbol.length() - 3);
                    String name = coinSymbol;
                    String id = coinSymbol.toLowerCase();
                    double currentPrice = ticker.get(7).asDouble();
                    double priceChange24h = ticker.get(6).asDouble();
                    double marketCap = 0.0;
                    double priceChange1h = 0.0;
                    double priceChange7d = 0.0;
                    double volume = ticker.get(8).asDouble() * currentPrice; // Объем в USDT

                    double currentPriceByn = currentPrice * usdToBynRate;
                    double marketCapByn = marketCap * usdToBynRate;

                    if (currentPrice == 0 || volume == 0) {
                        System.err.println("Skipping " + coinSymbol + " due to zero price or volume");
                        continue;
                    }

                    records.add(new Record(id, name, coinSymbol, currentPrice, currentPriceByn, marketCap, marketCapByn, priceChange1h, priceChange24h, priceChange7d, volume));
                    System.out.println("Processed ticker for " + coinSymbol + ": price=" + formatPrice(currentPrice) + " USD, priceBYN=" + formatPrice(currentPriceByn) + " BYN, change24h=" + formatPrice(priceChange24h) + "%, volume=" + formatVolume(volume));
                }
                System.out.println("Successfully fetched " + records.size() + " records from Bitfinex");
                return records;
            } catch (Exception e) {
                System.err.println("Error fetching data from Bitfinex: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        } else if (exchange.equals("huobi")) {
            String url = "https://api.huobi.pro/market/tickers";
            try {
                System.out.println("Fetching data from Huobi API...");
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                List<Record> records = new ArrayList<>();

                if (!jsonNode.has("data") || !jsonNode.get("data").isArray()) {
                    System.err.println("Huobi API returned invalid data structure");
                    return List.of();
                }

                double usdToBynRate = getUsdToBynRate();

                for (JsonNode ticker : jsonNode.get("data")) {
                    String symbol = ticker.get("symbol").asText().toLowerCase();
                    if (!symbol.endsWith("usdt")) {
                        continue;
                    }

                    String coinSymbol = symbol.replace("usdt", "").toUpperCase();
                    String name = coinSymbol; // Временное решение, можно улучшить через дополнительный API
                    String id = coinSymbol.toLowerCase();
                    double currentPrice = ticker.get("close").asDouble();
                    double openPrice = ticker.has("open") ? ticker.get("open").asDouble() : 0.0;
                    double priceChange24h = (openPrice != 0) ? ((currentPrice - openPrice) / openPrice) * 100 : 0.0;
                    double marketCap = 0.0;
                    double priceChange1h = 0.0;
                    double priceChange7d = 0.0;
                    double volume = ticker.get("vol").asDouble(); // Объем в USDT

                    double currentPriceByn = currentPrice * usdToBynRate;
                    double marketCapByn = marketCap * usdToBynRate;

                    if (currentPrice == 0 || volume == 0) {
                        System.err.println("Skipping " + coinSymbol + " due to zero price or volume");
                        continue;
                    }

                    records.add(new Record(id, name, coinSymbol, currentPrice, currentPriceByn, marketCap, marketCapByn, priceChange1h, priceChange24h, priceChange7d, volume));
                    System.out.println("Processed ticker for " + coinSymbol + ": price=" + formatPrice(currentPrice) + " USD, priceBYN=" + formatPrice(currentPriceByn) + " BYN, change24h=" + formatPrice(priceChange24h) + "%, volume=" + formatVolume(volume));
                }
                System.out.println("Successfully fetched " + records.size() + " records from Huobi");
                return records;
            } catch (Exception e) {
                System.err.println("Error fetching data from Huobi: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        } else {
            System.err.println("Exchange " + exchange + " is not supported yet.");
            return List.of();
        }
    }

    private double getUsdToBynRate() {
        // Проверяем кэш курса
        Double cachedRate = loadRateFromCache();
        if (cachedRate != null && !isCacheExpired(USD_BYN_RATE_CACHE_FILE)) {
            System.out.println("Returning cached USD to BYN rate: " + cachedRate);
            return cachedRate;
        }

        // Запрашиваем курс через API НБРБ
        String url = "https://api.nbrb.by/exrates/rates/431?parammode=0";
        try {
            System.out.println("Fetching USD to BYN rate from NBRB API...");
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            double rate = jsonNode.get("Cur_OfficialRate").asDouble();
            System.out.println("USD to BYN rate: " + rate);

            // Сохраняем курс в кэш
            saveRateToCache(rate);
            return rate;
        } catch (Exception e) {
            System.err.println("Error fetching USD to BYN rate from NBRB API: " + e.getMessage());
            // Если есть кэшированный курс, используем его, даже если он устарел
            if (cachedRate != null) {
                System.out.println("Using expired cached USD to BYN rate due to API failure: " + cachedRate);
                return cachedRate;
            }
            // Значение по умолчанию, если API недоступен и кэша нет
            System.err.println("Falling back to default USD to BYN rate: 3.20");
            return 3.20;
        }
    }

    private Double loadRateFromCache() {
        try {
            File file = new File(USD_BYN_RATE_CACHE_FILE);
            if (!file.exists()) {
                System.out.println("USD to BYN rate cache file does not exist: " + USD_BYN_RATE_CACHE_FILE);
                return null;
            }
            System.out.println("Loading USD to BYN rate from cache file: " + USD_BYN_RATE_CACHE_FILE);
            String json = new String(Files.readAllBytes(Paths.get(USD_BYN_RATE_CACHE_FILE)));
            JsonNode jsonNode = objectMapper.readTree(json);
            double rate = jsonNode.get("rate").asDouble();
            System.out.println("Loaded USD to BYN rate from cache: " + rate);
            return rate;
        } catch (Exception e) {
            System.err.println("Error loading USD to BYN rate from cache: " + e.getMessage());
            return null;
        }
    }

    private void saveRateToCache(double rate) {
        try {
            System.out.println("Saving USD to BYN rate to cache file: " + USD_BYN_RATE_CACHE_FILE);
            String json = objectMapper.writeValueAsString(new RateCache(rate));
            Files.write(Paths.get(USD_BYN_RATE_CACHE_FILE), json.getBytes());
            System.out.println("Successfully saved USD to BYN rate to cache: " + rate);
        } catch (Exception e) {
            System.err.println("Error saving USD to BYN rate to cache: " + e.getMessage());
        }
    }

    private List<Record> loadFromCache(String cacheFile) {
        try {
            File file = new File(cacheFile);
            if (!file.exists()) {
                System.out.println("Cache file does not exist: " + cacheFile);
                return null;
            }
            System.out.println("Loading from cache file: " + cacheFile);
            String json = new String(Files.readAllBytes(Paths.get(cacheFile)));
            Record[] recordsArray = objectMapper.readValue(json, Record[].class);
            System.out.println("Loaded " + recordsArray.length + " records from cache");
            return new ArrayList<>(Arrays.asList(recordsArray));
        } catch (Exception e) {
            System.err.println("Error loading from cache: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void saveToCache(String cacheFile, List<Record> records) {
        try {
            System.out.println("Saving to cache file: " + cacheFile);
            String json = objectMapper.writeValueAsString(records);
            Files.write(Paths.get(cacheFile), json.getBytes());
            System.out.println("Successfully saved " + records.size() + " records to cache");
        } catch (Exception e) {
            System.err.println("Error saving to cache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isCacheExpired(String cacheFile) {
        File file = new File(cacheFile);
        if (!file.exists()) {
            System.out.println("Cache file does not exist, treating as expired: " + cacheFile);
            return true;
        }
        long lastModified = file.lastModified();
        boolean expired = System.currentTimeMillis() - lastModified > CACHE_TTL;
        System.out.println("Cache file " + cacheFile + " last modified: " + lastModified + ", expired: " + expired);
        return expired;
    }

    // Вспомогательный класс для кэширования курса
    private static class RateCache {
        private final double rate;

        public RateCache(double rate) {
            this.rate = rate;
        }

        public double getRate() {
            return rate;
        }
    }
}