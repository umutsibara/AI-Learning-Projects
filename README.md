# Yazılım Mühendisliği Projeleri

Bu repository, iki farklı yapay zeka ve web uygulaması projesini içermektedir. Her iki proje de modern teknolojiler kullanılarak geliştirilmiş ve gerçek dünya problemlerine çözüm sunmayı amaçlamaktadır.

## 1. Chess Image to FEN Dönüştürücü

Bu proje, satranç tahtası fotoğraflarını FEN (Forsyth–Edwards Notation) notasyonuna dönüştüren bir yapay zeka uygulamasıdır. Proje, bir backend servisi ve Android uygulamasından oluşmaktadır.

### Özellikler
- Satranç tahtası fotoğraflarını otomatik olarak analiz etme
- FEN notasyonuna dönüştürme
- Kullanıcı dostu web arayüzü
- Android uygulaması ile mobil erişim
- Tam fonksiyonel satranç oyunu:
  - Tüm satranç kuralları implementasyonu
  - Taş hareketleri ve yeme kuralları
  - Sıra kontrolü (beyaz/siyah)
  - Geçersiz hamle kontrolü
- Gelişmiş satranç saati:
  - Ayarlanabilir başlangıç süresi
  - Hamle başına ek süre (increment)
  - Duraklatma ve devam ettirme
  - Otomatik sıra değişimi
  - Süre bitiminde oyun sonu

### Kullanılan Teknolojiler
- Backend: Python, Flask
- Yapay Zeka: TensorFlow/Keras
- Android:
  - Kotlin
  - Material Design 1.12.0
  - Retrofit2 ve OkHttp3
  - ViewBinding
  - Fragment-based UI
  - CountDownTimer
- API: RESTful

### Android Uygulaması Detayları
- Minimum SDK: 24 (Android 7.0)
- Hedef SDK: 35
- Java 11 uyumluluğu
- Fragment tabanlı UI mimarisi
- Retrofit2 ile API entegrasyonu
- GridLayout ile satranç tahtası implementasyonu
- Özelleştirilebilir satranç saati

### Demo Video
[Chess Image to FEN Dönüştürücü Demo](https://youtu.be/53zUPnJaTiI)

## 2. Emlak Tahmin Uygulaması

Bu proje, makine öğrenmesi kullanarak emlak fiyatlarını tahmin eden bir web uygulamasıdır. Kullanıcıların girdiği özelliklere göre fiyat tahmini yapabilmektedir.

### Özellikler
- Şehir, ilçe ve mahalle bazlı lokasyon seçimi
- Metrekare, oda sayısı ve bina yaşı gibi özelliklerin analizi
- Gerçek zamanlı fiyat tahmini
- Kullanıcı dostu arayüz

### Kullanılan Teknolojiler
- Frontend: Streamlit
- Backend: Python
- Veri Analizi: Pandas, NumPy
- Makine Öğrenmesi: Scikit-learn

### Demo Video
[Emlak Tahmin Uygulaması Demo](https://youtu.be/HMf7fo8fSoE)

## Proje Yapısı

### Chess Image to FEN
```
chess_image_to_fen/
├── backend/
│   └── FEN_SERVICE/
│       ├── app.py
│       └── fen_utils.py
└── android/
    └── chess_app/
        ├── app/
        │   ├── src/
        │   │   └── main/
        │   │       ├── java/
        │   │       │   └── com/
        │   │       │       └── umutsibara/
        │   │       │           └── chess_app/
        │   │       │               ├── MainActivity.kt
        │   │       │               ├── ChessPiece.kt
        │   │       │               ├── ChessClock.kt
        │   │       │               ├── ClockFragment.kt
        │   │       │               ├── PlayFragment.kt
        │   │       │               ├── FenFragment.kt
        │   │       │               └── FEN_API_SERVICE.kt
        │   │       └── res/
        │   │           └── layout/
        │   │               ├── activity_main.xml
        │   │               ├── fragment_play.xml
        │   │               ├── fragment_clock.xml
        │   │               ├── fragment_chess_clock.xml
        │   │               ├── fragment_fen.xml
        │   │               ├── dialog_time_settings.xml
        │   │               └── bottom_navigation_bar.xml
        │   └── build.gradle.kts
        └── build.gradle.kts
```

### Emlak Tahmin
```
emlak_tahmin/
├── app.py
├── algoritma.ipynb
├── veri.ipynb
├── data/
│   └── neighborhoods.py
├── utils/
│   └── format_price.py
├── templates/
└── static/
```

## Kurulum ve Çalıştırma

### Chess Image to FEN
1. Backend kurulumu:
```bash
cd chess_image_to_fen/backend/FEN_SERVICE
pip install -r requirements.txt
python app.py
```

2. Android uygulaması için:
- Android Studio'yu açın
- Projeyi `chess_image_to_fen/android/chess_app` dizininden açın
- Gradle sync işlemini tamamlayın
- Uygulamayı derleyin ve çalıştırın

### Emlak Tahmin
```bash
cd emlak_tahmin
pip install -r requirements.txt
streamlit run app.py
```

## Geliştirici Notları

Her iki proje de yazılım mühendisliği öğrencileri tarafından geliştirilmiştir. Projelerde modern yazılım geliştirme pratikleri kullanılmış ve kod kalitesi gözetilmiştir.

### Chess Image to FEN
- Yapay zeka modeli, satranç tahtası tanıma için özel olarak eğitilmiştir
- RESTful API tasarımı ile modüler yapı sağlanmıştır
- Android uygulaması Material Design prensiplerini takip etmektedir
- Fragment tabanlı UI mimarisi kullanılmıştır
- Retrofit2 ile API entegrasyonu sağlanmıştır
- Tam fonksiyonel satranç oyunu ve gelişmiş satranç saati özellikleri implementasyonu

### Emlak Tahmin
- Veri analizi ve ön işleme adımları Jupyter notebook'larda detaylı olarak belgelenmiştir
- Streamlit framework'ü ile hızlı ve etkileşimli bir arayüz geliştirilmiştir
- Makine öğrenmesi modeli gerçek emlak verileri üzerinde eğitilmiştir

## Lisans

Bu projeler MIT lisansı altında lisanslanmıştır. Detaylar için LICENSE dosyasına bakınız.
