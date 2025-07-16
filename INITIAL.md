## FEATURE:

- [ ] Add "Batch Mode" checkbox to the home screen
- [ ] They may be "batch mode" items that have been changed to always on by default but, these features will be moved to the batch mode only: add item, show list, the current send record and set bin (the last 2 will be added in different forms in the new default mode)
- [ ] Default mode now pops up a large "BINS" menu when a roll# and customer name has been found. Pop up window has no label and takes up the entire screen except for where the capture button is. When clicked outside the menu (where the capture button was) the window disappears so user can attempt another capture.
- [ ] Menu is a set of buttons 5 wide and as long as it needs to be. Buttons scale vertically so no scrolling is needed to see all of them.
- [ ] Buttons have as large of text as possible while fitting in its box. Buttons go from 9-65 as well as F1-F4. (61 total buttons) The text is the number or F1-F4.
- [ ] Clicking a button sets the bin for the record, then sends the record. Then closes the menu and clears the textview. Ready for another capture.

## EXAMPLES:

https://github.com/android/nowinandroid?tab=readme-ov-file - a simple android app EXAMPLE.

## DOCUMENTATION:

https://developer.android.com/topic/architecture -app architecture. use this as one of the main guides for best practives and inspiration.
https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
https://developer.android.com/ - a very high quality resource for all things android development focused. Search the subdomains for more relevant topics to your task.

## OTHER CONSIDERATIONS:

If extra info is needed like keys or tokens, set up the code for them and inform the user after completion of what needs to be added. User will give the info before executing the PRP.
User will help when asked. Work with the user if something cannot be handled bu Codex alone.
