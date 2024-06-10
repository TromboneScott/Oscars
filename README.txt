To start a new year:

Create Google Form:
1. Open last year's form at forms.google.com
2. Select the dots menu at top right and click "Make a copy"
3. Make all necessary updates for the current year
  a. Change tie-breakers 3 & 4
    i. Even years: 3 = Actress, 4 = Supporting Actor
    ii. Odd years: 3 = Actor, 4 = Supporting Actress
  b. Update the broadcast date
  c. Update nominees - Can be copied from oscars.org and include special characters
4. Set up data export
  a. Click Responses tab
  b. Click the green spreadsheet button to Create Spreadsheet
  c. Open responses spreadsheet
  d. Select File/Share/Publish to web
  e. Choose Link = Form Responses 1 (the tab name) and Embed = Comma-separated values (.csv)
  f. Check box for "Automatically rebulish when changes are made"
  g. Click "Publish"
  h. Copy the URL it generates and paste it as the only line in the file: ResponsesURL.txt

Download 100x148 pixel pictures for Google Form for all categories
1. Go to imdb.com
2. Use the search window to search for the movie or actor
3. Replace image on Google Form
  a. Click the X to remove existing image
  b. Click the "Add Image" icon that appears on the nominee
  c. Drag and drop the thumbnail image from the IMDB search

Update any code and files as needed
1. git add <file> (for each <file>)
2. git commit
3. git push
4. git tag -f <year> && git push -f --tags (for current <year>)

Copy year folder to oscars site and rename to current year.

Edit data/definitions.xml using NOTEPAD
1. Update the categories as a few change each year
2. Make sure the categories are in the same order as the Google Form
3. Update the tieBreaker values
4. These categories have to have these exact names: Timestamp, First, Last, Time, EMail
5. Add all the nominees under each category in the order you want them (usually alphabetical)
  a. Be sure to use NOTEPAD so all special characters such as á and é are in UTF-8 format
6. Set the img value for each nominee
  a. Right-click on image in Google Form and select: Copy image address
  b. Paste into img attribute

Proofread the category definitions:
1. Open the web page: oscars.site44.com/<year>/data/definitions.xml
2. Verify that the tie breakers are correct (including Time as the final tie breaker)
3. Verify that images for the Best Picture nominees show up in the banner at the top
4. Verify that all nominee images match their names
5. Verify that all nominee names are short enough to be displayed
6. Verify that any special characters in nominee names are displayed correctly


-- While ballots are being collected

From the new year folder run the batch:
  oscars.bat Ballot (or ./oscars.sh -b)
Leave it running so it can continuosly download the ballots as they're entered

Update the link in ../index.html to point to current year

Check the ballots as they come in and fix any names where they didn't capitalize properly
1. Fix names direclty in the Google Sheet that the Google Form is using to collect the data
2. Either restart the batch or just wait until a new ballot is entered

Proofread the category mapping:
1. Open the web page: oscars.site44.com/<year>/data/definitions.xml
2. Verify that the year now shows up in the banner title (20xx OSCARS)
2. Verify that each category (in bold) is mapped to the correct question on the ballot


--- After contest deadline for entering guesses

From the new year folder run the batch:
  oscars.bat Ballot emails (or ./emails.sh)
Send the list of names and emails to Scott Takeda.

From the new year folder run the batch:
  oscars.bat Oscars (or ./oscars.sh)
You will be prompted on how to map survey answers to nominees for each category where it can't be done automatically.
This will be saved so you only have to do this for new survey answers.

Proofread the nominee mapping:
1. Open the web page: oscars.site44.com/<year>/data/definitions.xml
2. Verify that each nominee (in bold) is mapped to the correct response(s) from the ballot
3. Verify that any special characters in ballot responses are displayed correctly

Edit (or delete and start over) the file data/responses.xml
- Fix any mistakes
- Fix any special characters that were copied/pasted (use NOTEPAD so characters will be in UTF-8)
- If any duplicates are found for the website mapping the last one will be used
- Start oscars.bat or oscars.sh over again if any changes were made

On the Responses tab of the Google Form turn off the Accepting Responses option.
