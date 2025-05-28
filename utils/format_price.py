def format_price(price):
    """Fiyatı daha okunabilir formata çevirir"""
    if price >= 1000000:
        return f"{price/1000000:.1f} Milyon TL"
    elif price >= 1000:
        return f"{price/1000:.1f} Bin TL"
    return f"{price} TL" 