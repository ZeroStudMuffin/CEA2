## FEATURE:

- App home screen with a button to go into "bin locator" which will house this FEATURE
- Takes a picture, rotates image so text is correctly oriented, preprocesses image for MLkit OCR
- Bounding box shows where the image will be cropped. Bounding box in landscape orientation and cover about 70% of the full size image.
- Button captures the image for use with MLkit.
- Text from the image is OCR'd and shown to the user in a popup window
- Indicate the "top" of the screen so users know what orientation to hold the device.
- Allow for landscape or portrait modes. Switch orientation with a small round button, no auto-rotate.


## EXAMPLES:

https://github.com/android/nowinandroid?tab=readme-ov-file - a simple android app EXAMPLE.
https://github.com/ryccoatika/Image-To-Text - an image-to-text app example
https://github.com/krishnachaitanya0107/TextRecognizerApp/blob/master/app/src/main/java/com/example/textrecognizer/MainActivity.kt - the mainactivity.kt example from another image-to-text app

## DOCUMENTATION:

https://developer.android.com/topic/architecture -app architecture. use this as one of the main guides for best practives and inspiration.
https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
https://developer.android.com/ - a very high quality resource for all things android development focused. Search the subdomains for more relevant topics to your task.


## OTHER CONSIDERATIONS:

Ensure tests pass
Download Android SDK when needed. If possible, add the SDK to the repo.
User will help when asked. Work with the user if something cannot be handled bu Codex alone.