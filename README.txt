To start a new year:

Create Google Form:
1. Open last year's form at forms.google.com
2. Select the dots menu at top right and click "Make a copy"
3. Make all necessary updates for the current year
  a. Change tie-breakers 3 & 4
    - Even years: 3 = Actress, 4 = Supporting Actor
    - Odd years: 3 = Actor, 4 = Supporting Actress
    i. Update rule #4 in Contest Info
    ii. Add/Delete descriptions of the acting categories
  b. Update the broadcast date
  c. Replace "Last Year's Winner"
    i. Use a picture from the Oscars awards ceremony
    ii. Use the nominee description from last year
  d. Update nominees
    i.  Copy from oscars.org and include special characters
    ii. Use the string " - " to separate movie from person (or actor from movie)
4. Set up data export
  a. Click Responses tab
  b. Click the green spreadsheet button to Create Spreadsheet
  c. Open responses spreadsheet
  d. Select File/Share/Publish to web
  e. Choose Link = Form Responses 1 (the tab name) and Embed = Comma-separated values (.csv)
  f. Check box for "Automatically republish when changes are made"
  g. Click "Start Publishing"
  h. Copy the URL it generates and paste it as the only line in the file: ResponsesURL.txt
  i. Click the 3 dots next to View in Sheets and select "Get email notifications for new responses" 

Download 100x148 pixel pictures for Google Form for all categories
1. Go to imdb.com
2. Use the search window to search for the movie or actor
3. Replace image on Google Form
  a. Click the X to remove existing image
  b. Click the "Add Image" icon that appears on the nominee
  c. Drag and drop the thumbnail image from the IMDB search

Edit year/data/definitions.xml using Eclipse
1. Update the year
2. Update the curtain date and time for the start of the broadcast in local time
3. Update the categories as a few change each year
4. Make sure the categories are in the same order as the Google Form
5. Update the tieBreaker values as indicated in Google Form steps above
6. These categories have to have these exact names: Timestamp, First Name, Last Name, Time, EMail
7. Add all the nominees under each category in the order you want them (usually alphabetical)
  a. Using Eclipse will put all special characters such as á and é in UTF-8 format
8. Set the img value for each nominee
  a. Right-click on image in Google Form and select: Copy image address
  b. Paste into img attribute

Start the year
1. Copy year folder to Oscars site and rename to current year
2. Update the year in global/index.html
3. Copy global/index.html to [DropBox]/Apps/site44/oscars.site44.com/index.html
4. Add current year to history/winners.xml
  a. Insert an element for the current year but no winners: <year year="20XX" />
  b. Copy to Oscars site

Proofread the category definitions:
1. Open the web page: oscars.site44.com/<year>/data
2. Verify the year in the banner title (20xx OSCARS)
3. Verify that the tie breakers are correct (including Time as the final tie breaker)
4. Verify that images for the Best Picture nominees show up in the banner at the top
5. Verify that all nominee images match their names
6. Verify that all nominee names are short enough to be displayed
7. Verify that any special characters in nominee names are displayed correctly

Commit changes for the year
1. Commit and push all changes
  a. To format an XML file do Ctrl+A then Ctrl+Shift+F
2. Replace <year> in this command: git tag -f <year> && git push -f --tags


-- While ballots are being collected

From the new year folder run the batch:
  oscars.bat ballot.Ballot (or ./oscars.sh -b)
Leave it running so it can continuously download the ballots as they're entered

Check the ballots as they come in and fix any names where they didn't capitalize properly
1. Fix names directly in the Google Sheet that the Google Form is using to collect the data
2. Either restart the batch or just wait until a new ballot is entered


-- After contest deadline for entering guesses

Kill (ctrl-c) the ballot process so nobody can see any late entries
Wait until the next morning to let any late entries trickle in  

From the new year folder run the batch:
  oscars.bat ballot.Ballot emails (or ./oscars.sh -e)
Send the list of names and emails to Scott Takeda.

From the new year folder run the batch:
  oscars.bat Oscars (or ./oscars.sh)
You will be prompted on how to map survey answers to nominees for each category where it can't be done automatically.
This will be saved so you only have to do this for new survey answers.

Proofread the category definitions:
1. Open the web page: oscars.site44.com/<year>/data
2. Verify that each category (in bold) is mapped to the correct question on the ballot
3. Verify that each nominee (in bold) is mapped to the correct response(s) from the ballot
4. Verify that any special characters in ballot responses are displayed correctly

Edit (or delete and start over) the file data/mappings.xml
- Fix any mistakes
- Fix any special characters that were copied/pasted (use NOTEPAD so characters will be in UTF-8)
- If any duplicates are found for the website mapping the last one will be used
- Start oscars.bat or oscars.sh over again if any changes were made

On the Responses tab of the Google Form turn off the Accepting Responses option.


-- After contest is over

Add the winner to history/winners.xml
1. Update the element for this year for correct and players and add the winner element
2. Copy the file to the Oscars site
3. Commit and push changes to Git 



====

HOW TO HANDLE TIES


Solution:

If there's a tie in a category, all players that picked one of the correct nominees get the
point for that category.


Justification:

We could award no points for that category since nobody guessed that there would be a tie.  But
we don't give players the option of guessing a tie.  Also, it's better to guess one of the
winners than to guess one of the losers so we can't just ignore categories with a tie. 

It could be said that guessing correctly in a category where there's a tie isn't as good as
guessing the single winner outright when there isn't a tie.  So we could give a fraction of a
point, like half a point, to each player that guessed one of the winners.  But this creates an
implicit tie-breaker.  Consider what would happen if there were only two categories.  If all
players get just one category correct they would all have one correct guess.  But the players that
guessed correctly in the category without a tie would win.  Since we explicitly enumerate the
tie-breakers for this contest and we don't include this situation, awarding a fraction of a point
would be problematic as well as confusing.

This means that giving a full point to each person that guessed one of the winners is the best
solution.  The value of this point is diluted due to the fact that more players will be receiving
the point.  So a player will do better against their competition if they guess correctly when
there isn't a tie.  But when there is a tie, it's still better to guess one of the winners.