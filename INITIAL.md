- [ ] Debug mode tuning: In debug mode, a button brings up a menu with all the tuning "knobs" for parsing and preprocessing.
- [ ] Tuning knobs are derived from the tuning example file. There are 7 knobs detailedin the file. If Currently we do not have defined values for some of the knobs, add a default value that seems appropriate. The guide gives some decent defaults for most or all of the parameters.
- [ ] Uses sliders and text boxes for user input. Label all the knobs. Derive the possible values from the guide.
- [ ] Accept tuning changes with a button and apply them to debug mode only.
- [ ] Tuning knob changes do not get saved across app reboots.
- [ ] Additional tuning knobs: The "percent height of tallest line" will be tunable. 100% = only the tallest line (or equal). 0% = all lines regardless of height.

## EXAMPLES:
examples/pipeline_tuning_guide.md - helpful for understanding the tuning knobs that are available and how they work.
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
