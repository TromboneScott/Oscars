<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="include.xsl" />
  <xsl:template match="/player">
    <html>
      <xsl:call-template name="header" />
      <body>
        <center>
          <xsl:choose>
            <xsl:when test="@type = 'all'">
              <xsl:choose>
                <xsl:when test="count($results/standings/player) = 0">
                  <a href="javascript:history.go(0)" style="all:unset">
                    <table>
                      <tr>
                        <td id="rank">
                          <br />
                          <b>BALLOTS ARE BEING COLLECTED</b>
                          <br />
                          <br />
                          <img
                            src="https://lh7-us.googleusercontent.com/9tsl1dpGEmRGVe77r7hNNZqUxK8zNCRGeHEzQOm6bxeJ-8mPknIrB0PtexESrvvf9Ty4jf1r1Sbuof9oyLmLmC9pfN-WXj3umHvHcQZ9BsX6a0VK8gykda1TgBvNFNsrzEzQFklGwS7yRp1Foyl8oR6fIyCvwA"
                            alt="&#9993; - &#9993; - &#9993; - &#9993; - &#9993;"
                            title="Counting Ballots"
                            width="200" />
                          <br />
                          <br />Ballot names will appear
                          here a few minutes after being cast.<br />The actual
                          guesses will be loaded after all ballots have been
                          collected.<br />
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
                          <th onclick="sortBallots('received')"
                            style="cursor:pointer">
                            <u>
                              Received
                            </u>
                          </th>
                          <th onclick="sortBallots('name')"
                            style="cursor:pointer">
                            <u>
                              Name
                            </u>
                          </th>
                        </tr>
                      </thead>
                      <tbody id="ballots">
                        <xsl:for-each select="$results/ballots/player">
                          <tr class="unannounced">
                            <td id="scott"
                              style="padding-left:10px; padding-right:10px" />
                            <td style="padding-left:10px; padding-right:10px" />
                          </tr>
                        </xsl:for-each>
                      </tbody>
                    </table>
                    <script>
                      const table = Array.from(document.getElementById("ballots").getElementsByTagName("tr"))
                          .map(row => row.getElementsByTagName("td"));

                      class Ballot {
                        constructor(received, firstName, lastName, nameText) {
                          this.received = received;
                          this.receivedText = received.substring(5, 10).replace('-', '/') + '/' +
                              received.substring(0, 4) + ' ' +
                              String((parseInt(received.substring(11, 13)) + 11) % 12 + 1).padStart(2, '0') +
                              (received + '00').substring(13, 19) +
                              (parseInt(received.substring(11, 13)) > 11 ? ' pm' : ' am');
                          this.firstName = firstName;
                          this.lastName = lastName;
                          this.nameText = nameText;
                        }
                      }
                            
                      // Load the ballots from XML files
                      const ballots = [];
                      <xsl:for-each select="$results/ballots/player">
                        <xsl:variable name="player" select="." />
                        ballots.push(new Ballot(
                          '<xsl:value-of select="@timestamp"/>',
                          '<xsl:value-of select="@firstName"/>',
                          '<xsl:value-of select="@lastName"/>',
                          '<xsl:apply-templates select="." mode="playerName" />'
                        ));
                      </xsl:for-each>
        

                      // Sorts the ballots based on the column clicked by the user
                      let sort;
                      let descending;
                      function sortBallots(column) {
                        descending = column === sort ? !descending : false;
                        sort = column;
        
                        // Sort the ballots
                        const columns = (sort === 'name' ? 'lastName,firstName' : 'received')
                            .split(',');
                        ballots.sort(function(a, b) {
                          return columns.concat(['lastName', 'firstName']).reduce((total, field, index) =>
                              total !== 0 ? total : (descending &amp;&amp; index &lt; columns.length ? -1 : 1) *
                                  (typeof a[field] === 'number' ? a[field] - b[field] :
                                      a[field].localeCompare(b[field], undefined, {sensitivity: 'base'})), 0);
                        });

                        // Update the ballots table
                        ballots.forEach((ballot, row) => {
                          table[row][0].innerHTML = ballot.receivedText;
                          table[row][1].innerHTML = ballot.nameText;
                        });
                      }
                      sortBallots('received');
                    </script>
                  </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:if test="count($results/awards/category/nominee) > 0">
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat($rootDir, 'category')" />
                      </xsl:attribute>
                      <h2>OSCAR WINNERS</h2>
                    </a>
                    <table style="table-layout:fixed; width:700px">
                      <xsl:call-template name="winners">
                        <xsl:with-param name="start" select="0" />
                      </xsl:call-template>
                    </table>
                    <br />
                    <br />
                    <br />
                  </xsl:if>
                  <h3 style="display:inline">Rankings</h3>
                  <br />
                  <br />
                  <div class="info">
                    <xsl:if test="not($ended)">
                      <u>BPR / WPR</u> - Best Possible Rank / Worst Possible
                    Rank: If guesses for all remaining <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat($rootDir, 'category')" />
                        </xsl:attribute>
                    categories</a> turn out to be correct / incorrect. <br />
                  <br />
                    </xsl:if>
                <u>
                    Score</u> - One point for each correct <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat($rootDir, 'category')" />
                      </xsl:attribute>
                    category</a> plus .1 for tie breaker #1, .01 for #2, .001
                    for #3, etc. </div>
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
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat($rootDir, 'category')" />
                        </xsl:attribute>
                        Category</a>
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
                        <a>
                          <xsl:attribute name="href">
                            <xsl:value-of
                              select="concat($rootDir, 'category/', @name, '.xml')" />
                          </xsl:attribute>
                          <xsl:value-of select="@name" />
                        </a>
                        <xsl:apply-templates select="$categoryDefinition"
                          mode="tieBreaker" />
                      </td>
                      <td>
                        <xsl:value-of select="$playerGuess" />
                      </td>
                      <td>
                        <xsl:variable name="winners">
                          <xsl:for-each select="nominee">
                            <xsl:value-of select="', '" />
                            <xsl:value-of select="@name" />
                          </xsl:for-each>
                        </xsl:variable>
                        <xsl:value-of select="substring-after($winners, ', ')" />
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
              <h3 style="display:inline">Rankings</h3>
              <a id="decided_L" style="display:none">
                <br /> All players in <font color="red">red</font> will finish
                above <xsl:value-of
                  select="$playerName" />
              </a>
              <a id="decided_W" style="display:none">
                <br /> All players in <font color="green">green</font> will
                finish below <xsl:value-of select="$playerName" />
              </a>
              <a id="decided_T" style="display:none">
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
          <th class="header" onclick="sortTable('name')" style="cursor:pointer">
            <u>
              Name
            </u>
          </th>
          <th onclick="sortTable('rank')" style="cursor:pointer">
            <u>
              Rank
            </u>
          </th>
          <xsl:if test="not($ended)">
            <th onclick="sortTable('bpr')" style="cursor:pointer">
              <u>
                BPR
              </u>
            </th>
            <th onclick="sortTable('wpr')" style="cursor:pointer">
              <u>
                WPR
              </u>
            </th>
          </xsl:if>
          <th onclick="sortTable('score')" style="cursor:pointer">
            <u>
              Score
            </u>
          </th>
          <th id="time_header" onclick="sortTable('time')"
            style="cursor:pointer">
            <u>
              Time
            </u>
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
            <td />
          </tr>
        </xsl:for-each>
      </tbody>
    </table>
    <script>
      const table = Array.from(document.getElementById("rankings").getElementsByTagName("tr"))
          .map(row => row.getElementsByTagName("td"));
      const baseSortColumns = '<xsl:value-of select="@columns" />'.split(',');
      const sortColumns = baseSortColumns.concat(['lastName', 'firstName']);
      const colors = new Map([["-", "white"], ["W", "limegreen"], ["L", "red"], ["T", "tan"],
          ["?", "silver"], ["X", "silver"], ["none", "transparent"]]);
      let elapsed = 0;

      // Sorts the table based on the column clicked by the user
      let sort = 'rank';
      let descending = false;
      function sortTable(column) {
        if (column !== undefined) {
          descending = column === sort ? !descending : false;
          sort = column;
        }
        
        // Sort the players
        const columns = (sort === 'name' ? 'lastName,firstName' :
            sort === 'bpr' ? 'bpr,rank,wpr' : sort === 'wpr' ? 'wpr,rank,bpr' :
            sort === 'time' ? 'time' : 'rank,bpr,wpr').split(',');
        players.sort(function(a, b) {
          return columns.concat(['lastName', 'firstName']).reduce((total, field, index) =>
              total !== 0 ? total : (descending &amp;&amp; index &lt; columns.length ? -1 : 1) *
                  (typeof a[field] === 'number' ? a[field] - b[field] :
                      a[field].localeCompare(b[field], undefined, {sensitivity: 'base'})), 0);
        });

        // Update the rankings table
        players.forEach((player, row) =>
            ["link", "rank",
                <xsl:if test="not($ended)">
                  "bpr", "wpr",
                </xsl:if>
                "scoreText", "timeText"
            ].forEach((field, column) => {
              table[row][column].innerHTML = player[field];
              table[row][column].style.backgroundColor = colors.get(
                  field === "link" ?
                      <xsl:if test="$inPlayer">
                        true ? inPlayer.decided[player.id] :
                      </xsl:if>
                      "-" :
                  field === "timeText" ?
                      <xsl:if test="$ended">
                        player.time > elapsed ? "L" :
                      </xsl:if>
                      player.time > elapsed ? "?" : "W" :
                  (field === "bpr" || field === "wpr") &amp;&amp; player.bpr === player.wpr ?
                      "?" : "none"
              );
            })
        );
      }

      // Formats the time value (in seconds) as: H:MM:SS
      function formatTime(time) {
        return [time / 60 / 60, time / 60 % 60, time % 60].map((value, index) =>
            String(Math.trunc(value)).padStart(index > 0 ? 2 : 1, '0')).join(':');
      }

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
        <xsl:variable name="player" select="." />
        players.push(new Player(
          '<xsl:value-of select="@firstName"/>',
          '<xsl:value-of select="@lastName"/>',
          '&lt;a href="<xsl:apply-templates select="." mode="playerLink" />">' +
              '<xsl:apply-templates select="." mode="playerName" />&lt;/a>',
          '<xsl:value-of select="@score"/>',
          <xsl:value-of select="$ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/@time" />,
          '<xsl:value-of select="@decided"/>'
        ));
      </xsl:for-each>

      <xsl:if test="$inPlayer">
        // Find the instance for this player
        const inPlayer = players.find(player => player.firstName === '<xsl:value-of select="$inPlayer/@firstName" />'
            &amp;&amp; player.lastName === '<xsl:value-of select="$inPlayer/@lastName" />');
        document.getElementById("time_player").innerHTML = inPlayer.timeText;
      </xsl:if>

      // Calculate and popluate values for player grid
      readElapsed(function() {
        const start = new Date().getTime() / 1000 - Math.max(parseInt(this.responseText), 0);
        let next = 0;
        function update() {
          elapsed = Math.trunc(new Date().getTime() / 1000 - start);

          <xsl:if test="$results/awards/@START">
            // Update the running time
            document.getElementById("time_header").innerHTML =
                '&lt;u>' + formatTime(elapsed) + '&lt;/u>';
            document.getElementById("time_header").style.backgroundColor =
                colors.get(elapsed >= next &amp;&amp; next > 0 ? "W" : "-");
            <xsl:if test="$inPlayer">
              document.getElementById("time_value").innerHTML = formatTime(elapsed);
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
              for (const opponent of players.filter(opponent => player.decided[opponent.id] === 'X' &amp;&amp;
                    elapsed >= player.time &amp;&amp; elapsed >= opponent.time &amp;&amp; player.time !== opponent.time))
                player.decided[opponent.id] =
                    player.time > opponent.time &amp;&amp; player.score >= opponent.score ? 'W' :
                    opponent.time > player.time &amp;&amp; opponent.score >= player.score ? 'L' : '?';
              const undecided = players.filter(opponent => player.decided[opponent.id] === 'X');
              const timeWillTell = undecided.filter(opponent => player.score >= opponent.score);

              player.rank = players.filter(opponent => opponent.score > player.score ||
                    opponent.score === player.score &amp;&amp; elapsed >= opponent.time  &amp;&amp;
                        (player.time > elapsed || opponent.time > player.time)).length + 1;
              player.bpr = player.decided.filter(decision => decision === 'L').length + 1;
              player.wpr = player.bpr + players.filter(opponent => player.decided[opponent.id] === '?').length +
                  undecided.filter(opponent => opponent.score > player.score).length +
                  Math.max(timeWillTell.filter(opponent => player.time > opponent.time).length,
                           timeWillTell.filter(opponent => opponent.time > player.time).length);
            }
            
            sortTable();

            // Update the player page
            <xsl:if test="$inPlayer">
              document.getElementById('player_rank').innerHTML = inPlayer.rank;
              <xsl:if test="not($ended)">
                document.getElementById('possible_rank').innerHTML =
                    inPlayer.wpr === inPlayer.bpr ? 'Rank is Final' :
                        'Possible Final Rank: ' + inPlayer.bpr + ' to ' + inPlayer.wpr;
              </xsl:if>

              const timeColor =  colors.get(
                  <xsl:if test="$ended">
                    inPlayer.time > elapsed ? "L" :
                  </xsl:if>
                  inPlayer.time > elapsed ? "?" : "W");
              for (let id of ["guess", "actual", "score"])
                document.getElementById("time_" + id).style.backgroundColor = timeColor;

              for (let decision of ['W', 'L', 'T'].filter(decision => inPlayer.decided.includes(decision)))
                document.getElementById("decided_" + decision).style.display = 'inline';
            </xsl:if>
          }
        }
        update();
        <xsl:if test="$results/awards/@START and not($ended)">
          setInterval(update, 1000);
        </xsl:if>
      });
    </script>
  </xsl:template>
  <xsl:template name="winners">
    <xsl:param name="start" />
    <xsl:if test="$start &lt; count($results/awards/category)">
      <xsl:variable name="end" select="$start + 6" />
      <tr class="unannounced">
        <xsl:for-each
          select="$results/awards/category[position() > $start and position() &lt;= $end]">
          <td style="text-align:center; vertical-align:top; white-space:normal">
            <a>
              <xsl:attribute name="id">
                <xsl:value-of select="@name" />
              </xsl:attribute>
              <xsl:attribute name="href">
                <xsl:value-of
                  select="concat($rootDir, 'category/', @name, '.xml')" />
              </xsl:attribute>
              <xsl:apply-templates select="nominee" mode="poster">
                <xsl:with-param name="category" select="@name" />
              </xsl:apply-templates>
              <xsl:if test="not(nominee)">
                <img
                  src="https://lh7-us.googleusercontent.com/rXNEERY7mvEvjOSojoZkUokJe6j0-ZN69W7G1J8tsF4JCwgBQzP9rf5EmfnscQGEF7t05eMqRE7dVyXt-trP-dolg7DqwKwGiK7h6iYoRfL3QIsnfemqcJTqBpgjUIK5hJumr7cBwIDB9xdkpF8tXiX_0qf_mg"
                  alt="?" title="Not Yet Announced" />
              </xsl:if>
              <br />
              <xsl:value-of select="@name" />
            </a>
          </td>
        </xsl:for-each>
      </tr>
      <xsl:call-template name="winners">
        <xsl:with-param name="start" select="$end" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>