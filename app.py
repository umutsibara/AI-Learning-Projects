import streamlit as st
from data.neighborhoods import NEIGHBORHOODS
from utils.format_price import format_price
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression

# Sayfa başlığı
st.title('Emlak Fiyat Tahmini')

# Şehir seçimi
city = st.selectbox('Şehir seçiniz:', options=list(NEIGHBORHOODS.keys()))

# Seçilen şehre göre ilçe seçimi
if city:
    districts = list(NEIGHBORHOODS[city].keys())
    district = st.selectbox('İlçe seçiniz:', options=districts)

# Seçilen ilçeye göre mahalle seçimi
if 'district' in locals() and district:
    neighborhoods = NEIGHBORHOODS[city][district]
    neighborhood = st.selectbox('Mahalle seçiniz:', options=neighborhoods)

# Diğer özellikler
area = st.number_input('Metrekare:', min_value=30, max_value=1000, value=100)
rooms = st.number_input('Oda Sayısı:', min_value=1, max_value=10, value=2)
age = st.number_input('Bina Yaşı:', min_value=0, max_value=50, value=5)

# Tahmin butonu
if st.button('Fiyat Tahmini Yap'):
    # Lokasyon bazlı katsayılar (örnek değerler)
    location_multipliers = {
        'Kadıköy': 1.5,
        'Beşiktaş': 1.6,
        'Üsküdar': 1.3,
        'Çankaya': 1.4,
        'Yenimahalle': 1.2
    }
    
    # Temel fiyat hesaplama
    base_price = (
        area * 15000 +  # metrekare etkisi
        rooms * 100000 +  # oda sayısı etkisi
        max(0, (20 - age) * 50000)  # bina yaşı etkisi (yeni bina primi)
    )
    
    # Lokasyon çarpanını uygula
    multiplier = location_multipliers.get(district, 1.0)
    predicted_price = base_price * multiplier
    
    # Rastgele varyasyon ekle (%5 ile %10 arası)
    variation = np.random.uniform(0.95, 1.10)
    predicted_price *= variation
    
    # Formatlanmış fiyatı göster
    st.success(f'Tahmini Fiyat: {format_price(predicted_price)}')