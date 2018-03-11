<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="header.xsl" />
  <xsl:template match="/sort">
    <html>
      <xsl:variable name="results"
        select="document('../results.xml')/results" />
      <xsl:call-template name="init">
        <xsl:with-param name="results" select="$results" />
      </xsl:call-template>
      <body>
        <center>
          <xsl:call-template name="header">
            <xsl:with-param name="results" select="$results" />
          </xsl:call-template>
          <b>
            <a href="../../history">Oscars History</a>
          </b>
          <br />
          <br />
          <xsl:choose>
            <xsl:when test="$results/players/count=0">
              <br />
              <b>BALLOTS ARE BEING COUNTED</b>
              <br />
              <br />
              <br />
              Check here for live results during the Oscars broadcast.
            </xsl:when>
            <xsl:otherwise>
              <div class="info">
                <u>Score</u>
                - One point for each correct
                <a href="../category/all.xml">category</a>
                plus .1 for tie breaker #1, .01 for #2, .001 for #3,
                etc.
                <br />
                <br />
                <u>BPR / WPR</u>
                - Best Possible Rank / Worst Possible Rank: If guesses
                for
                all remaining
                <a href="../category/all.xml">categories</a>
                turn out to be correct / incorrect.
              </div>
              <br />
              <br />
              <xsl:call-template name="player-table">
                <xsl:with-param name="results" select="$results" />
                <xsl:with-param name="sort" select="." />
              </xsl:call-template>
              <br />
              <a href="../category/all.xml" id="return">All Categories</a>
              <br />
              <br />
              <div id="date">
                Last updated:
                <xsl:value-of select="$results/updated" />
              </div>
            </xsl:otherwise>
          </xsl:choose>
        </center>
      </body>
    </html>
  </xsl:template>
  <xsl:template name="player-table">
    <xsl:param name="results" />
    <xsl:param name="sort" />
    <xsl:param name="inPlayer" />
    <table>
      <thead>
        <tr>
          <th class="header">
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'Name'" />
              <xsl:with-param name="link" select="'name.xml'" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <th>
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'Rank'" />
              <xsl:with-param name="link" select="'rank.xml'" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <th>
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'BPR'" />
              <xsl:with-param name="link" select="'bpr.xml'" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <th>
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'WPR'" />
              <xsl:with-param name="link" select="'wpr.xml'" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <th>
            <xsl:call-template name="player-table-column-header">
              <xsl:with-param name="text" select="'Score'" />
              <xsl:with-param name="link" select="'rank.xml'" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <th>
            <xsl:value-of select="$results/showTime/header" />
          </th>
        </tr>
      </thead>
      <tbody>
        <xsl:choose>
          <xsl:when test="$sort = 'name'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'rank'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="rank" data-type="number" />
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'bpr'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="bpr" data-type="number" />
              <xsl:sort select="rank" data-type="number" />
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="$sort = 'wpr'">
            <xsl:apply-templates select="$results/players/player">
              <xsl:sort select="wpr" data-type="number" />
              <xsl:sort select="rank" data-type="number" />
              <xsl:sort select="lastName" />
              <xsl:sort select="firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:apply-templates>
          </xsl:when>
        </xsl:choose>
      </tbody>
    </table>
  </xsl:template>
  <xsl:template name="player-table-column-header">
    <xsl:param name="text" />
    <xsl:param name="link" />
    <xsl:param name="inPlayer" />
    <xsl:choose>
      <xsl:when test="$inPlayer">
        <xsl:value-of select="$text" />
      </xsl:when>
      <xsl:otherwise>
        <A>
          <xsl:attribute name="href">
            <xsl:value-of select="$link" />
          </xsl:attribute>
          <xsl:value-of select="$text" />
        </A>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="player">
    <xsl:param name="inPlayer" />
    <xsl:variable name="id" select="@id" />
    <tr>
      <xsl:variable name="playerName">
        <xsl:value-of select="firstName" />
        <xsl:if test="firstName != '' and lastName != ''">
          <xsl:value-of select="' '" />
        </xsl:if>
        <xsl:value-of select="lastName" />
      </xsl:variable>
      <xsl:variable name="playerFile"
        select="concat('../player/', $playerName, '.xml')" />
      <td>
        <xsl:attribute name="class">
          header
          <xsl:if test="$inPlayer">
            <xsl:choose>
              <xsl:when test="@id = $inPlayer/@id">
                unannounced
              </xsl:when>
              <xsl:when
          test="$inPlayer/opponents/player[number($id)] = 'BETTER' or $inPlayer/opponents/player[number($id)] = 'WORSE'">
                correct
              </xsl:when>
            </xsl:choose>
          </xsl:if>
        </xsl:attribute>
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="$playerFile" />
          </xsl:attribute>
          <xsl:value-of select="lastName" />
          <xsl:if test="firstName != '' and lastName != ''">
            <xsl:value-of select="', '" />
          </xsl:if>
          <xsl:value-of select="firstName" />
        </a>
      </td>
      <td class="rank">
        <xsl:value-of select="rank" />
      </td>
      <td class="rank">
        <xsl:value-of select="bpr" />
      </td>
      <td class="rank">
        <xsl:value-of select="wpr" />
      </td>
      <td class="rank">
        <xsl:value-of select="score" />
      </td>
      <td>
        <xsl:attribute name="class">
          <xsl:value-of select="time/@status" /> time
        </xsl:attribute>
        <xsl:value-of select="time" />
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>