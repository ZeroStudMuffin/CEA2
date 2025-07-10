import pandas as pd
from pathlib import Path

from ocr_parser import parse_pdf_labels

PDF_PATH = Path("EXtract.pdf")
CSV_PATH = Path("Extract_ground_truth - Sheet1.csv")


def test_page_count_matches_ground_truth():
    gt = pd.read_csv(CSV_PATH)
    results = parse_pdf_labels(str(PDF_PATH), pages=(1, len(gt)))
    assert len(results) == len(gt)


def test_first_three_pages_exact_match():
    gt = pd.read_csv(CSV_PATH).iloc[:3]
    results = parse_pdf_labels(str(PDF_PATH), pages=(1, 3))
    for (pred_roll, pred_customer), (_, row) in zip(results, gt.iterrows()):
        assert pred_roll == str(row.roll_true)
        assert pred_customer.lower()[:5] == row.customer_true.lower()[:5]


def test_bad_page_returns_values():
    # page 18 is omitted from accuracy checks but should still return values
    results = parse_pdf_labels(str(PDF_PATH), pages=(18, 18))
    roll, customer = results[0]
    assert roll is None or isinstance(roll, str)
    assert customer is None or isinstance(customer, str)


def test_overall_accuracy():
    gt = pd.read_csv(CSV_PATH)
    # drop pages 12 and 18 from evaluation
    skip = [11, 17]
    results = parse_pdf_labels(str(PDF_PATH), pages=(1, len(gt)))

    correct = 0
    total = 0
    for idx, ((pred_roll, pred_customer), (_, row)) in enumerate(zip(results, gt.iterrows())):
        if idx in skip:
            continue
        total += 1
        roll_ok = pred_roll == str(row.roll_true)
        cust_ok = pred_customer and pred_customer.lower()[:5] == row.customer_true.lower()[:5]
        if roll_ok and cust_ok:
            correct += 1

    accuracy = correct / total
    assert accuracy >= 0.7


def test_accuracy_with_height_filter():
    gt = pd.read_csv(CSV_PATH)
    skip = [11, 17]
    thresholds = [0.0, 0.02, 0.04]

    best = 0.0
    for thr in thresholds:
        results = parse_pdf_labels(str(PDF_PATH), pages=(1, len(gt)), min_height_ratio=thr)
        correct = 0
        total = 0
        for idx, ((pred_roll, pred_customer), (_, row)) in enumerate(zip(results, gt.iterrows())):
            if idx in skip:
                continue
            total += 1
            roll_ok = pred_roll == str(row.roll_true)
            cust_ok = pred_customer and pred_customer.lower()[:5] == row.customer_true.lower()[:5]
            if roll_ok and cust_ok:
                correct += 1
        best = max(best, correct / total)

    assert best >= 0.7
