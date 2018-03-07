To start a new year:

Copy folder to a new year.

Edit categories.csv as a spreadsheet
1. Update the categories as a few change each year
2. Make sure the categories are in the same order as the export file
3. Edit the titles so the tie-breakers in parenthesis are correct
4. Add all the nominees under each category in the order you want them (usually alphabetical)
5. Save in CSV (DOS) format as categories.csv

Delete categoryMaps.xml
Delete category\*.*
Delete player\*.*
Delete results.xml

Run oscars.bat or ./oscars.sh

Update the link in ../index.html to point to current year.

Download the survey results as an Excel file.
Remove any columns not in categories.csv
Verify columns are in same order as categories.csv
Update any names where they didn't capitalize properly
Add PSEUDO- at the front of the first name of anyone that isn't an actual player
Remove time guess for any PSEUDO players that didn't have a guess
Remove the header row so all you have is the survey data
IMPORTANT: Use search and replace to remove any commas
Save in CSV (DOS) format as players.csv

You will be prompted on how to map survey answers to nominees for each category.
This will be saved so you only have to do this for new survey answers.
If any mistakes are made you must edit (or delete) categoryMaps.xml and start oscars.bat or oscars.sh over again.
