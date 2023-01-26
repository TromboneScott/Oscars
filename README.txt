To start a new year:

Download 100x180 pixel pictures for Google Form for these categories:
- Actor/Actress including Supporting
- Best Picture
- International Feature
- Animated Short
- Documentary Feature
- Animated Feature
1. Go to imdb.com
2. Use the search window to search for the movie or actor
3. Right-click on the little image of the correct movie or actor that appears in the search drop down and select Save image as...
4. Save the ~5Kb JPG file for use in the Google Form

Create Google Form:
1. Open last year's form at forms.google.com
2. Select the dots menu at top right and click "Make a copy"
3. Make all necessary updates for the current year
  a. Change tie-breakers 3 & 4
    i. Even years: 3 = Actress, 4 = Supporting Actor
    ii. Odd years: 3 = Actor, 4 = Supporting Actress
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
  h. Copy the URL it generates and paste it as the only line in the file: ResponsesURL.txt

Update any code and files as needed
1. git add <file> (for each <file>)
2. git commit
3. git push
4. git tag -f <year> && git push -f --tags (for current <year>)

Copy year folder to oscars site and rename to current year.

Edit categories.csv as a spreadsheet
1. Update the categories as a few change each year
2. Make sure the categories are in the same order as the Google Form
3. Edit the titles so the tie-breakers in parenthesis are correct
4. Add all the nominees under each category in the order you want them (usually alphabetical)
5. Replace any commas (,) with back-quotes (`)
6. Special characters such as á and é are allowed
7. Save in CSV (DOS) format as categories.csv

Delete this file if it exists:
File: categoryMaps.xml

From the new year folder run oscars.bat or ./oscars.sh

Update the link in ../index.html to point to current year.


-- While ballots are being collected

From the new year folder run the batch:
  ballots.bat (or ./ballots.sh) <url>
Leave it running so it can continuosly download the ballots as they're entered

Check the ballots and fix any names where they didn't capitalize properly direclty in the Google Sheet that the Google Form is using to collect the data.  Either restart the batch or just wait until a new ballot is entered.

--- After contest deadline for entering guesses

From the new year folder run the batch:
  emails.bat (or ./emails.sh) <url>
Send the list of names and emails to Scott Takeda.

From the new year folder run the batch:
  oscars.bat (or ./oscars.sh) <url>
You will be prompted on how to map survey answers to nominees for each category where it can't be done automatically.
This will be saved so you only have to do this for new survey answers.
If any mistakes are made you must edit (or delete) categoryMaps.xml and start oscars.bat or oscars.sh over again.

On the Responses tab of the Google Form turn off the Accepting Responses option.
