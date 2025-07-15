1. Gaussian Blur Kernel (Size: 5×5)
<Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)>
What it does: Smooths out high-frequency noise (e.g. small specks, sensor grain) so that edge detection doesn’t pick up every tiny artifact.

Why it matters: Too little blur → noisy Canny edges and lots of spurious contours. Too much blur → edges get “rounded off” and you lose fine detail (corners become less sharp).

How to tune:

If you see a lot of tiny irrelevant contours → increase kernel to 7×7 or even 9×9.

If your label’s corners look rounded/missing → drop to 3×3.

Always use an odd size (3,5,7,9…).

2. Canny Edge Thresholds (50, 150)
<Imgproc.Canny(gray, edges, 50.0, 150.0)>
What it does: Marks pixels as edges if the gradient magnitude crosses the high threshold (150), and “grows” edges down to the low threshold (50) to connect weak edges that are adjacent to strong ones.

Why it matters:

Low threshold too low → picks up faint noise.

High threshold too high → you’ll miss legitimate edges (corners of your label may disappear).

How to tune:

Start with the “ratio” rule of thumb: high ≈ 3 × low.

If your printed label is very high-contrast, you can bump the low threshold up to 80 or 100 to reject paper grain.

If edges look dashed/broken, lower the low threshold to 30 or even 20.

3. Dilation Kernel (3×3)
<Imgproc.dilate(edges, edges, getStructuringElement(MORPH_RECT, Size(3.0,3.0)))>
What it does: Grows (“thickens”) each white pixel in the edge map, closing small gaps.

Why it matters: Broken edges can cause your quad approximation to fail or produce more than four sides.

How to tune:

If you still see dashed edges, increase to 5×5.

If the shape “bulges” too much, stick with 3×3 or even 2×2.

4. approxPolyDP Epsilon (10.0)
<Imgproc.approxPolyDP(contour2f, poly, 10.0, true)>
What it does: Simplifies a curve (your raw contour) into a polygon whose vertices lie on the contour. Epsilon controls how “coarse” that simplification is.

Why it matters:

Low epsilon (e.g. 2–5) → polygon will hug every wiggle, might have dozens of points.

High epsilon (15–20) → polygon will be very blocky, might collapse non-rectangles into 4 corners.

How to tune:

If you get many more than 4 points on a clean rectangle → increase epsilon.

If your rectangle looks skewed (triangle or pentagon) → decrease epsilon.

5. MIN_AREA
<if (r.area() > MIN_AREA) { … }>
What it does: Discards contours whose bounding-box area is too small to be your label.

Why it matters: Tiny specks or small objects in the scene can otherwise be mistaken for tiny “rectangles.”

How to tune:

Roughly estimate: if your label occupies 25% of the image in normal framing, MIN_AREA = (imageWidth × imageHeight) × 0.1.

If you move farther away (label appears small), lower MIN_AREA; moving closer, raise it.

6. TARGET_RATIO & RATIO_TOLERANCE
<val ratio = r.width.toDouble() / r.height
if (abs(ratio - TARGET_RATIO) < RATIO_TOLERANCE) { … }>
What it does: Keeps only quads whose width/height matches your known label aspect ratio (e.g. 4.0 for a 4:1 label).

Why it matters: Eliminates all rectangles that aren’t your label: signage, logos, screens, phone displays, etc.

How to tune:

If your label is exactly 4:1, set TARGET_RATIO = 4.0.

Choose RATIO_TOLERANCE = 0.05–0.15 (±5–15%) depending on how much perspective/skew you expect.

7. Output Size (LABEL_W×LABEL_H)
<Imgproc.warpPerspective(..., Size(LABEL_W.toDouble(), LABEL_H.toDouble()))>
What it does: Normalizes every cropped label to the same pixel dimensions before OCR.

Why it matters: OCR accuracy benefits from a consistent text size and resolution.

How to tune:

If your labels have fine print, bump up resolution (e.g. 800×200 px for a 4:1 label).

If speed is paramount and text is large, you can go as low as 300×75 px or so.

Putting It All Together
Start coarse: pick mid-range values (5×5 blur, 50/150 Canny, epsilon ≈10, tolerance ≈0.1, MIN_AREA ≈10% of frame).

Visualize: draw each stage (blurred, edges, filtered quads) on-screen so you can see what’s getting through.

Adjust one knob at a time: e.g. only tweak Canny thresholds until edges look clean, then move on to epsilon, etc.

Lock in when you see reliable rectangle detection under your typical lighting and framing