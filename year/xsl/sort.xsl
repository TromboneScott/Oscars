<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="include.xsl" />
  <xsl:variable name="categoryCount"
    select="count($definitions/category/nominee[position() = 1])" />
  <xsl:variable name="pointWidth" select="16" />
  <xsl:template match="/sort">
    <html>
      <xsl:call-template name="init" />
      <body>
        <center>
          <xsl:call-template name="header" />
          <xsl:choose>
            <xsl:when test="count($results/standings/player)=0">
              <a href="javascript:history.go(0)" style="all: unset">
                <table>
                  <tr>
                    <td id="rank">
                      <br />
                      <b>BALLOTS ARE BEING COLLECTED</b>
                      <br />
                      <br />
                      &#9993; - &#9993; - &#9993; - &#9993; - &#9993; <br />
                      <br />
                      Ballot names will appear here a few minutes after being
                      cast. <br /> The "votes" will be loaded after all ballots
                      have been collected. <br />
                      <br />
                      <i>Check here for live
                      results during the Oscars broadcast.</i>
                      <br />
                      <br />
                    </td>
                  </tr>
                </table>
              </a>
              <xsl:if test="count($results/ballots/ballot) > 0">
                <br />
                <br />
                <h3> Ballots Received: <xsl:value-of
                    select="count($results/ballots/ballot)" />
                </h3>
                <table>
                  <tr>
                    <th>
                      <xsl:call-template name="player-table-column-header">
                        <xsl:with-param name="text" select="'Timestamp'" />
                        <xsl:with-param name="type" select="'rank'" />
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
                  <xsl:apply-templates select="$results/ballots/ballot">
                    <xsl:sort select="@*[name() = $ballotSort]" order="{@order}" />
                    <xsl:sort select="@name" order="{@order}" />
                  </xsl:apply-templates>
                </table>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <a href="../category/all.xml">
                <h2>OSCAR WINNERS</h2>
              </a>
              <table>
                <xsl:call-template name="winners">
                  <xsl:with-param name="start" select="0" />
                </xsl:call-template>
              </table>
              <br />
              <br />
              <div class="info">
                <xsl:if test="$inProgress">
                  <u>BPR / WPR</u> - Best Possible Rank / Worst Possible Rank:
                If guesses for all remaining <a href="../category/all.xml">
                categories</a> turn out to be correct / incorrect. <br />
                  <br />
                </xsl:if>
                <u>
                Score</u> - One point for each correct <a
                  href="../category/all.xml">category</a> plus .1 for tie
                breaker #1, .01 for #2, .001 for #3, etc. </div>
              <br />
              <br />
              <xsl:apply-templates select="." mode="player-table" />
              <br />
              <a href="../category/all.xml" id="return">All Categories</a>
            </xsl:otherwise>
          </xsl:choose>
          <br />
          <br />
          <a href="../../history">Oscars History</a>
          <xsl:if test="$results/@updated">
            <br />
            <br />
            <xsl:call-template name="updated" />
          </xsl:if>
        </center>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="/results/ballots/ballot">
    <tr class="unannounced">
      <td>
        <xsl:value-of
          select="concat(translate(substring(@timestamp, 6, 5), '-', '/'), '/', substring(@timestamp, 1, 4), ' ')" />
        <xsl:value-of
          select="concat(format-number((substring(@timestamp, 12, 2) + 11) mod 12 + 1, '00'), substring(@timestamp, 14, 6))" />
        <xsl:value-of
          select="concat(' ', substring('AP', floor(substring(@timestamp, 12, 2) div 12) + 1, 1), 'M')" />
      </td>
      <td>
        <xsl:value-of select="@name" />
      </td>
    </tr>
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
              <xsl:with-param name="type" select="'rank'" />
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
              <xsl:with-param name="type" select="'score'" />
              <xsl:with-param name="sort" select="@name" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:call-template>
          </th>
          <th>
            <xsl:variable name="timeHeader">
              <xsl:value-of select="'Time'" />
              <xsl:choose>
                <xsl:when test="$inProgress">
                  <xsl:value-of select="'&gt;'" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="'='" />
                </xsl:otherwise>
              </xsl:choose>
              <xsl:call-template name="time">
                <xsl:with-param name="time">
                  <xsl:value-of select="$results/awards/@duration" />
                </xsl:with-param>
              </xsl:call-template>
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
      <tbody>
        <xsl:choose>
          <xsl:when test="starts-with(@name, 'time')">
            <xsl:for-each select="$ballots/player">
              <xsl:sort select="@time" data-type="number" order="{@order}" />
              <xsl:sort select="@lastName" />
              <xsl:sort select="@firstName" />
              <xsl:variable name="player" select="." />
              <xsl:apply-templates
                select="$results/standings/player[@firstName = $player/@firstName and @lastName = $player/@lastName]" />
            </xsl:for-each>
          </xsl:when>
          <xsl:when test="starts-with(@name, 'name')">
            <xsl:apply-templates select="$results/standings/player">
              <xsl:sort select="@lastName" order="{@order}" />
              <xsl:sort select="@firstName" order="{@order}" />
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
              <xsl:sort select="@lastName" />
              <xsl:sort select="@firstName" />
              <xsl:with-param name="inPlayer" select="$inPlayer" />
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </tbody>
    </table>
  </xsl:template>
  <xsl:template name="player-table-column-header">
    <xsl:param name="text" />
    <xsl:param name="type" />
    <xsl:param name="sort" />
    <xsl:param name="inPlayer" />
    <xsl:choose>
      <xsl:when test="$inPlayer">
        <xsl:value-of select="$text" />
      </xsl:when>
      <xsl:otherwise>
        <A>
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
        <xsl:attribute name="class"> header <xsl:if test="$inPlayer">
            <xsl:variable name="player" select="." />
            <xsl:choose>
              <xsl:when
                test="@firstName = $inPlayer/@firstName and @lastName = $inPlayer/@lastName">
          unannounced
              </xsl:when>
              <xsl:when
                test="substring($inPlayer/@decided, number($ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/@id), 1) = 'Y'">
          correct
              </xsl:when>
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
          <xsl:attribute name="class"> rank <xsl:if test="@bpr = @wpr">
            unannounced
            </xsl:if>
          </xsl:attribute>
          <xsl:value-of select="@bpr" />
        </td>
        <td>
          <xsl:attribute name="class"> rank <xsl:if test="@bpr = @wpr">
            unannounced
            </xsl:if>
          </xsl:attribute>
          <xsl:value-of select="@wpr" />
        </td>
      </xsl:if>
      <td class="rank">
        <xsl:value-of select="@score" /> -- <xsl:variable
          name="tieBreakersWidth">
          <xsl:call-template name="tieBreakersWidth">
            <xsl:with-param name="score" select="@score" />
            <xsl:with-param name="width" select="$pointWidth div 2" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable
          name="scoreWidth"
          select="round(@score) * $pointWidth + $tieBreakersWidth" />
        <img
          src="../../bar_green.bmp" height="15">
          <xsl:attribute name="width">
            <xsl:value-of select="$scoreWidth" />
          </xsl:attribute>
        </img>
        <img
          src="../../bar_grey.bmp" height="15">
          <xsl:attribute name="width">
            <xsl:value-of
              select="$pointWidth * ($categoryCount + 1) - 1 - $scoreWidth" />
          </xsl:attribute>
        </img>
      </td>
      <td>
        <xsl:apply-templates select="." mode="attribute" />
        <xsl:apply-templates select="." mode="time" />
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="tieBreakersWidth">
    <xsl:param name="score" />
    <xsl:param name="width" />
    <xsl:choose>
      <xsl:when test="$width &lt; 1">
        0
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="tieBreakersWidth">
          <xsl:call-template name="tieBreakersWidth">
            <xsl:with-param name="score" select="$score * 10" />
            <xsl:with-param name="width" select="$width div 2" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of
          select="round($score * 10) mod 2 * $width + $tieBreakersWidth" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="winners">
    <xsl:param name="start" />
    <xsl:if test="$start &lt; count($results/awards/category)">
      <xsl:variable name="end" select="$start + 6" />
      <tr class="unannounced">
        <xsl:for-each
          select="$results/awards/category[position() &gt; $start and position() &lt;= $end]">
          <td style="text-align: center">
            <a>
              <xsl:attribute name="id">
                <xsl:value-of select="@name" />
              </xsl:attribute>
              <xsl:attribute name="href">
                <xsl:value-of select="concat('../category/', @name, '.xml')" />
              </xsl:attribute>
              <xsl:apply-templates select="nominee" mode="poster">
                <xsl:with-param name="category" select="@name" />
              </xsl:apply-templates>
              <xsl:if test="not(nominee)">
                <img src="http://oscars.site44.com/trophy_poster.png"
                  title="Not Yet Announced" />
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
          test="$ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/@time &lt;= $results/awards/@duration">
      correct
        </xsl:when>
        <xsl:when test="$inProgress">
          unannounced
        </xsl:when>
        <xsl:otherwise>
          incorrect
        </xsl:otherwise>
      </xsl:choose> time </xsl:attribute>
  </xsl:template>
  <xsl:template match="/results/standings/player" mode="time">
    <xsl:variable name="player" select="." />
    <xsl:call-template name="time">
      <xsl:with-param name="time">
        <xsl:value-of
          select="$ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/@time" />
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template name="time">
    <xsl:param name="time" />
    <xsl:value-of
      select="concat(format-number(floor($time div 60 div 60), '0'), ':', format-number(floor($time div 60) mod 60, '00'), ':', format-number($time mod 60, '00'))" />
  </xsl:template>
</xsl:stylesheet>