#!/usr/bin/env python3
"""Standard for Quran text in Daily Dua.

  python3 tools/quran_text.py normalize   # rewrite data files to the standard
  python3 tools/quran_text.py check       # fail if any Arabic character would render broken

The standard:
  * Uthmani script (Tanzil / quran-json), one DuaLine per verse.
  * Open tanween uses the dedicated code points the Amiri Quran font draws correctly:
      U+0657 (inverted damma, used by Tanzil for open fathatan)  -> U+08F0
      U+065E (fatha with two dots, Tanzil open dammatan)          -> U+08F1
      U+0656 (subscript alef, Tanzil open kasratan)               -> U+08F2
  * Every Arabic character must exist in res/font/amiri_quran.ttf, otherwise Android
    falls back to another font for that one mark and the word breaks apart.
"""
import glob, os, sys, unicodedata

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "app/src/main/java/com/dailydua/tracker/data")
FONT = os.path.join(ROOT, "app/src/main/res/font/amiri_quran.ttf")
MAP = {"ٗ": "ࣰ", "ٞ": "ࣱ", "ٖ": "ࣲ"}


def arabic(ch):
    o = ord(ch)
    return 0x0600 <= o <= 0x08FF or 0xFB50 <= o <= 0xFEFF


def files():
    return sorted(glob.glob(os.path.join(DATA, "*.kt")))


def normalize():
    for p in files():
        s = open(p, encoding="utf-8").read()
        t = s.translate(str.maketrans(MAP))
        if t != s:
            open(p, "w", encoding="utf-8").write(t)
            print("normalized", os.path.basename(p))


def check():
    from fontTools.ttLib import TTFont
    cmap = TTFont(FONT).getBestCmap()
    bad = 0
    for p in files():
        for n, line in enumerate(open(p, encoding="utf-8"), 1):
            for ch in set(line):
                if not arabic(ch):
                    continue
                if ch in MAP:
                    print(f"{os.path.basename(p)}:{n}: U+{ord(ch):04X} {unicodedata.name(ch,'?')} - run normalize")
                    bad += 1
                elif ord(ch) not in cmap:
                    print(f"{os.path.basename(p)}:{n}: U+{ord(ch):04X} {unicodedata.name(ch,'?')} is not in the Quran font")
                    bad += 1
    if bad:
        sys.exit(f"{bad} problem(s) in Quran text")
    print("Quran text OK")


if __name__ == "__main__":
    {"normalize": normalize, "check": check}[sys.argv[1] if len(sys.argv) > 1 else "check"]()
