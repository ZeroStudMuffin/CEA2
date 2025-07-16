## FEATURE:

- [ ] New module. Button on home screen called "Checkout Items". Layout is same as bin locator but with different buttons and functionality.
- [ ] "checkout Items" should use as much of the existing code as possible to eliminate bloat.
- [ ] Same as bin locator batch mode, add items and show list are available. The rest are hidden.
- [ ] Instead of "Send Record" there is a "Checkout" button that brings up a confirmation pop-up window. Confirmation sends the update.
- [ ] checkout.php expects a roll_num, customer, and the pin used when starting the app. If all are given and the item is not already checkout out or missing from the database, it updates the records as "checked out"

## EXAMPLES:

examples/checkout.php - this is the actual checkout.php that will be used.
https://github.com/android/nowinandroid?tab=readme-ov-file - a simple android app EXAMPLE.

## DOCUMENTATION:

https://developer.android.com/topic/architecture -app architecture. use this as one of the main guides for best practives and inspiration.
https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
https://developer.android.com/ - a very high quality resource for all things android development focused. Search the subdomains for more relevant topics to your task.

## OTHER CONSIDERATIONS:

If extra info is needed like keys or tokens, set up the code for them and inform the user after completion of what needs to be added. User will give the info before executing the PRP.
User will help when asked. Work with the user if something cannot be handled bu Codex alone.
