"""Genera planos "apilados": cada planta muestra debajo las plantas inferiores atenuadas.

Uso: python3 scripts/apilar_plantas.py "app/src/main/assets/Modelo 3D ETSIIT/ConSuelo" "app/src/main/assets/Modelo 3D ETSIIT/Apiladas"
Requiere: numpy, scipy, pillow. Los renders deben tener la misma cámara (el suelo de fondo es idéntico).
"""
import sys
import numpy as np
from scipy import ndimage
from PIL import Image, ImageFilter

src, out = sys.argv[1], sys.argv[2]
floors = ['-1', '0', '1', '2', '3', '4']
ims = {f: np.asarray(Image.open(f'{src}/{f}.jpg').convert('RGB')).astype(np.int16) for f in floors}

def building_mask(f):
    """True donde la imagen f muestra edificio (no coincide con el suelo de otra planta)."""
    me = ims[f]
    match = np.zeros(me.shape[:2], bool)
    for o in floors:
        if o == f or o == '-1':  # -1 tiene terreno propio (césped, patio), no sirve de referencia
            continue
        match |= np.abs(me - ims[o]).sum(2) < 18
    # sombras sobre el suelo: oscuras y poco saturadas -> también son suelo
    lum = me.mean(2)
    sat = me.max(2) - me.min(2)
    ground_like = (lum < 95) & (sat < 28)
    shadow = (lum < 85) & (sat < 22)
    m = ~((match & ground_like) | shadow)
    # rellenar agujeros pequeños (detalles oscuros del interior); los patios grandes se mantienen
    holes, n = ndimage.label(~m)
    sizes = ndimage.sum(np.ones_like(m), holes, range(1, n + 1))
    small = np.isin(holes, np.nonzero(sizes < 400)[0] + 1)
    m |= small
    img = Image.fromarray((m * 255).astype(np.uint8))
    img = img.filter(ImageFilter.MinFilter(3)).filter(ImageFilter.MaxFilter(3))  # quita ruido
    img = img.filter(ImageFilter.MaxFilter(3)).filter(ImageFilter.MinFilter(3))  # rellena huecos
    return img.filter(ImageFilter.GaussianBlur(0.8))  # borde suave

masks = {f: building_mask(f) for f in floors if f != '-1'}

for i, f in enumerate(floors):
    canvas = Image.fromarray(ims['-1'].astype(np.uint8))  # base: sótano con terreno
    if f != '-1':
        # atenuar todo lo que hay por debajo para que destaque la planta actual
        canvas = Image.blend(canvas, Image.new('RGB', canvas.size, (40, 36, 34)), 0.45)
        for lower in floors[1:i]:
            layer = Image.fromarray(ims[lower].astype(np.uint8))
            layer = Image.blend(layer, Image.new('RGB', layer.size, (40, 36, 34)), 0.35)
            canvas.paste(layer, (0, 0), masks[lower])
        # sombra suave de la planta actual sobre las de abajo
        shadow = masks[f].filter(ImageFilter.GaussianBlur(6)).point(lambda v: int(v * 0.55))
        canvas.paste(Image.new('RGB', canvas.size, (0, 0, 0)), (6, 8), shadow)
        canvas.paste(Image.fromarray(ims[f].astype(np.uint8)), (0, 0), masks[f])
    canvas.save(f'{out}/{f}.jpg', quality=90)
