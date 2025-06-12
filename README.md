# Satranç Uygulaması ve Derin Öğrenme Tabanlı FEN Dönüştürücü

Bu depo, birbiriyle entegre çalışan iki ana projeden oluşmaktadır: 2D bir satranç tahtası görüntüsünü analiz ederek Forsyth-Edwards Notasyonu (FEN) üreten bir derin öğrenme sunucusu ve bu sunucuyu kullanan, aynı zamanda satranç oynama ve satranç saati gibi ek özellikler sunan bir Android uygulaması.

Bu projeler, bir yazılım mühendisliği öğrencisinin makine öğrenmesi, mobil uygulama geliştirme ve bu iki farklı disiplini bir REST API üzerinden bir araya getirme yeteneklerini sergilemek amacıyla geliştirilmiştir.

### İçindekiler
1.  **[Proje 1: Chess FEN API (Derin Öğrenme Sunucusu)](#1-proje-chess-fen-api-derin-öğrenme-sunucusu)**
    * [Amacı ve Çalışma Prensibi](#11-projenin-amacı-ve-çalışma-prensibi)
    * [Kullanılan Teknolojiler](#12-kullanılan-teknolojiler)
    * [Dosya Yapısı ve Açıklamaları](#13-dosya-yapısı-ve-açıklamaları)
    * [Kurulum ve Çalıştırma](#14-kurulum-ve-çalıştırma)
2.  **[Proje 2: Chess Companion App (Android Uygulaması)](#2-proje-chess-companion-app-android-uygulaması)**
    * [Özellikler](#21-özellikler)
    * [Kullanılan Teknolojiler](#22-kullanılan-teknolojiler)
    * [Dosya Yapısı ve Mimarisi](#23-dosya-yapısı-ve-mimarisi)
    * [Kurulum ve Çalıştırma](#24-kurulum-ve-çalıştırma)

---

## 1. Proje: Chess FEN API (Derin Öğrenme Sunucusu)

Bu proje, bir satranç tahtası görüntüsünü alıp içerisindeki taşları ve konumlarını tespit ederek standart FEN formatına dönüştüren akıllı bir sistemdir. Proje, bir Python Flask sunucusu üzerinde çalışan ve bir REST API aracılığıyla hizmet veren bir derin öğrenme modeli etrafında şekillenmiştir.

### 1.1. Projenin Amacı ve Çalışma Prensibi

Projenin temel amacı, fiziksel veya dijital bir satranç oyununun anlık görüntüsünü, dijital satranç motorları ve yazılımları tarafından kolayca anlaşılabilecek evrensel bir formata (FEN) dönüştürmektir.

**Çalışma Akışı:**
1.  **Görüntü Alımı:** Flask tabanlı web sunucusu, `/predict` endpoint'i üzerinden bir POST isteği ile resim dosyasını alır.
2.  **Nesne Tespiti:** Eğitilmiş YOLOv8 modeli, resim üzerindeki satranç taşlarını (Şah, Vezir, Piyon vb. için 'K', 'q', 'p' gibi) ve satranç tahtasının kendisini (`board` sınıfı) sınırlayıcı kutular (bounding boxes) ile tespit eder. Model, 13 farklı sınıfı tanıyacak şekilde eğitilmiştir (6 beyaz taş, 6 siyah taş, 1 tahta).
3.  **Koordinat İşleme (`fen_utils.py`):**
    * İlk olarak, en yüksek güven skoruna sahip `board` nesnesi bulunur ve bu nesnenin koordinatları referans alınır. Eğer `board` tespit edilemezse, resmin tamamı tahta olarak kabul edilir.
    * Her bir taşın merkez koordinatları (`center_x`, `center_y`) hesaplanır.
    * Taşın göreli konumu, 8x8'lik bir matriste hangi kareye (satır, sütun) denk geldiğini bulmak için kullanılır.
    * Bir kareye birden fazla taş denk gelmesi (nadir bir durum) halinde, daha yüksek tespit güven skoruna (`confidence score`) sahip olan taş geçerli sayılır.
4.  **FEN Üretimi (`fen_utils.py`):**
    * Oluşturulan 8x8'lik matris, satır satır taranır.
    * Boş kareler sayılır ve bir taşa denk gelindiğinde bu sayı FEN dizesine eklenir. Taşların FEN formatındaki karakterleri (büyük harf beyaz, küçük harf siyah) matristen alınır.
    * Satırlar arasına `/` karakteri eklenerek FEN dizesi tamamlanır.
5.  **Cevap Dönüşü:** Üretilen FEN dizesi, JSON formatında istemciye (Android uygulama) geri gönderilir.

### 1.2. Kullanılan Teknolojiler

* **Python 3.9:** Projenin ana programlama dili.
* **Ultralytics YOLOv8:** Satranç taşlarını ve tahtayı tespit etmek için kullanılan son teknoloji nesne tanıma modeli.
* **PyTorch:** YOLOv8 modelinin çalıştığı derin öğrenme kütüphanesi.
* **Flask:** Görüntü işleme mantığını bir web API'si olarak sunmak için kullanılan hafif web framework'ü.
* **OpenCV & NumPy:** Görüntü ön işleme ve koordinat manipülasyonları için kullanılan temel kütüphaneler.

### 1.3. Dosya Yapısı ve Açıklamaları

* `train_chess_model.ipynb`:
    * Modelin eğitim sürecini içeren Jupyter Notebook dosyasıdır.
    * `2D Chessboard and Chess Pieces.v4i.yolov8` adlı hazır veri seti kullanılmıştır.
    * `yolov8n.pt` (nano) modeli temel alınarak **25 epoch** boyunca eğitilmiştir.
    * Eğitim sonunda elde edilen mAP50-95 (ortalama hassasiyet) değeri **0.995** gibi oldukça yüksek bir başarı oranına ulaşmıştır. Bu, modelin taşları ve tahtayı çok yüksek bir doğrulukla tespit ettiğini göstermektedir.
* `fen_utils.py`:
    * Projenin çekirdek mantığını içerir. Eğitilmiş modeli yükleme, bir resimden FEN üretme, tespit edilen nesneleri sanal bir 8x8 tahtaya yerleştirme ve bu tahtayı FEN metnine çevirme gibi kritik fonksiyonları barındırır.
* `app.py`:
    * Flask uygulamasını başlatan ve API endpoint'ini tanımlayan dosyadır. Gelen istekleri alır, `fen_utils.py`'deki fonksiyonları çağırır ve sonucu döndürür.
* `index.html`:
    * API'yi tarayıcı üzerinden hızlıca test etmek için kullanılan basit bir HTML arayüzü.

### 1.4. Kurulum ve Çalıştırma

1.  **Gerekli Kütüphaneleri Yükleyin:**
    ```bash
    pip install flask flask_cors ultralytics opencv-python numpy
    ```
2.  **Model Dosyasının Konumunu Doğrulayın:**
    * `fen_utils.py` içerisindeki `MODEL_PATH` değişkeninin, `train_chess_model.ipynb` çalıştırıldıktan sonra `runs/detect/yolov8n_chess_roboflow_v2/weights/best.pt` klasöründe oluşan `best.pt` dosyasının doğru yolunu gösterdiğinden emin olun.
3.  **Sunucuyu Başlatın:**
    ```bash
    python app.py
    ```
    Sunucu varsayılan olarak `localhost:5000` adresinde çalışmaya başlayacaktır. Android uygulaması ile test yapabilmek için yerel ağınızdaki IP adresini kullanmanız gerekecektir.

---

## 2. Proje: Chess Companion App (Android Uygulaması)

Bu proje, yukarıda anlatılan FEN API'sini kullanarak kullanıcılara modern ve kullanışlı bir satranç deneyimi sunan bir Android uygulamasıdır.

### 2.1. Özellikler

* **Satranç Oyna:**
    * İki kişilik yerel (cihaz üzerinden) oynanış imkanı sunar.
    * Piyon, Kale, At, Fil, Vezir ve Şah için temel hamle kurallarını uygular.
    * Hamlelerin önünde başka bir taş olup olmadığını (engel kontrolü) denetler.
    * Sıranın kimde olduğunu gösteren bir arayüze sahiptir.
* **FEN Tanıma:**
    * Galeriden bir satranç tahtası fotoğrafı seçme imkanı sunar.
    * Seçilen görüntüyü Python sunucusuna göndererek anında FEN kodunu alır ve ekranda gösterir.
* **Satranç Saati:**
    * Her iki oyuncu için tamamen özelleştirilebilir başlangıç süresi (dakika ve saniye).
    * Hamle başına zaman eklemesi (increment) özelliği.
    * Oyunu durdurma (pause) ve devam ettirme (resume) imkanı.
    * Süre bitiminde oyunun sona ermesi ve kazananın bildirilmesi.
    * Dikkat dağılmaması için oyun sırasında tam ekran modu.

### 2.2. Kullanılan Teknolojiler

* **Kotlin:** Resmi Android geliştirme dili. Asenkron işlemler için Coroutine'ler aktif olarak kullanılmıştır.
* **Retrofit & OkHttp:** Python ile yazılmış FEN API'si ile ağ iletişimini sağlamak için kullanılan standart kütüphaneler.
* **ViewBinding:** XML layout dosyalarına güvenli ve verimli erişim sağlamak için kullanılır.
* **AndroidX Fragment KTX:** Fragment'lar arası geçiş ve yönetimi kolaylaştırmak için kullanılır.
* **Material Components:** `BottomNavigationView` gibi modern ve tutarlı bir kullanıcı arayüzü oluşturmak için kullanılır.

### 2.3. Dosya Yapısı ve Mimarisi

Uygulama, **Single-Activity, Multi-Fragment** mimarisine dayanmaktadır. Bu yapı, modern ve verimli bir Android uygulama geliştirmesi için tercih edilmiştir.

* `MainActivity.kt`: Uygulamanın ana aktivitesidir. `BottomNavigationView`'ı yönetir ve `PlayFragment`, `ClockFragment` ve `FenFragment` arasında geçişi sağlar. Ayrıca `ClockFragment`'in tam ekran moduna girip çıkmasını yönetmek için bir arayüz uygular.
* **Oyun Mantığı (`PlayFragment.kt`):**
    * Oyunun tüm mantığını ve arayüz etkileşimlerini yönetir.
    * Satranç tahtasını bir `GridLayout` ve 64 `ImageView` ile dinamik olarak oluşturur.
    * Oyun durumunu `boardState` adlı 8x8'lik bir `ChessPiece` dizisi ile takip eder.
    * Kullanıcının karelere tıklamasını (`onSquareClicked`) yönetir, taşları seçer ve `makeMove` ile hamleleri gerçekleştirir.
    * `isValidMove` ve alt fonksiyonları (`isValidPawnMove`, `isValidRookMove` vb.) ile temel satranç kurallarını uygular.
* **Satranç Saati Mantığı (`ClockFragment.kt` & `ChessClock.kt`):**
    * `ClockFragment.kt`: Saatin arayüzünü (`fragment_clock.xml`) ve kullanıcı etkileşimlerini yönetir. Zaman ayarlarını bir `AlertDialog` (`dialog_time_settings.xml`) ile alır.
    * `ChessClock.kt`: Saatin tüm çekirdek mantığını barındırır. `CountDownTimer` kullanarak zamanı geri sayar, sıra değiştirir, zaman eklemesi yapar ve oyunun bitişini dinleyici (listener) aracılığıyla `ClockFragment`'e bildirir.
* **FEN Tanıma Mantığı (`FenFragment.kt` & `FEN_API_SERVICE.kt`):**
    * `FenFragment.kt`: Galeriden resim seçme işlemini yönetir ve seçilen resmi `uploadImageToServer` fonksiyonu ile işler.
    * `FEN_API_SERVICE.kt`: Retrofit arayüzünü ve istemcisini tanımlar. Sunucunun `BASE_URL`'i ve `/predict` endpoint'i burada belirtilmiştir. Ağ isteklerini loglamak için `HttpLoggingInterceptor` içerir.
* **Veri ve Yardımcı Sınıflar:**
    * `ChessPiece.kt`: Satranç taşlarını, türlerini, değerlerini ve drawable kaynaklarını tanımlayan bir `enum` sınıfıdır.
* **UI Layoutları (XML):**
    * Her fragment (`fragment_play.xml`, `fragment_clock.xml`, `fragment_fen.xml`) ve ana aktivite (`activity_main.xml`) için ayrı XML dosyaları bulunur. Bu dosyalar, uygulamanın görsel tasarımını ve bileşen yerleşimini tanımlar.

### 2.4. Kurulum ve Çalıştırma

1.  **Projeyi Android Studio'da Açın.**
2.  **Sunucu IP Adresini Güncelleyin:**
    * `FEN_API_SERVICE.kt` dosyasını açın.
    * `RetrofitClient` objesi içindeki `BASE_URL` sabitini, **Python FEN API sunucusunu çalıştırdığınız bilgisayarın yerel ağ IP adresi** ile değiştirin. Örneğin:
        ```kotlin
        // private const val BASE_URL = "[http://192.168.1.24:5000/](http://192.168.1.24:5000/)"
        private const val BASE_URL = "http://YENI_IP_ADRESINIZ:5000/"
        ```
    * **ÖNEMLİ:** Test edeceğiniz Android cihazın (veya emülatörün) Python sunucusunu çalıştıran bilgisayarla **aynı Wi-Fi ağına** bağlı olduğundan emin olun.
3.  **Uygulamayı Derleyin ve Çalıştırın.**
    * Uygulama açıldıktan sonra alt navigasyon menüsünden ilgili sekmelere geçerek tüm fonksiyonları test edebilirsiniz.

# Proje Koleksiyonu: Makine Öğrenmesi ve Mobil Geliştirme

Bu depo, birbiriyle entegre çalışan iki ana projeden oluşmaktadır: 2D bir satranç tahtası görüntüsünü analiz ederek Forsyth-Edwards Notasyonu (FEN) üreten bir derin öğrenme sunucusu ve bu sunucuyu kullanan bir Android uygulaması; ve ikinci el araç fiyatlarını tahmin eden bir makine öğrenmesi web uygulaması.

Bu projeler, bir yazılım mühendisliği öğrencisinin makine öğrenmesi, mobil uygulama geliştirme ve bu iki farklı disiplini bir REST API üzerinden bir araya getirme yeteneklerini sergilemek amacıyla geliştirilmiştir.

### İçindekiler
1.  **[Proje 1: Satranç Asistanı (FEN Tanıma & Mobil Uygulama)](#1-proje-1-satranç-asistanı-fen-tanıma--mobil-uygulama)**
    * [Genel Bakış](#11-genel-bakış)
    * [Bölüm A: Chess FEN API (Derin Öğrenme Sunucusu)](#12-bölüm-a-chess-fen-api-derin-öğrenme-sunucusu)
    * [Bölüm B: Chess Companion App (Android Uygulaması)](#13-bölüm-b-chess-companion-app-android-uygulaması)
2.  **[Proje 2: İkinci El Araç Fiyat Tahmin Uygulaması](#2-proje-2-ikinci-el-araç-fiyat-tahmin-uygulaması)**
    * [Genel Bakış](#21-genel-bakış)
    * [Çalışma Mimarisi](#22-çalışma-mimarisi)
    * [Kullanılan Teknolojiler](#23-kullanılan-teknolojiler)
    * [Dosya Yapısı ve Açıklamaları](#24-dosya-yapısı-ve-açıklamaları)
    * [Kurulum ve Çalıştırma](#25-kurulum-ve-çalıştırma)

---

## 1. Proje: Satranç Asistanı (FEN Tanıma & Mobil Uygulama)

### 1.1. Genel Bakış
Bu proje, iki ana bileşenden oluşur: Birincisi, bir satranç tahtası görüntüsünü işleyerek FEN formatına dönüştüren bir Python sunucusu. İkincisi ise bu sunucuyla haberleşen, satranç oynama ve satranç saati özellikleri sunan çok fonksiyonlu bir Android uygulamasıdır.

### 1.2. Bölüm A: Chess FEN API (Derin Öğrenme Sunucusu)

Bu bileşen, bir satranç tahtası görüntüsünü alıp içerisindeki taşları ve konumlarını tespit ederek standart FEN formatına dönüştüren akıllı bir sistemdir.

**Çalışma Akışı:**
1.  **Görüntü Alımı:** Flask tabanlı web sunucusu, `/predict` endpoint'i üzerinden resim dosyasını alır.
2.  **Nesne Tespiti:** Eğitilmiş bir YOLOv8 modeli, resim üzerindeki 13 sınıfı (6 beyaz taş, 6 siyah taş, 1 tahta) tanır.
3.  **Koordinat İşleme (`fen_utils.py`):** Tahtanın konumu referans alınarak her taşın 8x8'lik matristeki konumu, en yüksek güven skoruna göre belirlenir.
4.  **FEN Üretimi (`fen_utils.py`):** Oluşturulan matris, satır satır taranarak standart FEN dizesine dönüştürülür.
5.  **Cevap Dönüşü:** Üretilen FEN dizesi, JSON formatında istemciye (Android uygulama) geri gönderilir.

**Teknolojiler:** Python, Flask, PyTorch, Ultralytics YOLOv8, OpenCV, NumPy.

### 1.3. Bölüm B: Chess Companion App (Android Uygulaması)

Bu proje, FEN API'sini kullanarak kullanıcılara modern ve kullanışlı bir satranç deneyimi sunan bir Android uygulamasıdır.

**Özellikler:**
* **Satranç Oyna:** İki kişilik yerel oynanış, temel hamle kurallarının uygulanması ve sıra takibi.
* **FEN Tanıma:** Galeriden seçilen satranç tahtası fotoğrafını sunucuya göndererek FEN kodunu alma.
* **Satranç Saati:** Özelleştirilebilir başlangıç süresi, hamle başına zaman eklemesi (increment), durdurma/devam etme ve tam ekran modu.

**Mimari ve Teknolojiler:**
* **Mimari:** Single-Activity, Multi-Fragment.
* **Teknolojiler:** Kotlin, Coroutines, Retrofit, OkHttp, ViewBinding, AndroidX Fragment KTX, Material Components.
* **Ana Dosyalar:**
    * `MainActivity.kt`: Fragment'lar arası geçişi yönetir.
    * `PlayFragment.kt`: Oyun mantığını ve arayüzünü yönetir.
    * `ClockFragment.kt` & `ChessClock.kt`: Satranç saatinin arayüzünü ve mantığını barındırır.
    * `FenFragment.kt` & `FEN_API_SERVICE.kt`: FEN tanıma özelliğinin arayüzünü ve API iletişimini yönetir.

---

## 2. Proje: İkinci El Araç Fiyat Tahmin Uygulaması

Bu proje, Türkiye'deki ikinci el otomobil piyasası verileri kullanılarak geliştirilmiş bir makine öğrenmesi modelini temel alır. Kullanıcıların, bir aracın marka, model, yıl, kilometre gibi özelliklerini seçerek tahmini piyasa değerini anında öğrenmelerini sağlayan interaktif bir web uygulamasıdır.

### 2.1. Genel Bakış
Proje, on binlerce araç verisiyle eğitilmiş bir makine öğrenmesi modelini, kullanıcı dostu bir web arayüzü üzerinden sunarak ikinci el araç alım-satım sürecine teknolojik bir yaklaşım getirir. Geliştirilen **Random Forest Regressor** modeli, yüksek doğrulukla fiyat tahminleri yapabilmektedir.

### 2.2. Çalışma Mimarisi

Proje, standart bir istemci-sunucu mimarisiyle çalışır:

1.  **Model Eğitimi (Çevrimdışı Adım):**
    * `veri.ipynb` içerisinde, `veri.csv` dosyasındaki ham veriler okunur, eksik veriler temizlenir, veri türleri düzeltilir ve veri seti modellemeye hazır hale getirilir.
    * `algoritma.ipynb` içerisinde, işlenmiş veri seti kullanılarak bir **Random Forest Regressor** modeli eğitilir. Kategorik veriler (Marka, Model vb.) `OrdinalEncoder` kullanılarak sayısal değerlere dönüştürülür. Eğitilen model ve encoder'lar, daha sonra kullanılmak üzere `.joblib` dosyaları olarak kaydedilir.
2.  **Backend (Flask API):**
    * `app.py` çalıştığında, önceden eğitilmiş model (`random_forest_model.joblib`) ve encoder'lar (`encoders.joblib`) sunucu belleğine yüklenir.
    * API, marka/model verilerini ve `/predict` tahmin endpoint'ini sunar.
3.  **Frontend (Web Arayüzü):**
    * `index.html` ve `main.js`, kullanıcı arayüzünü oluşturur.
    * Kullanıcı bir marka seçtiğinde, `main.js` bu markaya ait modelleri backend'den dinamik olarak çeker.
    * "Fiyatı Tahmin Et" butonuna basıldığında, tüm veriler JSON formatında paketlenir ve `/predict` endpoint'ine gönderilir. Sunucudan gelen tahmin, kullanıcıya gösterilir.

### 2.3. Kullanılan Teknolojiler

* **Backend & Makine Öğrenmesi:**
    * **Python:** Projenin ana programlama dili.
    * **Flask:** Makine öğrenmesi modelini bir REST API olarak sunmak için kullanılan web sunucu çatısı.
    * **Pandas & NumPy:** Veri manipülasyonu, temizlenmesi ve ön işlenmesi.
    * **Scikit-learn:** Makine öğrenmesi modelini (Random Forest) oluşturmak, eğitmek ve değerlendirmek.
    * **Joblib:** Eğitilmiş makine öğrenmesi modelini ve veri işleme araçlarını (encoder'lar) kaydetmek ve tekrar yüklemek.
* **Frontend:**
    * **HTML:** Web sayfasının yapısını oluşturur.
    * **JavaScript (Vanilla):** Kullanıcı etkileşimlerini yönetir, dinamik olarak verileri yükler ve API'ye istekler gönderir.
* **Veri Analizi:**
    * **Jupyter Notebook:** Veri setini analiz etmek (`veri.ipynb`) ve makine öğrenmesi modelini geliştirmek (`algoritma.ipynb`).

### 2.4. Dosya Yapısı ve Açıklamaları

* `veri.csv`: Modelin eğitiminde kullanılan, araç özelliklerini ve fiyatlarını içeren ham veri seti.
* `veri.ipynb`: Veri setini keşfetme, temizleme ve ön işleme adımlarını içeren Jupyter Notebook.
* `algoritma.ipynb`: Makine öğrenmesi modelinin geliştirildiği, eğitildiği, test edildiği ve kaydedildiği Jupyter Notebook.
* `app.py`: Flask uygulamasını çalıştıran, API endpoint'lerini tanımlayan ve tahmin mantığını yürüten ana sunucu dosyası.
* `index.html`: Kullanıcının etkileşimde bulunduğu web arayüzünün HTML iskeleti.
* `main.js`: Frontend'in tüm dinamik davranışlarını (API istekleri, DOM manipülasyonu vb.) yöneten JavaScript dosyası.
* `random_forest_model.joblib` / `encoders.joblib`: Eğitilmiş ve kullanıma hazır model ve encoder dosyaları.

### 2.5. Kurulum ve Çalıştırma

1.  **Gerekli Kütüphaneleri Yükleyin:**
    ```bash
    pip install Flask pandas scikit-learn joblib
    ```
2.  **Modeli Eğitin ve Kaydedin (İsteğe Bağlı):**
    * `veri.ipynb` dosyasını çalıştırarak veriyi işleyin.
    * `algoritma.ipynb` dosyasını çalıştırarak `.joblib` dosyalarını oluşturun. (Eğer bu dosyalar depoda mevcutsa bu adım atlanabilir.)
3.  **Sunucuyu Başlatın:**
    ```bash
    python app.py
    ```
    Sunucu varsayılan olarak `http://127.0.0.1:5000` adresinde çalışmaya başlayacaktır.
4.  **Arayüzü Görüntüleyin:**
    * Bir web tarayıcısı açın ve `index.html` dosyasını doğrudan açarak uygulamayı kullanmaya başlayın.
