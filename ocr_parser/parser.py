"""OCR parsing logic for roll numbers and customer names."""

from __future__ import annotations

import re
from dataclasses import dataclass
from typing import List, Tuple, Optional

from pdf2image import convert_from_path
from PIL import Image
import pytesseract


@dataclass
class OCRLine:
    """A single OCR line."""

    text: str
    left: int
    top: int
    width: int
    height: int
    conf: float


# Reason: grouping words to lines simplifies downstream parsing


def ocr_image(
    image: Image.Image, psm: int = 3, min_height_ratio: float = 0.0
) -> List[OCRLine]:
    """Run OCR on an image and return lines with bounding boxes.

    This helper mimics the structure returned by **ML Kit** text recognition
    so the calling code is ML Kit compatible. Tesseract is used in tests as a
    stand-in engine.

    Args:
        image: Source image.
        psm: Tesseract page segmentation mode.
        min_height_ratio: Filter out lines smaller than this fraction of the
            image height.
    """
    config = f"--psm {psm}"
    data = pytesseract.image_to_data(
        image, config=config, output_type=pytesseract.Output.DICT
    )
    lines = {}
    for i, text in enumerate(data["text"]):
        if not text.strip():
            continue
        line_no = data["line_num"][i]
        left = data["left"][i]
        top = data["top"][i]
        width = data["width"][i]
        height = data["height"][i]
        conf = float(data["conf"][i])
        line = lines.setdefault(
            line_no,
            {
                "text": [],
                "left": left,
                "top": top,
                "right": left + width,
                "bottom": top + height,
                "confs": [],
            },
        )
        line["text"].append(text)
        line["confs"].append(conf)
        line["left"] = min(line["left"], left)
        line["top"] = min(line["top"], top)
        line["right"] = max(line["right"], left + width)
        line["bottom"] = max(line["bottom"], top + height)

    results: List[OCRLine] = []
    for info in lines.values():
        line_text = " ".join(info["text"]).strip()
        left = info["left"]
        top = info["top"]
        width = info["right"] - info["left"]
        height = info["bottom"] - info["top"]
        conf = sum(info["confs"]) / len(info["confs"])
        if min_height_ratio and height / image.height < min_height_ratio:
            continue
        results.append(OCRLine(line_text, left, top, width, height, conf))

    # sort by vertical position
    results.sort(key=lambda l: l.top)
    return results


def mlkit_ocr_image(
    image: Image.Image, psm: int = 3, min_height_ratio: float = 0.0
) -> List[OCRLine]:
    """Alias for :func:`ocr_image` representing the ML Kit-compatible API."""
    return ocr_image(image, psm=psm, min_height_ratio=min_height_ratio)


def _refine_roll(image: Image.Image, line: OCRLine) -> Optional[str]:
    """Attempt to improve OCR accuracy by re-reading the roll line."""
    # Reason: a slightly larger crop helps capture digits that may be cut off
    margin = 20
    left = max(line.left - margin, 0)
    top = max(line.top - margin, 0)
    right = line.left + line.width + margin
    bottom = line.top + line.height + margin
    crop = image.crop((left, top, right, bottom))
    text = pytesseract.image_to_string(
        crop,
        config="--psm 7 --oem 1 -c tessedit_char_whitelist=ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
    )
    match = re.search(r"([A-Za-z]{0,2}\d{6,10})", text)
    if match:
        return _normalize_roll(match.group(1)) or match.group(1)
    return None


def _clean_customer_text(text: str) -> str:
    """Clean a raw OCR line to obtain a customer name."""
    text = text.replace("_", " ")
    tokens = text.split()
    cleaned: list[str] = []
    for token in tokens:
        if re.search(r"\d{1,2}[/-]\d{1,2}", token):
            break
        token_clean = re.sub(r"[^A-Za-z0-9#]+", "", token)
        if token_clean.upper().startswith("PO"):
            break
        if re.fullmatch(r"\d{3,}", token_clean):
            break
        cleaned.append(token_clean)
        if re.search(r"#\d+$", token_clean):
            break
    return " ".join(cleaned).strip().rstrip(".")


def _normalize_roll(text: str) -> Optional[str]:
    """Normalize a raw roll string by stripping spurious letters."""
    m = re.search(r"([A-Za-z]{0,2}\d{6,10})", text)
    if not m:
        return None
    roll = m.group(1)
    digits = re.search(r"\d{6,10}", roll)
    if digits and len(digits.group(0)) == 8 and len(roll) == 9:
        # Many noise cases prepend a stray letter to an 8-digit roll
        roll = digits.group(0)
    return roll


def _parse_text_lines(lines: List[str]) -> Tuple[str | None, str | None]:
    """Parse roll and customer from plain OCR text lines."""
    roll_re = re.compile(r"([A-Za-z]{0,2}\d{6,10})")
    roll: str | None = None
    roll_idx: int | None = None
    for i, line in enumerate(lines):
        m = roll_re.search(line)
        if m:
            roll = _normalize_roll(m.group(1)) or m.group(1)
            roll_idx = i
            break
    customer: str | None = None
    if roll_idx is not None:
        for cand in lines[roll_idx + 1 : roll_idx + 6]:
            cleaned = _clean_customer_text(cand)
            if cleaned:
                customer = cleaned
                break
    return roll, customer


def parse_label(
    lines: List[OCRLine], image: Optional[Image.Image] = None
) -> Tuple[str | None, str | None]:
    """Extract roll number and customer name using bounding boxes."""

    roll_line: OCRLine | None = None
    roll = None
    # look for a roll number consisting of 6-10 digits with an optional
    # alphabetic prefix. Some labels encode the roll with a leading letter
    # like "F609890101" or "YB91025" so we allow up to two letters.
    roll_re = re.compile(r"\b([A-Za-z]{0,2}\d{6,10})\b")
    for line in lines:
        match = roll_re.search(line.text)
        if match:
            roll = _normalize_roll(line.text)
            if not roll:
                roll = match.group(1)
            roll_line = line
            if image is not None:
                refined = _refine_roll(image, line)
                if refined:
                    roll = _normalize_roll(refined) or refined
            break

    if roll is None and image is not None:
        # Fallback: OCR the entire page allowing only alphanumerics and
        # search for a plausible roll pattern. This helps when the line
        # segmentation fails entirely.
        text = pytesseract.image_to_string(
            image,
            config="--psm 6 -c tessedit_char_whitelist=ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        )
        match = roll_re.search(text)
        if match:
            roll = _normalize_roll(match.group(1)) or match.group(1)

    customer = None
    if roll_line:
        # candidate lines below the roll line
        below = [l for l in lines if l.top > roll_line.top]
        below.sort(key=lambda l: l.top)
        for cand in below:
            if not re.search(r"[A-Za-z]", cand.text):
                continue
            customer = _clean_customer_text(cand.text)
            if customer:
                break

    if (roll is None or customer is None) and image is not None:
        # Fallback: parse from plain OCR text lines
        text = pytesseract.image_to_string(image, config="--psm 6")
        text_lines = [t.strip() for t in text.splitlines() if t.strip()]
        alt_roll, alt_cust = _parse_text_lines(text_lines)
        if roll is None:
            roll = alt_roll
        if customer is None:
            customer = alt_cust

    return roll, customer


def parse_pdf_labels(
    pdf_path: str,
    pages: Tuple[int, int] | None = None,
    min_height_ratio: float = 0.0,
) -> List[Tuple[str | None, str | None]]:
    """Parse a PDF of labels returning roll and customer for each page.

    Args:
        pdf_path: PDF to parse.
        pages: Optional (first, last) page numbers.
        min_height_ratio: Passed through to :func:`ocr_image`.
    """
    first, last = (1, None)
    if pages:
        first, last = pages
    images = convert_from_path(pdf_path, dpi=200, first_page=first, last_page=last)
    results = []
    for img in images:
        lines = mlkit_ocr_image(img, min_height_ratio=min_height_ratio)
        results.append(parse_label(lines, img))
    return results
