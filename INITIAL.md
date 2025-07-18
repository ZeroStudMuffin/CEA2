## FEATURE:

- [ ] Developer login: when the app is logged (on boot) and the pin used was 8789, enable developer mode.
- [ ] Developer mode: debug mode checkbox relocated to developer mode. From now on, all debug and tuning related things will reside in developer mode.
- [ ] New developer module: Preproccessing debug mode. This new module allows developers to see exactly how preprocessing parameters effect the image being sent to MLkit.
- [ ] Preprocess debug module does not use MLkit, it stops right after all the preprocessing is finished so the developer can see the processed image.
- [ ] Preprocess debug module shows the processed image at the top of the screen without the need for a button press.
- [ ] Preprocess debug module does not have the normal buttons. It only has capture and tune buttons. No need for show crop as the image automatically shows.

## EXAMPLES:

examples/image_processing has files from another android app.

## DOCUMENTATION:

https://developer.android.com/topic/architecture -app architecture. use this as one of the main guides for best practives and inspiration.
https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md

## OTHER CONSIDERATIONS:

If extra info is needed like keys or tokens, set up the code for them and inform the user after completion of what needs to be added. User will give the info before executing the PRP.
User will help when asked. Work with the user if something cannot be handled bu Codex alone.
