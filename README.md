# 🏪 DynamicMarket - Minecraft Dinamik Market Eklentisi - Jura Plugins

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.18%20%7C%201.19%20%7C%201.20-blue.svg)](https://www.minecraft.net/)
[![Java Version](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/JuraPlugins/DynamicMarket)

> **Minecraft sunucuları için gelişmiş, dinamik fiyatlı, çok dilli market sistemi**
> **Created by Sakhino [@DogukanKckal](https://github.com/DogukanKckal) and JuraPlugins**

## 📋 İçindekiler

- [🎯 Özellikler](#-özellikler)
- [🚀 Kurulum](#-kurulum)
- [⚙️ Konfigürasyon](#️-konfigürasyon)
- [📖 Kullanım](#-kullanım)
- [🌍 Çoklu Dil Desteği](#-çoklu-dil-desteği)
- [📊 Görev Sistemi](#-görev-sistemi)
- [🏆 Sıralama Sistemi](#-sıralama-sistemi)
- [🎉 Etkinlik Sistemi](#-etkinlik-sistemi)
- [🤝 Geliştiriciler](#-geliştiriciler)
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

## 📄 Lisans

Bu proje **Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License** altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

### 🚫 **Lisans Kısıtlamaları:**

**İZİNLER:**
- ✅ Eklentiyi kullanabilirsiniz
- ✅ Orijinal yazarın adını belirtmelisiniz

**YASAKLAR:**
- ❌ **Ticari amaçla kullanamazsınız**
- ❌ **Değiştiremez veya türetemezsiniz**
- ❌ **Tekrar dağıtamazsınız**
- ❌ **Başka projelerde kullanamazsınız**

### 📋 **Lisans Detayları:**
```
Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License

Bu lisans altında:
- Eklentiyi kullanabilirsiniz
- Orijinal yazarın adını belirtmelisiniz
- Ticari amaçla kullanamazsınız
- Değiştiremez veya tekrar yayınlayamazsınız

Ticari kullanım veya özel izin için: info@juraplugins.com
```

## 🙏 Geliştiriciler

- **Sakhino**: [@DogukanKckal](https://github.com/DogukanKckal)

## 📞 İletişim

- **GitHub**: [@JuraPlugins](https://github.com/JuraPlugins)
- **Discord**: [JuraPlugins Discord](https://discord.gg/PPwnMCh)
- **Email**: info@juraplugins.shop

---

⭐ **Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!** ⭐

**Made with ❤️ by the JuraPlugins Team**
