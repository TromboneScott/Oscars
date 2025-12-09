<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="include.xsl" />
  <xsl:template match="/player">
    <html>
      <xsl:call-template name="header">
        <xsl:with-param name="storeSortOrder">
          sessionStorage.setItem('sortColumn', table.sortColumn);
          sessionStorage.setItem('sortDescending', table.sortDescending);
        </xsl:with-param>
      </xsl:call-template>
      <body>
        <script>
          // Table that can sort and update the underlying HTML table
          class SortableTable {
            constructor(id, headers, defaultSort, data, sortFieldsFunction, colorFunction) {
              this.elements = Array.from(document.getElementById(id).getElementsByTagName("tr"))
                  .map(row => row.getElementsByTagName("td"));
              this.headers = headers;
              this.sortColumn = (sortColumn => headers.includes(sortColumn) ? sortColumn :
                  defaultSort)(sessionStorage.getItem('sortColumn'));
              this.sortDescending = sessionStorage.getItem('sortDescending') === 'true';
              this.data = data;
              this.sortFieldsFunction = sortFieldsFunction;
              this.colorFunction = colorFunction;

              sessionStorage.removeItem('sortColumn');
              sessionStorage.removeItem('sortDescending');
            }

            // Sorts the data in the table, adds arrows to the header and updates the table
            sort(column) {
              // Determine the sort column and sort order
              if (column !== undefined){
                this.sortDescending = this.sortColumn === column &amp;&amp; !this.sortDescending;
                this.sortColumn = column;
              }

              // Sort the data
              const sortFields = this.sortFieldsFunction(this.sortColumn);
              const allFields = sortFields.concat(['lastName', 'firstName']);
              this.data.sort((a, b) => {
                return allFields.reduce((total, field, index) => total !== 0 ? total :
                    (this.sortDescending &amp;&amp; index &lt; sortFields.length ? -1 : 1) *
                    (typeof a[field] === 'number' ? a[field] - b[field] :
                        a[field].localeCompare(b[field], undefined, {sensitivity: 'base'})), 0);
              });

              // Update the table
              this.headers.forEach((header, column) => {
                const arrow = this.sortColumn === header ? this.sortDescending ? '↓' : '↑' : '';
                const headerElem = document.getElementById(header + '_header');
                const origHeader = headerElem.innerHTML.trim();
                headerElem.innerHTML = arrow + origHeader.match(/&lt;u>.*&lt;\/u>/i)[0] + arrow;

                this.data.forEach((instance, row) => {
                    this.elements[row][column].innerHTML = instance[header];
                    this.elements[row][column].style.backgroundColor =
                        this.colorFunction(instance, header);
                });
              });
            }
          }
        </script>
        <center>
          <xsl:choose>
            <xsl:when test="@type = 'all'">
              <xsl:choose>
                <xsl:when test="count($results/standings/player) = 0">
                  <a href="javascript:history.go(0)" style="all: unset">
                    <table>
                      <tr>
                        <td id="rank">
                          <br />
                          <b>BALLOTS ARE BEING COLLECTED</b>
                          <br />
                          <br />
                          <img
                            src="http://oscars.site44.com/Ballots.jpg"
                            alt="&#9993; - &#9993; - &#9993; - &#9993; - &#9993;"
                            title="Counting Ballots" width="200" />
                          <br />
                          <br />Ballot
                          names will appear here a few minutes after being cast.<br />The
                          actual guesses will be loaded after all ballots have
                          been collected.<br />
                          <br />
                          <i>Check
                            here for live results
                            during the Oscars broadcast.</i>
                          <br />
                          <br />
                        </td>
                      </tr>
                    </table>
                  </a>
                  <xsl:if test="count($results/ballots/player) > 0">
                    <br />
                    <br />
                    <h3> Ballots Received: <xsl:value-of
                        select="count($results/ballots/player)" />
                    </h3>
                    <table>
                      <thead>
                        <tr>
                          <th id="received_header"
                            onclick="table.sort('received')"
                            style="cursor:pointer">
                            <u>Received</u>
                          </th>
                          <th id="name_header" onclick="table.sort('name')"
                            style="cursor: pointer">
                            <u>Name</u>
                          </th>
                        </tr>
                      </thead>
                      <tbody id="ballots">
                        <xsl:for-each select="$results/ballots/player">
                          <tr>
                            <td style="padding-left: 10px; padding-right: 10px" />
                            <td style="padding-left: 10px; padding-right: 10px" />
                          </tr>
                        </xsl:for-each>
                      </tbody>
                    </table>
                    <script>
                      class Ballot {
                        constructor(timestamp, firstName, lastName, name) {
                          this.timestamp = timestamp;
                          this.received = new Intl.DateTimeFormat("en-US", {
                            month: "2-digit",
                            day: "2-digit",
                            year: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                            second: "2-digit",
                            hour12: true
                          }).format(new Date(timestamp)).replace(",", "");
                          this.firstName = firstName;
                          this.lastName = lastName;
                          this.name = name;
                        }
                      }

                      // Load the ballots from XML files
                      const ballots = [];
                      <xsl:for-each select="$results/ballots/player">
                        ballots.push(new Ballot(
                            '<xsl:value-of select="@timestamp"/>',
                            '<xsl:value-of select="@firstName"/>',
                            '<xsl:value-of select="@lastName"/>',
                            '<xsl:apply-templates select="." mode="playerName" />'
                        ));
                      </xsl:for-each>

                      const table = new SortableTable(
                          "ballots",
                          ["received", "name"],
                          "received",
                          ballots,
                          sort => sort === 'name' ? ['lastName', 'firstName'] : [sort],
                          (ballot, field) => 'silver'
                      );

                      table.sort();
                    </script>
                  </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:if test="count($results/awards/category/nominee) > 0">
                    <a href="{$rootDir}categories">
                      <h2>OSCAR WINNERS</h2>
                    </a>
                    <table style="table-layout: fixed; width: 600px">
                      <xsl:variable name="rowSize" select="6" />
                      <xsl:for-each
                        select="$results/awards/category[position() mod $rowSize = 1]">
                        <tr class="unannounced">
                          <xsl:for-each
                            select=".|following-sibling::category[position() &lt; $rowSize]">
                            <td
                              style="text-align: center; vertical-align: top; white-space: normal">
                              <a id="{@name}"
                                href="{$rootDir}categories/{@name}.xml">
                                <xsl:apply-templates select="nominee"
                                  mode="poster">
                                  <xsl:with-param name="category" select="@name" />
                                  <xsl:with-param name="width" select="'50'" />
                                </xsl:apply-templates>
                                <xsl:if test="not(nominee)">
                                  <img
                                    src="http://oscars.site44.com/trophy_poster.png"
                                    alt="?"
                                    title="Not Yet Announced" width="50" />
                                </xsl:if>
                                <br />
                                <xsl:call-template name="getOrDefault">
                                  <xsl:with-param name="value"
                                    select="$definitions/column[@name = current()/@name]/@wrappingName" />
                                  <xsl:with-param name="default" select="@name" />
                                </xsl:call-template>
                              </a>
                            </td>
                          </xsl:for-each>
                        </tr>
                      </xsl:for-each>
                    </table>
                    <br />
                    <br />
                    <br />
                  </xsl:if>
                  <h3 style="display: inline">Rankings</h3>
                  <br />
                  <br />
                  <div class="info">
                    <xsl:if test="not($ended)">
                      <u>BPR / WPR</u> - Best Possible Rank / Worst Possible
                    Rank: If guesses for all remaining <a
                        href="{$rootDir}categories">
                        categories
                      </a>
                    turn out to be correct / incorrect. <br />
                      <br />
                    </xsl:if>
                    <u>
                    Score</u> - One point for each correct <a
                      href="{$rootDir}categories">category</a> plus .1 for tie
                    breaker #1, .01 for #2, .001 for #3, etc. </div>
                  <br />
                  <xsl:call-template name="player-table" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <xsl:variable name="player" select="." />
              <xsl:variable
                name="playerName" select="concat(@firstName, ' ', @lastName)" />
              <div
                id="name">
                <xsl:value-of select="$playerName" />
              </div>
              <br />
              <table>
                <tr>
                  <td id="rank">Rank <div id="rank">
                      <a id="player_rank" />
                    </div> Out of <xsl:value-of
                      select="count($results/standings/player)" />
                  </td>
                </tr>
              </table>
              <br />
              <xsl:if test="not($ended)">
                <a id="possible_rank" />
                <br />
              </xsl:if>
              <br />
              <h3>Guesses</h3>
              <table>
                <thead>
                  <tr>
                    <th class="header">
                      <a href="{$rootDir}categories">
                        Category
                      </a>
                    </th>
                    <th>Guess</th>
                    <th>Actual</th>
                    <th>Score</th>
                  </tr>
                </thead>
                <tbody>
                  <xsl:for-each select="$results/awards/category">
                    <xsl:variable name="categoryName" select="@name" />
                    <xsl:variable name="categoryDefinition"
                      select="$definitions/column[@name = $categoryName]" />
                    <xsl:variable name="playerGuess"
                      select="$ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/category[@name = $categoryName]/@nominee" />
                    <tr>
                      <xsl:apply-templates select="." mode="attribute">
                        <xsl:with-param name="nominee" select="$playerGuess" />
                      </xsl:apply-templates>
                      <td class="header">
                        <a href="{$rootDir}categories/{@name}.xml">
                          <xsl:value-of select="@name" />
                        </a>
                        <xsl:apply-templates select="$categoryDefinition"
                          mode="tieBreaker" />
                      </td>
                      <td>
                        <xsl:value-of select="$playerGuess" />
                      </td>
                      <td>
                        <xsl:for-each select="nominee">
                          <xsl:if test="position() > 1">, </xsl:if>
                          <xsl:value-of select="@name" />
                        </xsl:for-each>
                      </td>
                      <td>
                        <xsl:if test="nominee">
                          <xsl:variable name="value">
                            <xsl:apply-templates select="$categoryDefinition"
                              mode="value" />
                          </xsl:variable>
                          <xsl:choose>
                            <xsl:when test="nominee[@name = $playerGuess]">
                              <xsl:value-of select="$value" />
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="translate($value, '1', '0')" />
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:if>
                      </td>
                    </tr>
                  </xsl:for-each>
                </tbody>
                <tfoot>
                  <tr>
                    <xsl:variable name="playerScore"
                      select="$results/standings/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/@score" />
                    <th class="header">Total</th>
                    <th>
                      <xsl:value-of select="floor($playerScore)" />
                    </th>
                    <th>
                      <xsl:value-of
                        select="count($results/awards/category[nominee])" />
                    </th>
                    <th>
                      <xsl:value-of select="$playerScore" />
                    </th>
                  </tr>
                  <tr class="time">
                    <td class="header">
                      <xsl:value-of select="'Show Running Time'" />
                      <xsl:apply-templates
                        select="$definitions/column[@name = 'Time']"
                        mode="tieBreaker" />
                    </td>
                    <td id="time_guess">
                      <center>
                        <A id="time_player" />
                      </center>
                    </td>
                    <td id="time_actual">
                      <center>
                        <A id="time_value" />
                      </center>
                    </td>
                    <td id="time_score">
                      <center>
                        <A id="time_difference" />
                      </center>
                    </td>
                  </tr>
                </tfoot>
              </table>
              <br />
              <br />
              <br />
              <h3 style="display: inline">Rankings</h3>
              <a id="decided_L" style="display: none">
                <br /> All players in <font color="red">red</font> will finish
                above <xsl:value-of
                  select="$playerName" />
              </a>
              <a id="decided_W" style="display: none">
                <br /> All players in <font color="green">green</font> will
                finish below <xsl:value-of select="$playerName" />
              </a>
              <a id="decided_T" style="display: none">
                <br /> All players in <font color="SaddleBrown">brown</font>
                will finish tied with <xsl:value-of select="$playerName" />
              </a>
              <br />
              <br />
              <xsl:call-template name="player-table">
                <xsl:with-param name="inPlayer" select="." />
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </center>
      </body>
      <xsl:call-template name="footer" />
    </html>
  </xsl:template>
  <xsl:template name="player-table">
    <xsl:param name="inPlayer" />
    <table>
      <thead>
        <tr>
          <th id="link_header" class="header" onclick="table.sort('link')"
            style="cursor: pointer">
            <u>Name</u>
          </th>
          <th id="rank_header" onclick="table.sort('rank')"
            style="cursor: pointer">
            <u>Rank</u>
          </th>
          <xsl:if test="not($ended)">
            <th id="bpr_header" onclick="table.sort('bpr')"
              style="cursor: pointer">
              <u>BPR</u>
            </th>
            <th id="wpr_header" onclick="table.sort('wpr')"
              style="cursor: pointer">
              <u>WPR</u>
            </th>
          </xsl:if>
          <th id="scoreText_header" onclick="table.sort('scoreText')"
            style="cursor: pointer">
            <u>Score</u>
          </th>
          <th id="timeText_header" onclick="table.sort('timeText')"
            style="cursor: pointer">
            <u>Time</u>
          </th>
        </tr>
      </thead>
      <tbody id="rankings">
        <xsl:for-each select="$results/standings/player">
          <tr>
            <td class="header" />
            <td class="rank" />
            <xsl:if test="not($ended)">
              <td class="rank" />
              <td class="rank" />
            </xsl:if>
            <td class="rank" />
            <td class="rank" />
          </tr>
        </xsl:for-each>
      </tbody>
    </table>
    <script>
      // Formats the time value (in seconds) as: H:MM:SS
      function formatTime(seconds) {
        return [seconds / 60 / 60, seconds / 60 % 60, seconds % 60].map((value, index) =>
            String(Math.trunc(value)).padStart(index > 0 ? 2 : 1, '0')).join(':');
      }

      const decidedColors = {
          "-": "white",
          "W": "limegreen",
          "L": "red",
          "T": "tan"
      };

      class Player {
        static #nextId = 0;

        constructor(firstName, lastName, link, score, time, decided) {
          this.id = Player.#nextId++;
          this.firstName = firstName;
          this.lastName = lastName;
          this.link = link;
          this.scoreText = score;
          this.score = parseFloat(score);
          this.time = time;
          this.timeText = formatTime(time);
          this.decided = decided.split('');
        }
      }

      // Load the players from XML files
      const players = [];
      <xsl:for-each select="$results/standings/player">
        <xsl:variable name="playerName">
          <xsl:apply-templates select="." mode="playerName"/>
        </xsl:variable>
        <xsl:variable name="playerURL">
          <xsl:apply-templates select="." mode="playerURL"/>
        </xsl:variable>
        players.push(new Player(
          '<xsl:value-of select="@firstName" />',
          '<xsl:value-of select="@lastName" />',
          '<xsl:value-of select="concat('&lt;a href=&quot;', $playerURL, '&quot;>', $playerName, '&lt;/a>')" />',
          '<xsl:value-of select="@score" />',
          <xsl:value-of select="$ballots/player[@firstName = current()/@firstName and @lastName = current()/@lastName]/@time" />,
          '<xsl:value-of select="@decided" />'
        ));
      </xsl:for-each>

      <xsl:if test="$inPlayer">
        // Find the instance for this player
        const inPlayer = players.find(player => player.firstName === '<xsl:value-of select="$inPlayer/@firstName" />'
            &amp;&amp; player.lastName === '<xsl:value-of select="$inPlayer/@lastName" />');
        document.getElementById("time_player").innerHTML = inPlayer.timeText;
      </xsl:if>

      let elapsed = -1;

      const table = new SortableTable(
          "rankings",
          ["link", "rank",
              <xsl:if test="not($ended)">
                "bpr", "wpr",
              </xsl:if>
              "scoreText", "timeText"
          ],
          "rank",
          players,
          sort =>
              sort === 'link' ? ['lastName', 'firstName'] :
              sort === 'bpr' ? ['bpr', 'rank', 'wpr'] :
              sort === 'wpr' ? ['wpr', 'rank', 'bpr'] :
              sort === 'timeText' ? ['time'] :
              ['rank', 'bpr', 'wpr'],
          (player, field) =>
              field === "link" ?
                    <xsl:if test="$inPlayer">
                      true ? decidedColors[inPlayer.decided[player.id]] || "silver" :
                    </xsl:if>
                    "white" :
                field === "timeText" ?
                    <xsl:if test="$ended">
                      player.time > elapsed ? "red" :
                    </xsl:if>
                    player.time > elapsed ? "silver" : "limegreen" :
                (field === "bpr" || field === "wpr") &amp;&amp; player.bpr === player.wpr ?
                   "silver" : "transparent"
      );

      // Calculate and popluate values for player grid
      readElapsed(function() {
        const start = new Date().getTime() / 1000 - Math.max(parseInt(this.responseText), 0);
        let next = 0;
        function update() {
          const tempElapsed = Math.trunc(new Date().getTime() / 1000 - start);
          if (tempElapsed > elapsed) {
            elapsed = tempElapsed;

            <xsl:if test="$results/awards/@START">
              // Update the running time
              const timeString = formatTime(elapsed);
              const timeTextHeader = document.getElementById("timeText_header");
              timeTextHeader.innerHTML =
                  timeTextHeader.innerHTML.replace(/&lt;u>.*&lt;\/u>/is, `<u>${timeString}</u>`);
              timeTextHeader.style.backgroundColor =
                  elapsed >= next &amp;&amp; next > 0 ? "limegreen" : "white";
              <xsl:if test="$inPlayer">
                document.getElementById("time_value").innerHTML = timeString;
                document.getElementById("time_difference").innerHTML =
                    <xsl:if test="$ended">
                      inPlayer.time > elapsed ? 'OVER' :
                    </xsl:if>
                    (elapsed &lt; inPlayer.time ? '-' : '') + formatTime(Math.abs(elapsed - inPlayer.time));
              </xsl:if>
            </xsl:if>

            // Process when next player's time is reached
            if (elapsed >= next) {
              next = Math.min(...players.map(player => player.time).filter(time => time > elapsed));

              // Recalculate rank, bpr and wpr
              for (const player of players) {
                for (const opponent of players.filter(opponent =>
                    player.decided[opponent.id] === 'X' &amp;&amp;
                    elapsed >= player.time &amp;&amp;
                    elapsed >= opponent.time &amp;&amp;
                    player.time !== opponent.time))
                  player.decided[opponent.id] =
                      player.time > opponent.time &amp;&amp; player.score >= opponent.score ? 'W' :
                      opponent.time > player.time &amp;&amp; opponent.score >= player.score ? 'L' :
                      '?';
                const undecided = players.filter(opponent => player.decided[opponent.id] === 'X');
                const timeWillTell = undecided.filter(opponent => player.score >= opponent.score);

                player.rank = players.filter(opponent => opponent.score > player.score ||
                    opponent.score === player.score &amp;&amp;
                    elapsed >= opponent.time  &amp;&amp;
                    (player.time > elapsed || opponent.time > player.time)).length + 1;
                player.bpr = player.decided.filter(decision => decision === 'L').length + 1;
                player.wpr = player.bpr +
                    players.filter(opponent => player.decided[opponent.id] === '?').length +
                    undecided.filter(opponent => opponent.score > player.score).length +
                    Math.max(timeWillTell.filter(opponent => player.time > opponent.time).length,
                             timeWillTell.filter(opponent => opponent.time > player.time).length);
              }

              // Sort and update the table
              table.sort();

              // Update the player page
              <xsl:if test="$inPlayer">
                document.getElementById('player_rank').innerHTML = inPlayer.rank;
                <xsl:if test="not($ended)">
                  document.getElementById('possible_rank').innerHTML =
                      inPlayer.wpr === inPlayer.bpr ? 'Rank is Final' :
                          'Possible Final Rank: ' + inPlayer.bpr + ' to ' + inPlayer.wpr;
                </xsl:if>

                const timeColor =
                    <xsl:if test="$ended">
                      inPlayer.time > elapsed ? "red" :
                    </xsl:if>
                    inPlayer.time > elapsed ? "silver" : "limegreen";
                for (let id of ["guess", "actual", "score"])
                  document.getElementById("time_" + id).style.backgroundColor = timeColor;

                for (let decision of
                    ['W', 'L', 'T'].filter(decision => inPlayer.decided.includes(decision)))
                  document.getElementById("decided_" + decision).style.display = 'inline';
              </xsl:if>
            }
          }
        }
        update();
        <xsl:if test="$results/awards/@START and not($ended)">
          setInterval(update, 500);
        </xsl:if>
      });
    </script>
  </xsl:template>
</xsl:stylesheet>