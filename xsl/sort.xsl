<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/sort">
    <html>
      <xsl:variable name="results"
        select="document('../results.xml')/results" />
      <head>
        <link rel="stylesheet" type="text/css" href="../oscars.css" />
        <title>
          <xsl:value-of select="$results/title" />
        </title>
      </head>
      <body>
        <center>
          <table id="header">
            <tr>
              <td rowspan="2">
                <img src="../trophy.png" id="trophy" />
              </td>
              <th>
                <xsl:value-of select="$results/title" />
              </th>
              <td rowspan="2">
                <img src="../trophy.png" id="trophy" />
              </td>
            </tr>
            <tr>
              <td>(Unofficial Results)</td>
            </tr>
          </table>
          <br />
          <br />
          <div class="info">
            <u>Score</u>
            - One point for each correct
            <a href="../category/all.xml">category</a>
            plus .1 for tie breaker #1, .01 for #2, .001 for #3, etc.
            <br />
            <br />
            <u>BPR / WPR</u>
            - Best Possible Rank / Worst Possible Rank: If guesses for
            all remaining
            <a href="../category/all.xml">categories</a>
            turn out to be correct / incorrect.
          </div>
          <br />
          <br />
          <table>
            <thead>
              <tr>
                <th class="header">
                  <A href="name.xml">Name</A>
                </th>
                <th>
                  <A href="rank.xml">Rank</A>
                </th>
                <th>
                  <A href="bpr.xml">BPR</A>
                </th>
                <th>
                  <A href="wpr.xml">WPR</A>
                </th>
                <th>
                  <A href="rank.xml">Score</A>
                </th>
                <th>
                  <xsl:value-of select="$results/showTime/header" />
                </th>
              </tr>
            </thead>
            <tbody>
              <xsl:choose>
                <xsl:when test=". = 'name'">
                  <xsl:apply-templates select="$results/players/player">
                    <xsl:sort select="lastName" />
                    <xsl:sort select="firstName" />
                  </xsl:apply-templates>
                </xsl:when>
                <xsl:when test=". = 'rank'">
                  <xsl:apply-templates select="$results/players/player">
                    <xsl:sort select="rank" data-type="number" />
                    <xsl:sort select="score" data-type="number"
                      order="descending" />
                    <xsl:sort select="lastName" />
                    <xsl:sort select="firstName" />
                  </xsl:apply-templates>
                </xsl:when>
                <xsl:when test=". = 'bpr'">
                  <xsl:apply-templates select="$results/players/player">
                    <xsl:sort select="bpr" data-type="number" />
                    <xsl:sort select="score" data-type="number"
                      order="descending" />
                    <xsl:sort select="lastName" />
                    <xsl:sort select="firstName" />
                  </xsl:apply-templates>
                </xsl:when>
                <xsl:when test=". = 'wpr'">
                  <xsl:apply-templates select="$results/players/player">
                    <xsl:sort select="wpr" data-type="number" />
                    <xsl:sort select="score" data-type="number"
                      order="descending" />
                    <xsl:sort select="lastName" />
                    <xsl:sort select="firstName" />
                  </xsl:apply-templates>
                </xsl:when>
              </xsl:choose>
            </tbody>
          </table>
          <br />
          <a href="../category/all.xml" id="return">All Categories</a>
          <br />
          <br />
          <div id="date">
            Last updated:
            <xsl:value-of select="$results/updated" />
          </div>
        </center>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="player">
    <tr>
      <xsl:attribute name="class">
        <xsl:if test="@type = 'pseudo'">
          unannounced
        </xsl:if>
      </xsl:attribute>
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
          <xsl:choose>
            <xsl:when test="@type = 'pseudo'">
              header unannounced
            </xsl:when>
            <xsl:otherwise>
              header
            </xsl:otherwise>
          </xsl:choose>
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
        <xsl:if test="not(@type = 'pseudo')">
          <xsl:value-of select="rank" />
        </xsl:if>
      </td>
      <td class="rank">
        <xsl:if test="not(@type = 'pseudo')">
          <xsl:value-of select="bpr" />
        </xsl:if>
      </td>
      <td class="rank">
        <xsl:if test="not(@type = 'pseudo')">
          <xsl:value-of select="wpr" />
        </xsl:if>
      </td>
      <td class="rank">
        <xsl:value-of select="score" />
      </td>
      <td>
        <xsl:attribute name="class">
          <xsl:value-of select="time/@status" />
        </xsl:attribute>
        <xsl:value-of select="time" />
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
