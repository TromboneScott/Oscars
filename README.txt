NOTE FOR 2023:

When creating the Google Form include the names of the people that are nominated in all categories that include them.  That way we'll be able to match them up with the correct winner during the broadast if they only annouce the name of the person and not the name of the movie.

We won't be able to use that for any nominees that no one guesses as those descriptions won't be downloaded from the Google Form.  But those nominees are highly unlikely to win.

----

To start a new year:

Create Google Form:
1. Open last year's form at forms.google.com
2. Select the dots menu at top right and click "Make a copy"
3. Make all necessary updates for the current year
  a. Change any categories and tie-breakers
  b. Update the broadcast date
  c. Update nominees
4. Set up data export
  a. Click Responses tab
  b. Click the green spreadsheet button to Create Spreadsheet
  c. Open responses spreadsheet
  d. Select File/Share/Publish to web
  e. Choose Link = Form Responses 1 (the tab name) and Embed = Comma-separated values (.csv)
  f. Check box for "Automatically rebulish when changes are made"
  g. Click "Publish"
  h. Copy the URL it generates and use that on the command line when running the oscars programs

Update any code and files as needed
1. git add <file> (for each <file>)
2. git commit
3. git push
4. git tag -f <year> (for current year)
5. git push -f --tags

Copy year folder to oscars site and rename to current year.

Edit categories.csv as a spreadsheet
1. Update the categories as a few change each year
2. Make sure the categories are in the same order as the export file
3. Edit the titles so the tie-breakers in parenthesis are correct
4. Add all the nominees under each category in the order you want them (usually alphabetical)
5. Replace any commas (,) with back-quotes (`)
6. Save in CSV (DOS) format as categories.csv

Delete this file if it exists:
File: categoryMaps.xml

From the new year folder run oscars.bat or ./oscars.sh

Update the link in ../index.html to point to current year.


--- After contest deadline for entering guesses

Download the survey results as an Excel file.
Update any names where they didn't capitalize properly
Replace any necessary commas (,) with back-quotes (`) in names
Remove any duplicated names so we use the last entry for each person
 - Sort by: Last Name, First Name, Timestamp to find duplicates
Remove any columns not in categories.csv
Verify columns are in same order as categories.csv
Remove the header row so all you have is the survey data
IMPORTANT: Use search and replace to remove any commas
Save in CSV (DOS) format as players.csv

You will be prompted on how to map survey answers to nominees for each category.
This will be saved so you only have to do this for new survey answers.
If any mistakes are made you must edit (or delete) categoryMaps.xml and start oscars.bat or oscars.sh over again.

Go to site44.com and click on the refresh button after each category is updated so that it will update the website.
