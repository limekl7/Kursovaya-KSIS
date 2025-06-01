package ru.codekitchen.entity;

public class Record {
    private String id;
    private String name;
    private String symbol;
    private double currentPrice; // в USD
    private double currentPriceByn; // в BYN
    private double marketCap; // в USD
    private double marketCapByn; // в BYN
    private double priceChange1h;
    private double priceChange24h;
    private double priceChange7d;
    private double volume; // Объем торгов

    public Record(String id, String name, String symbol, double currentPrice, double currentPriceByn,
                  double marketCap, double marketCapByn, double priceChange1h, double priceChange24h,
                  double priceChange7d, double volume) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.currentPrice = currentPrice;
        this.currentPriceByn = currentPriceByn;
        this.marketCap = marketCap;
        this.marketCapByn = marketCapByn;
        this.priceChange1h = priceChange1h;
        this.priceChange24h = priceChange24h;
        this.priceChange7d = priceChange7d;
        this.volume = volume;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSymbol() { return symbol; }
    public double getCurrentPrice() { return currentPrice; }
    public double getCurrentPriceByn() { return currentPriceByn; }
    public double getMarketCap() { return marketCap; }
    public double getMarketCapByn() { return marketCapByn; }
    public double getPriceChange1h() { return priceChange1h; }
    public double getPriceChange24h() { return priceChange24h; }
    public double getPriceChange7d() { return priceChange7d; }
    public double getVolume() { return volume; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public void setCurrentPriceByn(double currentPriceByn) { this.currentPriceByn = currentPriceByn; }
    public void setMarketCap(double marketCap) { this.marketCap = marketCap; }
    public void setMarketCapByn(double marketCapByn) { this.marketCapByn = marketCapByn; }
    public void setPriceChange1h(double priceChange1h) { this.priceChange1h = priceChange1h; }
    public void setPriceChange24h(double priceChange24h) { this.priceChange24h = priceChange24h; }
    public void setPriceChange7d(double priceChange7d) { this.priceChange7d = priceChange7d; }
    public void setVolume(double volume) { this.volume = volume; }
}