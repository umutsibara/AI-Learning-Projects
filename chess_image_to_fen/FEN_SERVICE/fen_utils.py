import os
import cv2 # Görüntü işleme için (eğer resim boyutları vs. okunacaksa)
import numpy as np
from ultralytics import YOLO


MODEL_PATH = r"C:\Users\ronarduino\Desktop\_SoftwareDev\MachineLearning\chess_image_to_fen\model_traning\runs\detect\yolov8n_chess_roboflow_v2\weights\best.pt"

def load_chess_model(model_path=MODEL_PATH):
    """
    Eğitilmiş YOLO modelini yükler.
    """
    if not os.path.exists(model_path):
        raise FileNotFoundError(f"Model dosyası bulunamadı: {model_path}")
    
    try:
        model = YOLO(model_path)
        print(f"Model başarıyla yüklendi: {model_path}")
        return model
    except Exception as e:
        print(f"Model yüklenirken hata oluştu: {e}")
        raise


def get_fen_from_image(model, image_path, confidence_threshold=0.5):
    """
    Ana fonksiyon: Bir resim yolunu alır, FEN notasyonunu döndürür.
    1. Resim üzerinde nesne tespiti yapar.
    2. Tespit edilen nesneleri 8x8'lik bir tahta matrisine yerleştirir.
    3. Bu matristen FEN stringini oluşturur.
    """
    try:
        results = model.predict(source=image_path, conf=confidence_threshold, verbose=False) # verbose=False çıktıyı temiz tutar
        result = results[0] # Tek resim için ilk sonucu al
        
        detected_objects = []
        for box in result.boxes:
            class_id = int(box.cls[0].item())
            class_name = model.names[class_id]
            confidence = float(box.conf[0].item())
            coordinates = box.xyxy[0].tolist()
            detected_objects.append({
                'class_name': class_name,
                'confidence': confidence,
                'box': coordinates  # [xmin, ymin, xmax, ymax]
            })
    except Exception as e:
        print(f"Resim üzerinde tahmin yapılırken hata oluştu: {e}")
        return None

    # 2. Tespit edilen nesneleri 8x8'lik bir tahta matrisine yerleştirme
    board_representation, confidence_board = detections_to_board(detected_objects, result.orig_shape)
    if board_representation is None:
        return None # Hata oluştuysa None döndür

    # 3. Matristen FEN stringi oluşturma
    fen_string = board_to_fen(board_representation)
    
    return fen_string


def detections_to_board(detected_objects, image_shape):
    """
    Tespit edilen nesnelerin listesini alır ve 8x8'lik bir tahta matrisi döndürür.
    """
    # 8x8'lik boş bir tahta ve güven skoru tahtası oluştur
    board_representation = [['' for _ in range(8)] for _ in range(8)]
    confidence_board = [[0.0 for _ in range(8)] for _ in range(8)]

    # En yüksek güven skoruna sahip 'board' nesnesini bul
    board_bbox = None
    board_confidence = 0.0
    for obj in detected_objects:
        if obj['class_name'] == 'board' and obj['confidence'] > board_confidence:
            board_bbox = obj['box']
            board_confidence = obj['confidence']

    # Eğer 'board' tespit edilemezse, resmin tamamını tahta olarak varsay
    if board_bbox is None:
        print("UYARI: Satranç tahtası ('board' sınıfı) tespit edilemedi! Resmin tamamı kullanılıyor.")
        b_xmin, b_ymin = 0, 0
        board_actual_width, board_actual_height = image_shape[1], image_shape[0] # (h, w, c) -> w, h
    else:
        b_xmin, b_ymin, b_xmax, b_ymax = board_bbox
        board_actual_width = b_xmax - b_xmin
        board_actual_height = b_ymax - b_ymin

    if board_actual_width <= 0 or board_actual_height <= 0:
        print("HATA: Tespit edilen/varsayılan tahta boyutları geçersiz!")
        return None, None

    square_width = board_actual_width / 8
    square_height = board_actual_height / 8

    # Taşları tahtaya yerleştir
    for obj in detected_objects:
        class_name = obj['class_name']
        if class_name == 'board':
            continue # Tahtanın kendisini yerleştirme

        box = obj['box']
        confidence = obj['confidence']
        p_xmin, p_ymin, p_xmax, p_ymax = box
        
        # Taşın merkezini hesapla
        center_x = (p_xmin + p_xmax) / 2
        center_y = (p_ymin + p_ymax) / 2
        
        # Taşın merkezinin tahta sınırları içinde olup olmadığını kontrol et
        if not (b_xmin <= center_x < b_xmin + board_actual_width and b_ymin <= center_y < b_ymin + board_actual_height):
            continue # Tahta dışındaysa bu taşı atla

        # Tahtaya göreli koordinatları ve hangi kareye düştüğünü hesapla
        relative_center_x = center_x - b_xmin
        relative_center_y = center_y - b_ymin
        
        col = int(relative_center_x // square_width)
        row = int(relative_center_y // square_height)
        
        # Sınırları kontrol et (kaymalara karşı)
        row = max(0, min(7, row))
        col = max(0, min(7, col))
        
        # Eğer bu kareye daha yüksek güven skorlu bir taş atanmadıysa ata
        if confidence > confidence_board[row][col]:
            board_representation[row][col] = class_name
            confidence_board[row][col] = confidence
            
    return board_representation, confidence_board


def board_to_fen(board):
    """
    8x8'lik tahta matrisini alır ve FEN stringini oluşturur.
    """
    fen_ranks = []
    for rank_array in board: # Her bir satır için (8. sıradan 1. sıraya doğru)
        fen_rank_str = ""
        empty_squares_count = 0
        for square_char in rank_array: # Satırdaki her bir kare için (a sütunundan h sütununa)
            if not square_char: # Kare boşsa
                empty_squares_count += 1
            else: # Karede taş varsa
                if empty_squares_count > 0:
                    fen_rank_str += str(empty_squares_count)
                    empty_squares_count = 0
                fen_rank_str += square_char # Taşın FEN karakterini ekle
        
        if empty_squares_count > 0:
            fen_rank_str += str(empty_squares_count)
        
        fen_ranks.append(fen_rank_str)

    return "/".join(fen_ranks)


if __name__ == '__main__':
    try:
        # 1. Modeli yükle
        print("Test amacıyla model yükleniyor...")
        chess_model = load_chess_model()
        
        # 2. Test edilecek resmin yolunu belirt
        test_image_path = r"C:\Users\ronarduino\Desktop\img1.jpg" # Test için kullandığımız resim

        if not os.path.exists(test_image_path):
             print(f"UYARI: Test resmi yolu geçersiz: {test_image_path}")
        else:
            print(f"\nTest edilecek resim: {test_image_path}")
            
            # 3. FEN notasyonunu al
            fen = get_fen_from_image(chess_model, test_image_path)
            
            # 4. Sonucu yazdır
            if fen:
                print("\n" + "="*50)
                print(f"Test Sonucu FEN Notasyonu: {fen}")
                print("="*50)
            else:
                print("Bu resimden FEN notasyonu üretilemedi.")

    except Exception as e:
        print(f"Test sırasında genel bir hata oluştu: {e}")