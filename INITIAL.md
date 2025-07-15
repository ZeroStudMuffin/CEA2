## FEATURE:

- [ ] Modified pipeline from image to OCR result.
- [ ] still uses the current crop box as the first pre-processing step.
- [ ] modified pipeline starts after the intial crop.
- [ ] uses the labels aspect ratio (can be inferred from the crop box visual aids aspect ratio) to identify a likely candidate for the label. This allows for further cropping before OCR.
- [ ] compensates for labels that are not perfectly flat or at an angle.
- [ ] debug mode: "show crop" button no longer shows the bitmap
- [ ] debug mode: the image sent to MLkit gets saved temporarily. This image is available by using the "show crop" button.

## EXAMPLES:
Key example: examples/Image_to_OCR_pipeline.md - this is a guide on how the pipeline should work with a few code examples.
examples/pipeline_tuning_guide.md - helpful for understanding the tuning knobs that are available.
https://github.com/android/nowinandroid?tab=readme-ov-file - a simple android app EXAMPLE.
https://github.com/ryccoatika/Image-To-Text - an image-to-text app example.
https://github.com/krishnachaitanya0107/TextRecognizerApp/blob/master/app/src/main/java/com/example/textrecognizer/MainActivity.kt - the mainactivity.kt example from another image-to-text app.

## DOCUMENTATION:

https://developer.android.com/topic/architecture -app architecture. use this as one of the main guides for best practives and inspiration.
https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
https://developer.android.com/ - a very high quality resource for all things android development focused. Search the subdomains for more relevant topics to your task.

## OTHER CONSIDERATIONS:

If extra info is needed like keys or tokens, set up the code for them and inform the user after completion of what needs to be added. User will give the info before executing the PRP.
User will help when asked. Work with the user if something cannot be handled bu Codex alone.
