<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="utf-8" indent="yes" />
  <xsl:variable name="definitions"
    select="document('../data/definitions.xml')/definitions" />
  <xsl:variable name="rootDir" select="concat('/', $definitions/@year, '/')" />
  <xsl:variable name="resultsFile" select="concat($rootDir, 'data/results.xml')" />
  <xsl:variable name="results" select="document($resultsFile)/results" />
  <xsl:variable name="mappings"
    select="document(concat($rootDir, 'data/mappings.xml'))/mappings" />
  <xsl:variable name="ballots"
    select="document(concat($rootDir, 'data/ballots.xml'))/ballots" />
  <xsl:variable name="ended" select="boolean($results/awards/@END)" />
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
  <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:template name="header">
    <xsl:param name="storeSortOrder" />
    <xsl:comment>OSCARS website created by Scott McDonald</xsl:comment>
    <head>
      <link rel="stylesheet" type="text/css" href="/oscars.css" />
      <meta http-equiv="cache-control"
        content="no-cache, no-store, must-revalidate" />
      <meta http-equiv="expires" content="0" />
      <meta http-equiv="pragma" content="no-cache" />
      <title><xsl:value-of select="$definitions/@year" /> OSCARS</title>
      <style>
        body {
          background-color: PaleGoldenrod;
        }
        .sortable u {
          text-decoration: underline;
        }
        .sortable u::before,
        .sortable u::after {
          text-decoration: none;
          display: inline-block;
        }
        .sortable[data-sort="asc"] u::before,
        .sortable[data-sort="asc"] u::after {
          content: "↑";
        }
        .sortable[data-sort="desc"] u::before,
        .sortable[data-sort="desc"] u::after {
          content: "↓";
        }
        .decision {
          display: none;
        }
        .decision.visible {
          display: inline;
        }
      </style>
      <script>
        // Sends the HTTP request, avoiding cached responses, and performs the action
        function http(url, onLoad, method = "GET") {
          const http = new XMLHttpRequest();
          http.onload = () => onLoad(http);
          http.open(method, `${url}?_=${Date.now()}`);
          http.send();
        }

        // Reads the start time relative to the system clock and performs the action
        function readStart(action) {
          http("<xsl:value-of select="$rootDir"/>data/elapsed.txt",
            http => action(Date.now() - Number(http.responseText)));
        }

        <xsl:if test="not($ended)">
          // Reads the last-modified timestamp of the results file and performs the action
          function readModified(action) {
            http("<xsl:value-of select="$resultsFile"/>",
              http => action(http.getResponseHeader("Last-Modified")), "HEAD");
          }

          // Get the last modified timestamp
          readModified(function(updated) {
            // Get the start time (actual or scheduled) of the broadcast
            readStart(function(start) {
              <xsl:if test="not($results/awards/@START)">
                const units = [
                  { label: "Second", size: 60 },
                  { label: "Minute", size: 60 },
                  { label: "Hour",   size: 24 },
                  { label: "Day",    size: Infinity }
                ];
                const countdownElement = document.getElementById("countdown");
                const countdownRow = document.getElementById("countdown_row");
                const startTimezoneOffset =
                    new Date('<xsl:value-of select="$definitions/@curtain" />')
                        .getTimezoneOffset();

                // Repeatedly update the countdown timer
                function update() {
                  let remaining = Math.floor((start - Date.now()) / 1000);
                  countdownElement.style.display = remaining > 0 ? 'inline' : 'none';
                  countdownRow.innerHTML = units.map(unit => {
                    const cell = remaining &lt;= 0 ? '' : `
                      &lt;td style="width:100px; text-align:center">
                        &lt;B style="font-size:60px">${remaining % unit.size}&lt;/B>
                        &lt;br />
                        ${unit.label}${remaining % unit.size === 1 ? "" : "s"}
                      &lt;/td>
                    `;
                    remaining = Math.floor(remaining / unit.size);
                    return cell;
                  }).reverse().join("");
                  document.getElementById("dst").style.display = new Date().getTimezoneOffset() ===
                      startTimezoneOffset ? 'none' : 'inline';
                }
                update();
                setInterval(update, 500);
              </xsl:if>

              // Repeatedly check for updates and reload the page when results are updated
              const interval = 3;
              let skips = 0;
              setInterval(function() {
                if ((++skips >= 60 / interval || start - Date.now() &lt; 10 * 60 * 1000)
                    &amp;&amp; document.visibilityState === "visible")
                  readModified(function(latest) {
                    if (latest !== updated) {
                      sessionStorage.setItem('scrollPosition', window.scrollY);
                      <xsl:value-of select="$storeSortOrder"/>
                      window.location.reload();
                    }
                    skips = 0;
                  });
              }, interval * 1000);
            });
          });

          // Restore the vertical scroll position when new data causes the page to be reloaded
          window.addEventListener("load", () => {
            const scrollPosition = sessionStorage.getItem('scrollPosition');
            if (scrollPosition !== null)
              window.scrollTo(0, parseInt(scrollPosition, 10));
            sessionStorage.removeItem('scrollPosition');
          });
        </xsl:if>
      </script>
    </head>
    <header>
      <center>
        <a href="{$rootDir}"
          style="display: table; text-decoration: none; color: inherit">
          <table id="header"
            style="border: 2px solid black; color: PaleGoldenrod; background-image: url('http://oscars.site44.com/RedCurtain.jpg'); background-repeat: no-repeat; background-size: 100%">
            <tr>
              <td rowspan="3" />
              <td rowspan="3">
                <img src="http://oscars.site44.com/trophy.png" id="trophy" />
              </td>
              <td>
                <br />
              </td>
              <td rowspan="3">
                <img src="http://oscars.site44.com/trophy.png" id="trophy" />
              </td>
              <td rowspan="3" />
            </tr>
            <tr>
              <th style="background-color: transparent">
                <xsl:value-of select="$definitions/@year" /> OSCARS</th>
            </tr>
            <tr>
              <td>
                <i>(Unofficial Results)</i>
                <br />
                <br />
              </td>
            </tr>
            <tr>
              <td colspan="5">
                <xsl:apply-templates
                  select="$definitions/column[@name = 'Best Picture']/nominee"
                  mode="poster">
                  <xsl:with-param name="category" select="'Best Picture'" />
                  <xsl:with-param name="width" select="'50'" />
                </xsl:apply-templates>
              </td>
            </tr>
          </table>
        </a>
        <br />
        <hr
          width="500" />
        <a href="{$rootDir}players">PLAYERS</a>
        &#160;&#160;-&#160;&#160; <a href="{$rootDir}categories">CATEGORIES</a>
        &#160;&#160;-&#160;&#160; <a href="/history">HISTORY</a>
        <hr width="500" />
        <br />
        <xsl:if
          test="not($results/awards/@START)">
          <A id="countdown" style="display: none">
            <B style="font-size: 30px">Oscars Countdown</B>
            <br />
            <i id="dst">
              Includes daylight saving time change
              <br />
            </i>
            <br />
            <table style="table-layout: fixed; background-color: white">
              <tr id="countdown_row" />
            </table>
            <br />
          </A>
        </xsl:if>
      </center>
    </header>
  </xsl:template>
  <xsl:template name="footer">
    <footer>
      <center>
        <br />
        <font color="gray">
          <i>Contest by Scott Takeda | Website by Scott McDonald</i>
        </font>
      </center>
    </footer>
  </xsl:template>
  <xsl:template match="/definitions/column" mode="value">
    <xsl:if test="@tieBreaker">
      <xsl:value-of
        select="substring('1.00000000000000000000', 1, @tieBreaker + 1)" />
    </xsl:if>
    <xsl:value-of select="'1'" />
  </xsl:template>
  <xsl:template match="/definitions/column" mode="tieBreaker">
    <xsl:variable name="tieSymbols" select="'➀➁➂➃➄➅➆➇➈➉'" />
    <xsl:choose>
      <xsl:when test="string-length(substring($tieSymbols, @tieBreaker, 1))">
        <xsl:value-of select="substring($tieSymbols, @tieBreaker, 1)" />
      </xsl:when>
      <xsl:when test="@tieBreaker">
        <xsl:value-of select="concat('(', @tieBreaker, ')')" />
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="/results/awards/category" mode="attribute">
    <xsl:param name="nominee" />
    <xsl:attribute name="class">
      <xsl:choose>
        <xsl:when test="nominee/@name = $nominee">
          correct
        </xsl:when>
        <xsl:when test="nominee">
          incorrect
        </xsl:when>
        <xsl:otherwise>
          unannounced
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>
  <xsl:template match="nominee" mode="poster">
    <xsl:param name="category" />
    <xsl:param name="width" />
    <xsl:variable name="name" select="@name" />
    <img alt="{$name}" width="{$width}"
      src="{$definitions/column[@name = $category]/nominee[@name = $name]/@img}">
      <xsl:attribute name="title">
        <xsl:call-template name="getOrDefault">
          <xsl:with-param name="value"
            select="$mappings/column[@name = $category]/nominee[@name = $name][last()]/@ballot" />
          <xsl:with-param name="default" select="$name" />
        </xsl:call-template>
      </xsl:attribute>
    </img>
  </xsl:template>
  <xsl:template match="player" mode="playerName">
    <xsl:value-of
      select="concat(@lastName, substring(', ', 1, 2 * number(string-length(@firstName) and string-length(@lastName))), @firstName)" />
  </xsl:template>
  <xsl:template match="/results/standings/player" mode="playerURL">
    <xsl:value-of select="concat($rootDir, 'players/', @firstName, '_', @lastName, '.xml')" />
  </xsl:template>
  <xsl:template name="getOrDefault">
    <xsl:param name="value" />
    <xsl:param name="default" />
    <xsl:choose>
      <xsl:when test="$value">
        <xsl:value-of select="$value" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$default" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>