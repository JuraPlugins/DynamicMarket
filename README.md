# 🏪 DynamicMarket - Minecraft Dinamik Market Eklentisi

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.18%20%7C%201.19%20%7C%201.20-blue.svg)](https://www.minecraft.net/)
[![Java Version](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/JuraPlugins/DynamicMarket)

> **Minecraft sunucuları için gelişmiş, dinamik fiyatlı, çok dilli market sistemi**

## 📋 İçindekiler

- [🎯 Özellikler](#-özellikler)
- [🚀 Kurulum](#-kurulum)
- [⚙️ Konfigürasyon](#️-konfigürasyon)
- [📖 Kullanım](#-kullanım)
- [🔧 API](#-api)
- [🌍 Çoklu Dil Desteği](#-çoklu-dil-desteği)
- [📊 Görev Sistemi](#-görev-sistemi)
- [🏆 Sıralama Sistemi](#-sıralama-sistemi)
- [🎉 Etkinlik Sistemi](#-etkinlik-sistemi)
- [📁 Proje Yapısı](#-proje-yapısı)
- [🛠️ Geliştirme](#️-geliştirme)
- [🤝 Katkıda Bulunma](#-katkıda-bulunma)
- [📄 Lisans](#-lisans)

## 🎯 Özellikler

### ✨ Ana Özellikler
- **Dinamik Fiyat Sistemi**: Fiyatlar otomatik olarak dalgalanır ve gerçek zamanlı güncellenir
- **Çok Kategorili Market**: Madencilik, Tarım, Avcılık ve Balıkçılık kategorileri
- **Gelişmiş GUI Sistemi**: Kullanıcı dostu, modern arayüz tasarımı
- **Çoklu Dil Desteği**: Türkçe ve İngilizce dil desteği
- **Vault Entegrasyonu**: Ekonomi sistemi ile tam uyumluluk

### 🎮 Oyun İçi Özellikler
- **Görev Sistemi**: Günlük, haftalık ve özel etkinlik görevleri
- **Sıralama Sistemi**: Günlük ve tüm zamanların en iyi oyuncuları
- **Etkinlik Sistemi**: Bonus çarpanlı özel market etkinlikleri
- **Stok Yönetimi**: Akıllı stok takip ve yenileme sistemi
- **Komisyon Sistemi**: Esnek komisyon oranları

### 🔧 Teknik Özellikler
- **API Desteği**: Diğer eklentiler için kapsamlı API
- **YAML Konfigürasyon**: Kolay özelleştirilebilir ayarlar
- **Performans Optimizasyonu**: Düşük CPU kullanımı
- **Veri Saklama**: YAML tabanlı veri depolama sistemi
- **Event Sistemi**: Özelleştirilebilir event'ler

## 🚀 Kurulum

### Gereksinimler
- **Minecraft Server**: 1.18+ sürümü
- **Java**: 17 veya üzeri
- **Vault**: Ekonomi eklentisi
- **Spigot/Paper**: Sunucu yazılımı

### Adım Adım Kurulum

1. **Eklentiyi İndirin**
   ```bash
   # GitHub'dan en son sürümü indirin
   wget https://github.com/JuraPlugins/DynamicMarket/releases/latest/download/DynamicMarket.jar
   ```

2. **Plugins Klasörüne Kopyalayın**
   ```bash
   cp DynamicMarket.jar /path/to/your/server/plugins/
   ```

3. **Sunucuyu Yeniden Başlatın**
   ```bash
   # Sunucu konsolunda
   restart
   ```

4. **İlk Kurulum Sonrası**
   - `plugins/DynamicMarket/` klasörü otomatik oluşturulur
   - `config.yml` dosyası otomatik oluşturulur
   - Dil dosyaları otomatik yüklenir

### Hızlı Kurulum (Docker)
```dockerfile
FROM openjdk:17-jre-slim
WORKDIR /minecraft
COPY DynamicMarket.jar plugins/
EXPOSE 25565
CMD ["java", "-jar", "server.jar"]
```

## ⚙️ Konfigürasyon

### Ana Konfigürasyon (`config.yml`)

```yaml
# Dil seçimi
language: "tr"  # "tr" veya "en"

# Market fiyat ayarları
prices:
  coal: 5.0
  iron_ingot: 20.0
  diamond: 200.0
  emerald: 250.0
  netherite_ingot: 1000.0

# Fiyat dalgalanma ayarları
price_fluctuation:
  enabled: true
  interval_minutes: 1
  max_change_percentage: 50
```

### Görev Şablonları (`quest_templates.yml`)

```yaml
daily_quests:
  beginner_mining:
    type: SELL_ITEMS
    category: mining
    target_items:
      - "STONE:64"
      - "COBBLESTONE:128"
    reward_money: 50.0
    reward_exp: 100
    difficulty: EASY
```

## 📖 Kullanım

### Oyuncu Komutları

| Komut | Açıklama | Kullanım |
|-------|----------|----------|
| `/market` | Ana market menüsünü açar | `/market` |
| `/marketsat` | Ürün satış komutu | `/marketsat <ürün> [miktar]` |
| `/marketsiralama` | Sıralama menüsünü açar | `/marketsiralama [günlük]` |

### Admin Komutları

| Komut | Açıklama | Kullanım | İzin |
|-------|----------|----------|------|
| `/marketadmin reload` | Eklentiyi yeniler | `/marketadmin reload` | `dynamicmarket.admin` |
| `/marketadmin setprice` | Fiyat ayarlar | `/marketadmin setprice <ürün> <fiyat>` | `dynamicmarket.admin` |
| `/marketadmin info` | Sistem bilgilerini gösterir | `/marketadmin info` | `dynamicmarket.admin` |

### Market Kategorileri

#### 🏔️ Madencilik Marketi
- **Ürünler**: Taş, Kömür, Demir, Altın, Elmas, Netherite
- **Özellikler**: Yüksek değerli madenler, dinamik fiyatlar
- **Görevler**: Günlük madencilik hedefleri

#### 🌾 Tarım Marketi
- **Ürünler**: Buğday, Patates, Havuç, Karpuz, Balkabağı
- **Özellikler**: Mevsimsel fiyat değişimleri
- **Görevler**: Hasat festivali görevleri

#### 🗡️ Avcılık Marketi
- **Ürünler**: Rotten Flesh, Kemik, String, Spider Eye
- **Özellikler**: Canavar türüne göre fiyatlandırma
- **Görevler**: Monster hunt etkinlikleri

#### 🎣 Balıkçılık Marketi
- **Ürünler**: Cod, Salmon, Tropical Fish, Pufferfish
- **Özellikler**: Nadir balık bonusları
- **Görevler**: Balıkçılık turnuvaları

## 🔧 API

### MarketAPI Interface

```java
public interface MarketAPI {
    double getPrice(String itemId);
    void setPrice(String itemId, double newPrice);
}
```

### API Kullanım Örneği

```java
// API'yi al
MarketAPI api = Bukkit.getServicesManager()
    .getRegistration(MarketAPI.class)
    .getProvider();

// Fiyat bilgisi al
double price = api.getPrice("diamond");

// Fiyat ayarla
api.setPrice("diamond", 250.0);
```

### Event Sistemi

```java
@EventHandler
public void onMarketSell(DynamicMarketSellEvent event) {
    Player player = event.getPlayer();
    double amount = event.getAmount();
    Material item = event.getItem();
    
    // Özel işlemler
    player.sendMessage("Satış tamamlandı: " + amount + "x " + item.name());
}
```

## 🌍 Çoklu Dil Desteği

### Desteklenen Diller
- 🇹🇷 **Türkçe** (`messages_tr.yml`)
- 🇺🇸 **İngilizce** (`messages_en.yml`)

### Dil Değiştirme
```yaml
# config.yml
language: "tr"  # Türkçe için
language: "en"  # İngilizce için
```

### Özel Mesaj Ekleme
```yaml
# messages_tr.yml
custom_message: "&aÖzel mesajınız burada!"
```

## 📊 Görev Sistemi

### Görev Türleri

#### 📅 Günlük Görevler
- **Madencilik**: Belirli madenleri sat
- **Tarım**: Hasat ürünlerini sat
- **Avcılık**: Canavar düşürmelerini sat
- **Balıkçılık**: Balık ürünlerini sat

#### 📆 Haftalık Görevler
- **Mining Tycoon**: 5000₺ madencilik kazancı
- **Farming Mogul**: 3000₺ tarım kazancı
- **Hunter Legend**: 100 avcılık işlemi
- **Fishing Master**: Deniz ürünleri satışı

#### 🎉 Özel Etkinlik Görevleri
- **Double Mining Weekend**: 2x bonus
- **Harvest Festival**: 1.5x bonus
- **Monster Hunt**: 2.5x bonus
- **Fishing Tournament**: 2x bonus

### Görev Ödülleri
- 💰 **Para Ödülleri**: Görev zorluğuna göre değişir
- ⭐ **Deneyim Puanları**: Seviye atlama için
- 🏆 **Başarım Rozetleri**: Özel başarımlar
- 🎁 **Bonus Çarpanlar**: Etkinlik süresince

## 🏆 Sıralama Sistemi

### Sıralama Türleri
- **Günlük Sıralama**: Günlük kazanç sıralaması
- **Tüm Zamanlar**: Genel kazanç sıralaması

### Sıralama Ödülleri
```
🥇 1. Sıra: 1000₺ + Özel rozet
🥈 2. Sıra: 500₺ + Rozet
🥉 3. Sıra: 250₺ + Rozet
```

### Sıralama Güncellemeleri
- **Günlük**: Her gün gece yarısı sıfırlanır
- **Gerçek Zamanlı**: Anlık güncelleme
- **Otomatik**: Sunucu yeniden başlatıldığında korunur

## 🎉 Etkinlik Sistemi

### Etkinlik Türleri
- **Bonus Çarpan**: Satışlarda ekstra kazanç
- **Süreli Etkinlik**: Belirli süre boyunca aktif
- **Özel Görevler**: Etkinliğe özel görevler
- **Sıralama Yarışması**: Etkinlik süresince sıralama

### Etkinlik Yönetimi
```java
// Etkinlik başlat
marketManager.etkinlikBaslat(2.0, 3600000); // 2x bonus, 1 saat

// Etkinlik kontrol
marketManager.etkinlikKontrolVeBitir();
```

## 📁 Proje Yapısı

```
DynamicMarket/
├── src/main/java/me/example/
│   ├── DynamicMarket.java          # Ana eklenti sınıfı
│   ├── MarketManager.java          # Market yönetim sistemi
│   ├── MarketListener.java         # Event dinleyicileri
│   ├── MarketRankingGUI.java      # Sıralama arayüzü
│   ├── PriceFluctuationTask.java  # Fiyat dalgalanma görevi
│   ├── EarningsStorage.java       # Kazanç veri saklama
│   ├── LocalizationManager.java   # Çoklu dil yöneticisi
│   ├── marketapi/                  # API paketi
│   │   ├── MarketAPI.java         # API interface
│   │   └── MarketAPIProvider.java # API sağlayıcı
│   ├── event/                      # Event paketi
│   │   └── DynamicMarketSellEvent.java
│   ├── tagsintegration/            # Tags entegrasyonu
│   │   ├── TagsMarketIntegration.java
│   │   └── TagsPlugin.java
│   └── util/                       # Yardımcı sınıflar
│       └── MessageUtils.java
├── src/main/resources/
│   ├── config.yml                  # Ana konfigürasyon
│   ├── quest_templates.yml        # Görev şablonları
│   ├── lang/                       # Dil dosyaları
│   │   ├── messages_tr.yml        # Türkçe mesajlar
│   │   └── messages_en.yml        # İngilizce mesajlar
│   └── plugin.yml                  # Eklenti meta verisi
├── pom.xml                         # Maven konfigürasyonu
└── README.md                       # Bu dosya
```

## 🛠️ Geliştirme

### Gereksinimler
- **Java 17+**
- **Maven 3.6+**
- **Spigot API 1.20.4+**
- **IDE**: IntelliJ IDEA, Eclipse veya VS Code

### Geliştirme Ortamı Kurulumu

1. **Projeyi Klonlayın**
   ```bash
   git clone https://github.com/JuraPlugins/DynamicMarket.git
   cd DynamicMarket
   ```

2. **Maven Dependencies'i İndirin**
   ```bash
   mvn clean install
   ```

3. **IDE'de Açın**
   - IntelliJ IDEA: `File > Open > pom.xml`
   - Eclipse: `File > Import > Maven > Existing Maven Projects`

### Build İşlemi
```bash
# JAR dosyası oluştur
mvn clean package

# Test çalıştır
mvn test

# Javadoc oluştur
mvn javadoc:javadoc
```

### Kod Standartları
- **Java Naming Convention**: camelCase metodlar, PascalCase sınıflar
- **Package Structure**: Mantıklı paket organizasyonu
- **Documentation**: Tüm public metodlar için Javadoc
- **Error Handling**: Try-catch blokları ile hata yönetimi

## 🤝 Katkıda Bulunma

### Katkı Süreci

1. **Fork Yapın**: Projeyi kendi hesabınıza fork edin
2. **Branch Oluşturun**: `git checkout -b feature/amazing-feature`
3. **Değişiklikleri Commit Edin**: `git commit -m 'Add amazing feature'`
4. **Push Yapın**: `git push origin feature/amazing-feature`
5. **Pull Request Oluşturun**: GitHub'da PR açın

### Katkı Alanları
- 🐛 **Bug Fixes**: Hata düzeltmeleri
- ✨ **New Features**: Yeni özellikler
- 📚 **Documentation**: Dokümantasyon iyileştirmeleri
- 🌍 **Translations**: Yeni dil desteği
- 🎨 **UI Improvements**: Arayüz iyileştirmeleri

### Geliştirici Rehberi
- **Event Handling**: Bukkit event sistemi kullanın
- **Configuration**: YAML dosyalarını tercih edin
- **Localization**: Çoklu dil desteği ekleyin
- **Performance**: Async işlemler için BukkitRunnable kullanın

## 📄 Lisans

Bu proje **MIT License** altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

```
MIT License

Copyright (c) 2024 DynamicMarket Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🙏 Teşekkürler

- **Spigot Team**: Minecraft eklenti geliştirme platformu
- **Vault Team**: Ekonomi sistemi entegrasyonu
- **Minecraft Community**: Test ve geri bildirim için
- **Contributors**: Kod katkıları için

## 📞 İletişim

- **GitHub**: [@JuraPlugins](https://github.com/JuraPlugins)
- **Discord**: [JuraPlugins Discord](https://discord.gg/juraplugins)
- **Email**: info@juraplugins.com
- **Issues**: [GitHub Issues](https://github.com/JuraPlugins/DynamicMarket/issues)

## 📊 Proje İstatistikleri

![GitHub stars](https://img.shields.io/github/stars/JuraPlugins/DynamicMarket)
![GitHub forks](https://img.shields.io/github/forks/JuraPlugins/DynamicMarket)
![GitHub issues](https://img.shields.io/github/issues/JuraPlugins/DynamicMarket)
![GitHub pull requests](https://img.shields.io/github/issues-pr/JuraPlugins/DynamicMarket)

---

⭐ **Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!** ⭐

**Made with ❤️ by the DynamicMarket Team**
