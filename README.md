# Recipe_Buddy
An android application which scrapes only the essential recipe information from BBCGoodFood.com and Food.com. This app will be able to localise all you're recipes into one place, either from the websites mentioned previously or by adding them manually. All recipes will be linked to a unique account that you either create or authenticate through a google account. Security is a give since this application uses Firebase to manage all data and user accounts. No need to worry about account information being leaked or hacked.

# Pre-Requisites
If wanting to add on top of this app:
- This application uses Firebase. You will need to create your own Firestore project to get set up on your own machine

When using this app for the first time make sure to have an internet connection since this app relies heavily on connection to Firebase

# Features
- Google login/signup integration
- Firebase data storage
- Ability to add recipes from BBCGoodFood.com & Food.com
- Database storage is saved through "shared" recipes
  - when multiple users attempt to add a recipe from the same site
- Abitlity to manually add recipes
- Limited offline support through Firebase
- Ability to accept/deny application's access to device storage (image gallery)
