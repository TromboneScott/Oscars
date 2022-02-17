To start a new year:

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

Delete these if they exist:
File: categoryMaps.xml
Folder: rank
Folder: category
Folder: player
File: results.xml

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
Use search and replace to remove any special characters like á and é
Save in CSV (DOS) format as players.csv

You will be prompted on how to map survey answers to nominees for each category.
This will be saved so you only have to do this for new survey answers.
If any mistakes are made you must edit (or delete) categoryMaps.xml and start oscars.bat or oscars.sh over again.

Go to site44.com and click on the refresh button after each category is updated so that it will update the website.
