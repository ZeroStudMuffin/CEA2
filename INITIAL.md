## FEATURE:

- [ ] Checkbox above the debug checkbox enables "Batch Binning" mode
- [ ] Batch binning changes capture button to a square and adds a button to the right of the capture button called "Add Item"
- [ ] Capture button works like normal.
- [ ] Add Item button adds the current roll number and customer name to a list, then clears the textview, and gets ready for another capture.
- [ ] List of items ready to have a bin number added is available in a pop-up window similar to the "OCR raw text" button and pop-up wundow.
- [ ] When Set Bin is used, set the bin for all records in the list as well as the one that had just been captured but was not added to the list yet.
- [ ] Send Record now sends each item on the list. Each item is sent as its own record. After sending the records, everything is cleared and made ready for another batch.

## EXAMPLES:

https://github.com/android/nowinandroid?tab=readme-ov-file - a simple android app EXAMPLE.
https://github.com/ryccoatika/Image-To-Text - an image-to-text app example
https://github.com/krishnachaitanya0107/TextRecognizerApp/blob/master/app/src/main/java/com/example/textrecognizer/MainActivity.kt - the mainactivity.kt example from another image-to-text app

## DOCUMENTATION:

https://developer.android.com/topic/architecture -app architecture. use this as one of the main guides for best practives and inspiration.
https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
https://developer.android.com/ - a very high quality resource for all things android development focused. Search the subdomains for more relevant topics to your task.

## OTHER CONSIDERATIONS:

The pupose of this feature is to allow users to gather multiple roll number/customer name records and set the bin location for all of them at once. Then send the records all at once.
If extra info is needed like keys or tokens, set up the code for them and inform the user after completion of what needs to be added. User will give the info before executing the PRP.
User will help when asked. Work with the user if something cannot be handled bu Codex alone.
