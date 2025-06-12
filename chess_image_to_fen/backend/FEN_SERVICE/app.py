import os
from flask import Flask, request, jsonify, render_template
from flask_cors import CORS
from werkzeug.utils import secure_filename
from fen_utils import load_chess_model, get_fen_from_image

# --- Sunucu Kurulumu ---
# Flask'a HTML dosyalarını mevcut klasörde ('.') aramasını söylüyoruz
app = Flask(__name__, template_folder='.') 

CORS(app) 
app.config['UPLOAD_FOLDER'] = 'uploads/'

# --- Modeli Yükleme ---
print("FEN sunucusu başlatılıyor, satranç modeli yükleniyor...")
try:
    chess_model = load_chess_model()
    print("Model başarıyla sunucuya yüklendi.")
except Exception as e:
    print(f"Sunucu başlatılırken model yüklenemedi! Hata: {e}")
    chess_model = None

# --- Web Sayfası Route ---
@app.route('/')
def index():
    return render_template('index.html')

# --- API Endpoint ---
@app.route('/predict', methods=['POST'])
def predict_fen():
    if chess_model is None:
        return jsonify({'error': 'Model yüklenemediği için sunucu çalışmıyor'}), 500

    if 'file' not in request.files:
        return jsonify({'error': 'İstekte dosya bulunamadı'}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'Dosya seçilmedi'}), 400

    if file:
        filename = secure_filename(file.filename)
        os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
        image_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(image_path)

        print(f"Resim alındı ve şuraya kaydedildi: {image_path}")

        try:
            fen_string = get_fen_from_image(chess_model, image_path)

            if os.path.exists(image_path):
                os.remove(image_path)

            if fen_string:
                print(f"FEN üretildi: {fen_string}")
                return jsonify({'fen': fen_string})
            else:
                return jsonify({'error': 'Bu resimden FEN üretilemedi'}), 400
        except Exception as e:
            if os.path.exists(image_path):
                os.remove(image_path)
            print(f"FEN üretimi sırasında hata: {str(e)}")
            return jsonify({'error': f'FEN üretimi sırasında hata: {str(e)}'}), 500

    return jsonify({'error': 'Bilinmeyen bir hata oluştu'}), 500

# --- Sunucuyu Başlatma ---
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)