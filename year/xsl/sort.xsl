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
            <xsl:variable name="timeHeader">
              <xsl:choose>
                <xsl:when test="$results/awards/@END">
                  <xsl:call-template name="time">
                    <xsl:with-param name="time">
                      <xsl:value-of select="$results/standings/@time" />
                    </xsl:with-param>
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="'Time'" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="$timeHeader" />
              <xsl:with-param name="type" select="'time'" />
              <xsl:with-param name="sort" select="@name" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
        </tr>
      </thead>
      <tbody id="rankings">
        <xsl:choose>
          <xsl:when test="starts-with(@name, 'time')">
            <xsl:for-each select="$ballots/player">
              <xsl:sort select="@time" data-type="number" order="{@order}" />
              <xsl:sort select="translate(@lastName, $lowercase, $uppercase)" />
              <xsl:sort select="translate(@firstName, $lowercase, $uppercase)" />
              <xsl:variable name="player" select="." />
              <xsl:apply-templates
                select="$results/standings/player[@firstName = $player/@firstName and @lastName = $player/@lastName]" />
            </xsl:for-each>
          </xsl:when>
          <xsl:when test="starts-with(@name, 'name')">
            <xsl:apply-templates select="$results/standings/player">
              <xsl:sort select="translate(@lastName, $lowercase, $uppercase)"
                order="{@order}" />
              <xsl:sort select="translate(@firstName, $lowercase, $uppercase)"
                order="{@order}" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="sort" select="." />
            <xsl:apply-templates select="$results/standings/player">
              <xsl:sort select="@*[name() = $sort/@column1]" data-type="number"
                order="{@order}" />
              <xsl:sort select="@*[name() = $sort/@column2]" data-type="number"
                order="{@order}" />
              <xsl:sort select="@*[name() = $sort/@column3]" data-type="number"
                order="{@order}" />
              <xsl:sort select="translate(@lastName, $lowercase, $uppercase)" />
              <xsl:sort select="translate(@firstName, $lowercase, $uppercase)" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </tbody>
    </table>
    <xsl:if test="$results/awards/@START and not($results/awards/@END)">
      <script>
        function timeToString(time) {
          return Math.trunc(time / 60 / 60) + ":" + 
            String(Math.trunc(time / 60) % 60).padStart(2, '0') + ":" + 
            String(time % 60).padStart(2, '0');
        }

        class Player {
          constructor(id, firstName, lastName, score, rank, bpr, wpr, time, decided) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.score = score;
            this.rank = rank;
            this.bpr = bpr;
            this.wpr = wpr;
            this.time = time;
            this.decided = decided;
          }
          
          getScore() {
            return parseFloat(this.score);
          }

          compareTo(other) {
            if (this.lastName.toLowerCase() !== other.lastName.toLowerCase())
              return this.lastName.localeCompare(other.lastName, undefined, { sensitivity: 'base' });
            return this.firstName.localeCompare(other.firstName, undefined, { sensitivity: 'base' });
          }
        }

        const players = [];
        <xsl:for-each select="$results/standings/player">
          <xsl:variable name="player" select="." />
          players.push(new Player(
            <xsl:value-of select="$ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/@id" />,
            '<xsl:value-of select="@firstName"/>',
            '<xsl:value-of select="@lastName"/>',
            '<xsl:value-of select="@score"/>',
            <xsl:value-of select="@rank"/>,
            <xsl:value-of select="@bpr"/>,
            <xsl:value-of select="@wpr"/>,
            <xsl:apply-templates select="." mode="timeValue" />,
            '<xsl:value-of select="@decided"/>'
          ));
        </xsl:for-each>
        
        const cells = document.getElementById("rankings").getElementsByTagName("td");
        const tableWidth = cells.length / players.length;

        let next = 0;
        const time = parseInt('<xsl:value-of select="$results/standings/@time" />');
        const start = new Date().getTime();
        const repeat = setInterval(function() { 
          const elapsed = Math.floor((new Date().getTime() - start) / 1000) + time;
          document.getElementById("time_header").innerHTML = timeToString(elapsed);
          document.getElementById("timeHeader_cell").style.backgroundColor = "white";

          <xsl:if test="$inPlayer">
            const inPlayer = players[<xsl:value-of select="$ballots/player[@firstName = $inPlayer/@firstName and @lastName = $inPlayer/@lastName]/@id" /> - 1];

            document.getElementById("time_value").innerHTML = document.getElementById("time_header").innerHTML;
            document.getElementById("time_difference").innerHTML = 
              (elapsed &lt; inPlayer.time ? '-' : '') + timeToString(Math.abs(elapsed - inPlayer.time));
          </xsl:if>

          // Process when next player's time is reached
          if (elapsed >= next) {
            if (next > 0)
              document.getElementById("timeHeader_cell").style.backgroundColor = "limegreen";
            next = Number.MAX_SAFE_INTEGER;
            for (let player = 0; player &lt; players.length; player++)
              if (players[player].time &gt; elapsed &amp;&amp; players[player].time &lt; next)
                next = players[player].time;

            // Recalculate rank, bpr and wpr
            for (let player = 0; player &lt; players.length; player++) {
              players[player].rank = 1;
              players[player].bpr = 1;
              players[player].wpr = 1;
              
              for (let opponent = 0; opponent &lt; players.length; opponent++) {
                if (players[opponent].getScore() &gt; players[player].getScore() || 
                    players[opponent].getScore() === players[player].getScore() &amp;&amp;
                    players[opponent].time &lt;= elapsed &amp;&amp; (
                      players[player].time &gt; elapsed || players[opponent].time &gt; players[player].time))
                  players[player].rank++;

                var decision = players[player].decided.substr(opponent, 1);
                if (decision === 'T' &amp;&amp; players[player].time !== players[opponent].time &amp;&amp;
                    players[player].time &lt;= elapsed &amp;&amp; players[opponent].time &lt;= elapsed) {
                  decision = players[player].time &gt; players[opponent].time ? 'W' : 'L';
                  players[player].decided = players[player].decided.substring(0, opponent) + decision + 
                    players[player].decided.substring(opponent + 1);
                }

                if (decision === 'L')
                  players[player].bpr++;
                if (decision === 'L' || decision === '?' || decision === 'T' &amp;&amp; 
                    (players[opponent].time &gt; players[player].time || players[player].time &gt; elapsed))
                  players[player].wpr++;
              }
            }
            
            // Sort the players
            const sortedPlayers = [...players];
            sortedPlayers.sort(function(a, b) {
              return '<xsl:value-of select="@name" />'.startsWith('name') ?
                '<xsl:value-of select="@order" />' === 'descending' ? b.compareTo(a) : a.compareTo(b) :
                (((a.<xsl:value-of select="@column1" /> - b.<xsl:value-of select="@column1" />) * players.length
                  + a.<xsl:value-of select="@column2" /> - b.<xsl:value-of select="@column2" />) * players.length
                  + a.<xsl:value-of select="@column3" /> - b.<xsl:value-of select="@column3" />)
                  * ('<xsl:value-of select="@order" />' === 'descending' ? -2 : 2) + a.compareTo(b);
            });

            // Update the player table
            for (let player = 0; player &lt; players.length; player++) {
              <xsl:if test="$inPlayer">
                const decision = inPlayer.decided.substr(sortedPlayers[player].id - 1, 1);
                cells[(player * tableWidth) + 0].style.backgroundColor = 
                  decision === "W" ? "limegreen" : decision === "L" ? "red" : decision === "-" ? "white" : "silver";
              </xsl:if>
              cells[(player * tableWidth) + 0].innerHTML = 
                '&lt;a href="<xsl:value-of select="$rootDir" />player/' + 
                sortedPlayers[player].firstName + '_' + sortedPlayers[player].lastName + '.xml"' +
                '&gt;' + [sortedPlayers[player].lastName, sortedPlayers[player].firstName].join(', ') +
                '&lt;/a&gt;';
              cells[(player * tableWidth) + 1].innerHTML = sortedPlayers[player].rank;
              cells[(player * tableWidth) + 2].innerHTML = sortedPlayers[player].bpr;
              cells[(player * tableWidth) + 3].innerHTML = sortedPlayers[player].wpr;
              cells[(player * tableWidth) + 4].innerHTML = sortedPlayers[player].score;
              cells[(player * tableWidth) + 5].innerHTML = timeToString(sortedPlayers[player].time);
              cells[(player * tableWidth) + 5].style.backgroundColor = 
                sortedPlayers[player].time &gt; elapsed ? 'silver' : 'limegreen';
            }

            <xsl:if test="$inPlayer">
              document.getElementById('player_rank').innerHTML = inPlayer.rank;
              document.getElementById('possible_rank').innerHTML = 'Possible Final Rank: ' +
                inPlayer.bpr + (inPlayer.wpr === inPlayer.bpr ? '' : ' to ' + inPlayer.wpr);

              if (elapsed &gt;= inPlayer.time)
                for (let id of ["time_guess", "time_actual", "time_score"])
                  document.getElementById(id).style.backgroundColor = 'limegreen';

              // Show "will finish above" messages
              if (inPlayer.decided.includes("L"))
                document.getElementById("player_lost").style.display = 'inline';
              if (inPlayer.decided.includes("W"))
                document.getElementById("player_won").style.display = 'inline';
            </xsl:if>
          }
        }, 1000);
      </script>
    </xsl:if>
  </xsl:template>
  <xsl:template name="player-table-column-header">
    <xsl:param name="text" />
    <xsl:param name="type" />
    <xsl:param name="sort" />
    <xsl:param name="inPlayer" />
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
  <xsl:template match="/results/standings/player">
    <xsl:param name="inPlayer" />
    <tr>
      <td>
        <xsl:attribute name="class">header <xsl:if test="$inPlayer">
            <xsl:variable name="player" select="." />
            <xsl:variable name="decided"
              select="substring($inPlayer/@decided, number($ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/@id), 1)" />
            <xsl:choose>
              <xsl:when test="$decided = 'W'">correct</xsl:when>
              <xsl:when test="$decided = 'L'">incorrect</xsl:when>
              <xsl:when test="$decided = '?' or $decided = 'T'">unannounced</xsl:when>
            </xsl:choose>
          </xsl:if>
        </xsl:attribute>
        <xsl:apply-templates select="." mode="playerLink" />
      </td>
      <td class="rank">
        <xsl:value-of select="@rank" />
      </td>
      <xsl:if test="$inProgress">
        <td>
          <xsl:attribute name="class">rank <xsl:if test="@bpr = @wpr">
            unannounced
            </xsl:if>
          </xsl:attribute>
          <xsl:value-of select="@bpr" />
        </td>
        <td>
          <xsl:attribute name="class">rank <xsl:if test="@bpr = @wpr">
            unannounced
            </xsl:if>
          </xsl:attribute>
          <xsl:value-of select="@wpr" />
        </td>
      </xsl:if>
      <td class="rank">
        <xsl:value-of select="@score" />
      </td>
      <td>
        <xsl:apply-templates select="." mode="attribute" />
        <xsl:apply-templates select="." mode="time" />
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="winners">
    <xsl:param name="start" />
    <xsl:if test="$start &lt; count($results/awards/category)">
      <xsl:variable name="end" select="$start + 6" />
      <tr class="unannounced">
        <xsl:for-each
          select="$results/awards/category[position() &gt; $start and position() &lt;= $end]">
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
  <xsl:template match="/results/standings/player" mode="attribute">
    <xsl:attribute name="class">
      <xsl:variable name="player" select="." />
      <xsl:choose>
        <xsl:when
          test="$ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/@time &lt;= $results/standings/@time">
      correct
        </xsl:when>
        <xsl:when test="$results/awards/@END">
          incorrect
        </xsl:when>
        <xsl:otherwise>
          unannounced
        </xsl:otherwise>
      </xsl:choose>
      time</xsl:attribute>
  </xsl:template>
  <xsl:template match="/results/standings/player" mode="time">
    <xsl:call-template name="time">
      <xsl:with-param name="time">
        <xsl:apply-templates select="." mode="timeValue" />
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="/results/standings/player" mode="timeValue">
    <xsl:variable name="player" select="." />
    <xsl:value-of select="$ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/@time" />
  </xsl:template>
  <xsl:template name="time">
    <xsl:param name="time" />
    <xsl:value-of
      select="concat(format-number(floor($time div 60 div 60), '0'), ':', format-number(floor($time div 60) mod 60, '00'), ':', format-number($time mod 60, '00'))" />
  </xsl:template>
</xsl:stylesheet>