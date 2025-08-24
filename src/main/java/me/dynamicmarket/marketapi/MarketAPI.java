package me.example.marketapi;




public interface MarketAPI {
    
    double getPrice(String itemId);

    
    void setPrice(String itemId, double newPrice);
}

