## FEATURE:

- Instead of a popup window, the text is written in a text box on the top of the screen.
- 2 buttons on the screen after OCR with OCR results. 1 button called "Get Release" and the other button called "Set Bin".
- Get release button adds a "release number" to the OCRd record. The button searches for a barcode yeilding 7 digits. The 7 digit string is the release number. if not found, write a temporary message that fades away saying "no release found".
- Set Bin button adds a "bin location" to the OCRd record. The button searches for a QR code that yields "BIN:<#> UNTIED EXPRESS". The # is the bin location. If not found, write a temporary message that fades away saying "no bin found".

## EXAMPLES:

https://github.com/android/nowinandroid?tab=readme-ov-file - a simple android app EXAMPLE.
https://github.com/ryccoatika/Image-To-Text - an image-to-text app example
https://github.com/krishnachaitanya0107/TextRecognizerApp/blob/master/app/src/main/java/com/example/textrecognizer/MainActivity.kt - the mainactivity.kt example from another image-to-text app

## DOCUMENTATION:

https://developer.android.com/topic/architecture -app architecture. use this as one of the main guides for best practives and inspiration.
https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
https://developer.android.com/ - a very high quality resource for all things android development focused. Search the subdomains for more relevant topics to your task.


## OTHER CONSIDERATIONS:
Instructions already exist but the order of operations might be causing failures. Asses the order in which operations should be executed for proper setup.
User will help when asked. Work with the user if something cannot be handled bu Codex alone.