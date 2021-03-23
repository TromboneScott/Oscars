To start a new year:

Copy year folder to oscars site and rename to current year.

Edit categories.csv as a spreadsheet
1. Update the categories as a few change each year
2. Make sure the categories are in the same order as the export file
3. Edit the titles so the tie-breakers in parenthesis are correct
4. Add all the nominees under each category in the order you want them (usually alphabetical)
5. Save in CSV (DOS) format as categories.csv

Delete these if they exist:
File: categoryMaps.xml
Folder: category
Folder: player
File: results.xml

From the new year folder run oscars.bat or ./oscars.sh

Update the link in ../index.html to point to current year.


--- After contest deadline for entering guesses

Download the survey results as an Excel file.
Remove any columns not in categories.csv
Verify columns are in same order as categories.csv
Update any names where they didn't capitalize properly
Remove any duplicated names so we use the last entry for each person
Remove the header row so all you have is the survey data
IMPORTANT: Use search and replace to remove any commas
Save in CSV (DOS) format as players.csv

You will be prompted on how to map survey answers to nominees for each category.
This will be saved so you only have to do this for new survey answers.
If any mistakes are made you must edit (or delete) categoryMaps.xml and start oscars.bat or oscars.sh over again.

Go to site44.com and click on the refresh button after each category is updated so that it will update the website.