## FEATURE:

- [ ] Goal: End up with only Roll#s and Customer names after parsing
- [ ] add to or modify the currect parser, do no create a new one. The additions will happen after the current parsing logic. (this is for further parsing)
- [ ] Rules: 1. strings with known words become the customer name, then line with the most characters (excluding the newly set customer name line) becomes the roll#. 2. If no known words are detected, the line with the most numbers becomes the roll#, then the next line with the most characters becomes the customer name. 
- [ ] The final result is what is shown in the text box on screen. Previosuly it was showing the full OCR text, now It will show only the parsed (final) ocr text. Roll#s will be the first line structered as "Roll#:<extracted roll#>" and the customer name will be "Cust-Name:<extracted customer name>"
## EXAMPLES:

https://github.com/android/nowinandroid?tab=readme-ov-file - a simple android app EXAMPLE.
https://github.com/ryccoatika/Image-To-Text - an image-to-text app example
https://github.com/krishnachaitanya0107/TextRecognizerApp/blob/master/app/src/main/java/com/example/textrecognizer/MainActivity.kt - the mainactivity.kt example from another image-to-text app

## DOCUMENTATION:

https://developer.android.com/topic/architecture -app architecture. use this as one of the main guides for best practives and inspiration.
https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
https://developer.android.com/ - a very high quality resource for all things android development focused. Search the subdomains for more relevant topics to your task.

## OTHER CONSIDERATIONS:

User will help when asked. Work with the user if something cannot be handled bu Codex alone.
