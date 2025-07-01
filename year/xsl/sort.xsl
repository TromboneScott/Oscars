<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="include.xsl" />
  <xsl:template match="/sort">
    <html>
      <xsl:call-template name="header" />
      <body>
        <center>
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
                        title="Counting Ballots" />
                      <br />
                      <br />Ballot names will
                      appear here a few minutes after being cast.<br />The
                      actual guesses will be loaded after all ballots have been
                      collected.<br />
                      <br />
                      <i>Check here for live results
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
                  <tr>
                    <th>
                      <xsl:call-template name="player-table-column-header">
                        <xsl:with-param name="text" select="'Timestamp'" />
                        <xsl:with-param name="type" select="'default'" />
                        <xsl:with-param name="sort" select="@name" />
                      </xsl:call-template>
                    </th>
                    <th>
                      <xsl:call-template name="player-table-column-header">
                        <xsl:with-param name="text" select="'Name'" />
                        <xsl:with-param name="type" select="'name'" />
                        <xsl:with-param name="sort" select="@name" />
                      </xsl:call-template>
                    </th>
                  </tr>
                  <xsl:variable name="ballotSort" select="@ballotSort" />
                  <xsl:for-each select="$results/ballots/player">
                    <xsl:sort select="@*[name() = $ballotSort]" order="{@order}" />
                    <xsl:sort
                      select="translate(@lastName, $lowercase, $uppercase)"
                      order="{@order}" />
                    <xsl:sort
                      select="translate(@firstName, $lowercase, $uppercase)"
                      order="{@order}" />
                    <tr class="unannounced">
                      <td style="padding-left:10px; padding-right:10px">
                        <xsl:value-of
                          select="concat(
                            translate(substring(@timestamp, 6, 5), '-', '/'),
                            '/',
                            substring(@timestamp, 1, 4),
                            ' ',
                            format-number((substring(@timestamp, 12, 2) + 11) mod 12 + 1, '00'),
                            substring(concat(@timestamp, ':00'), 14, 6),                            
                            substring(' am pm', floor(substring(@timestamp, 12, 2) div 12) * 3 + 1, 3)
                            )" />
                      </td>
                      <td style="padding-left:10px; padding-right:10px">
                        <xsl:apply-templates select="." mode="playerName" />
                      </td>
                    </tr>
                  </xsl:for-each>
                </table>
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
                <xsl:if test="$inProgress">
                  <u>BPR / WPR</u> - Best Possible Rank / Worst Possible Rank:
                If guesses for all remaining <a>
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
                category</a> plus .1 for tie breaker #1, .01 for #2, .001 for
                #3, etc. </div>
              <br />
              <xsl:apply-templates select="." mode="player-table" />
            </xsl:otherwise>
          </xsl:choose>
        </center>
      </body>
      <xsl:call-template name="footer" />
    </html>
  </xsl:template>
  <xsl:template match="/sort" mode="player-table">
    <xsl:param name="inPlayer" />
    <table>
      <thead>
        <tr>
          <th class="header">
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'Name'" />
              <xsl:with-param name="type" select="'name'" />
              <xsl:with-param name="sort" select="@name" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <th>
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'Rank'" />
              <xsl:with-param name="type" select="'default'" />
              <xsl:with-param name="sort" select="@name" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <xsl:if test="$inProgress">
            <th>
              <xsl:call-template name="player-table-column-header">
                <xsl:with-param name="text" select="'BPR'" />
                <xsl:with-param name="type" select="'bpr'" />
                <xsl:with-param name="sort" select="@name" />
                <xsl:with-param name="inPlayer" select="$inPlayer" />
              </xsl:call-template>
            </th>
            <th>
              <xsl:call-template name="player-table-column-header">
                <xsl:with-param name="text" select="'WPR'" />
                <xsl:with-param name="type" select="'wpr'" />
                <xsl:with-param name="sort" select="@name" />
                <xsl:with-param name="inPlayer" select="$inPlayer" />
              </xsl:call-template>
            </th>
          </xsl:if>
          <th>
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'Score'" />
              <xsl:with-param name="type" select="'default'" />
              <xsl:with-param name="sort" select="@name" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <th id="timeHeader_cell">
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'Time'" />
              <xsl:with-param name="type" select="'time'" />
              <xsl:with-param name="sort" select="@name" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
        </tr>
      </thead>
      <tbody id="rankings">
        <xsl:for-each select="$results/standings/player">
          <tr>
            <td class="header" />
            <td class="rank" />
            <xsl:if test="$inProgress">
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
      // Format the time value as: H:MM:SS
      function timeToString(time) {
        return Math.trunc(time / 60 / 60) + ":" + 
            String(Math.trunc(time / 60) % 60).padStart(2, '0') + ":" + 
            String(time % 60).padStart(2, '0');
      }

      class Player {
        constructor(id, firstName, lastName, link, score, time, decided) {
          this.id = id;
          this.firstName = firstName;
          this.lastName = lastName;
          this.link = link;
          this.scoreText = score;
          this.score = parseFloat(score);
          this.time = time;
          this.decided = decided.split('');
        }

        compareTo(other) {
          return Math.sign(this.lastName.localeCompare(other.lastName, undefined, {sensitivity: 'base'}) * 2
              + this.firstName.localeCompare(other.firstName, undefined, {sensitivity: 'base'}));
        }
      }

      const players = [];
      <xsl:for-each select="$results/standings/player">
        <xsl:variable name="player" select="." />
        <xsl:variable name="ballot" select="$ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]" />
        players.push(new Player(
          <xsl:value-of select="$ballot/@id" />,
          '<xsl:value-of select="@firstName"/>',
          '<xsl:value-of select="@lastName"/>',
          '&lt;a href="<xsl:apply-templates select="." mode="playerLink" />">' +
              '<xsl:apply-templates select="." mode="playerName" />&lt;/a>',
          '<xsl:value-of select="@score"/>',
          <xsl:value-of select="$ballot/@time" />,
          '<xsl:value-of select="@decided"/>'
        ));
      </xsl:for-each>

      <xsl:if test="$inPlayer">
        const inPlayer = players.find(player => player.id ===
            <xsl:value-of select="$ballots/player[@firstName = $inPlayer/@firstName and @lastName = $inPlayer/@lastName]/@id" />);
        document.getElementById("time_player").innerHTML = timeToString(inPlayer.time);
      </xsl:if>

      const cells = document.getElementById("rankings").getElementsByTagName("td");
      const tableWidth = cells.length / players.length;

      let next = 0;
      const time = parseInt('<xsl:value-of select="$results/standings/@time" />');
      const start = new Date().getTime();
      function update() {
        const elapsed = Math.floor((new Date().getTime() - start) / 1000) + time;

        <xsl:if test="$results/awards[@START]">
          document.getElementById("time_header").innerHTML = timeToString(elapsed);
          document.getElementById("timeHeader_cell").style.backgroundColor =
              elapsed >= next &amp;&amp; next > 0 ? "limegreen" : "white";
          <xsl:if test="$inPlayer">
            document.getElementById("time_value").innerHTML = document.getElementById("time_header").innerHTML;
            document.getElementById("time_difference").innerHTML = 
                <xsl:if test="not($inProgress)">
                  inPlayer.time > elapsed ? 'OVER' :
                </xsl:if>
                (elapsed &lt; inPlayer.time ? '-' : '') + timeToString(Math.abs(elapsed - inPlayer.time));
          </xsl:if>
        </xsl:if>

        // Process when next player's time is reached
        if (elapsed >= next) {
          next = Math.min(...players.map(player => player.time).filter(time => time > elapsed));

          // Recalculate rank, bpr and wpr
          for (const player of players) {
            player.rank = players.filter(opponent => opponent.score > player.score ||
                  opponent.score === player.score &amp;&amp; elapsed >= opponent.time  &amp;&amp;
                      (player.time > elapsed || opponent.time > player.time)).length + 1;

            for (const opponent of players.filter(opponent => player.decided[opponent.id - 1] === 'X' &amp;&amp;
                  elapsed >= player.time &amp;&amp; elapsed >= opponent.time &amp;&amp; player.time !== opponent.time))
              player.decided[opponent.id - 1] = 
                  player.time > opponent.time &amp;&amp; player.score >= opponent.score ? 'W' :
                  opponent.time > player.time &amp;&amp; opponent.score >= player.score ? 'L' : '?';
            player.bpr = player.decided.filter(decision => decision === 'L').length + 1;

            const undecided = players.filter(opponent => player.decided[opponent.id - 1] === 'X');
            const timeWillTell = undecided.filter(opponent => player.score >= opponent.score);
            player.wpr = player.bpr + players.filter(opponent => player.decided[opponent.id - 1] === '?').length +
                undecided.filter(opponent => opponent.score > player.score).length +
                Math.max(timeWillTell.filter(opponent => player.time > opponent.time).length,
                         timeWillTell.filter(opponent => opponent.time > player.time).length);
          }

          // Sort the players
          players.sort(function(a, b) {
            return ('<xsl:value-of select="@name" />'.startsWith('name') ? a.compareTo(b) :
                  (Math.sign(a.<xsl:value-of select="@column1" /> - b.<xsl:value-of select="@column1" />) * 2 +
                  Math.sign(a.<xsl:value-of select="@column2" /> - b.<xsl:value-of select="@column2" />)) * 2 +
                  Math.sign(a.<xsl:value-of select="@column3" /> - b.<xsl:value-of select="@column3" />)
                ) * ('<xsl:value-of select="@order" />' === 'descending' ? -2 : 2) + a.compareTo(b);
          });

          // Update the rankings table
          players.forEach((player, row) => {
            <xsl:if test="$inPlayer">
              const decision = inPlayer.decided[player.id - 1];
              cells[row * tableWidth].style.backgroundColor = decision === "-" ? "white" :
                  decision === "W" ? "limegreen" : decision === "L" ? "red" :
                  decision === "T" ? "tan" : "silver";
            </xsl:if>
            const values = [player.link, player.rank,
                <xsl:if test="$inProgress">
                  player.bpr, player.wpr,
                </xsl:if>
                player.scoreText, timeToString(player.time)];
            values.forEach((value, column) => cells[row * tableWidth + column].innerHTML = value);
            <xsl:choose>
              <xsl:when test="$inProgress">
                for (let column = 2; column &lt; 4; column++)
                  cells[row * tableWidth + column].style.backgroundColor =
                      player.bpr === player.wpr ? 'silver': 'transparent';
                cells[row * tableWidth + 5].style.backgroundColor = 
                    player.time > elapsed ? 'silver' : 'limegreen';
              </xsl:when>
              <xsl:otherwise>
                cells[row * tableWidth + 3].style.backgroundColor = 
                    player.time > elapsed ? 'red' : 'limegreen';
              </xsl:otherwise>
            </xsl:choose>
          });

          // Update the player page
          <xsl:if test="$inPlayer">
            document.getElementById('player_rank').innerHTML = inPlayer.rank;
            document.getElementById('possible_rank').innerHTML = inPlayer.wpr === inPlayer.bpr ?
                'Rank is Final' : 'Possible Final Rank: ' + inPlayer.bpr + ' to ' + inPlayer.wpr;

            if (elapsed >= inPlayer.time)
              for (let id of ["guess", "actual", "score"])
                document.getElementById("time_" + id).style.backgroundColor = 'limegreen';

            for (let map of [{decision: 'W', id: 'won'}, {decision: 'L', id: 'lost'}])
              if (inPlayer.decided.includes(map.decision))
                document.getElementById("player_" + map.id).style.display = 'inline';
          </xsl:if>
        }
      }
      update();
      <xsl:if test="$results/awards/@START and $inProgress">
        setInterval(update, 1000);
      </xsl:if>
    </script>
  </xsl:template>
  <xsl:template name="player-table-column-header">
    <xsl:param name="text" />
    <xsl:param name="type" />
    <xsl:param name="sort" />
    <xsl:param
      name="inPlayer" />
    <xsl:choose>
      <xsl:when test="$inPlayer">
        <A>
          <xsl:attribute name="id">
            <xsl:value-of select="concat($type, '_header')" />
          </xsl:attribute>
          <xsl:value-of select="$text" />
        </A>
      </xsl:when>
      <xsl:otherwise>
        <A>
          <xsl:attribute name="id">
            <xsl:value-of select="concat($type, '_header')" />
          </xsl:attribute>
          <xsl:attribute name="href">
            <xsl:value-of select="$type" />
            <xsl:if test="$type = $sort">
              <xsl:value-of select="'Reverse'" />
            </xsl:if>
            <xsl:value-of select="'.xml'" />
          </xsl:attribute>
          <xsl:value-of select="$text" />
        </A>
      </xsl:otherwise>
    </xsl:choose>
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