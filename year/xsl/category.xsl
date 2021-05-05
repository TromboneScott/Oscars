<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="header.xsl" />
  <xsl:template match="/category">
    <html>
      <xsl:variable name="categoryName" select="." />
      <xsl:variable name="categoryData" select="document('../category/all.xml')/categories/category[name = $categoryName]" />
      <xsl:variable name="results" select="document('../results.xml')/results" />
      <xsl:variable name="categoryResults" select="$results/categories/category[name = $categoryName]" />
      <xsl:call-template name="init">
        <xsl:with-param name="results" select="$results" />
      </xsl:call-template>
      <body>
        <center>
          <xsl:call-template name="header">
            <xsl:with-param name="results" select="$results" />
          </xsl:call-template>
          <div id="name">
            <xsl:value-of select="$categoryName" />
          </div>
          Tie Breaker:
          <xsl:value-of select="$categoryData/tieBreaker" />
          <xsl:if test="$categoryData/tieBreaker = ''">
            NO
          </xsl:if>
          <br />
          Point Value:
          <xsl:value-of select="$categoryData/value" />
          <br />
          <br />
          <img>
            <xsl:attribute name="src">
              <xsl:value-of select="$categoryResults/chart" />
            </xsl:attribute>
          </img>
          <br />
          <br />
          <br />
          <table>
            <xsl:variable name="winners">
              <xsl:for-each select="$categoryResults/winner">
                <xsl:value-of select="concat('|', ., '|')" />
              </xsl:for-each>
            </xsl:variable>
            <thead>
              <tr>
                <th class="header">Name</th>
                <xsl:for-each select="$categoryData/guesses/guess">
                  <th>
                    <xsl:attribute name="class">
                      <xsl:choose>
                        <xsl:when test="$winners = ''">
                          unannounced
                        </xsl:when>
                        <xsl:when test="contains($winners, concat('|', ., '|'))">
                          correct
                        </xsl:when>
                        <xsl:otherwise>
                          incorrect
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:attribute>
                    <xsl:value-of select="." />
                  </th>
                </xsl:for-each>
              </tr>
            </thead>
            <tbody>
              <xsl:variable name="guesses" select="$categoryData/guesses" />
              <xsl:for-each select="$categoryData/players/player">
                <xsl:sort select="lastName" />
                <xsl:sort select="firstName" />
                <xsl:variable name="playerName">
                  <xsl:value-of select="firstName" />
                  <xsl:if test="firstName != '' and lastName != ''">
                    <xsl:value-of select="' '" />
                  </xsl:if>
                  <xsl:value-of select="lastName" />
                </xsl:variable>
                <xsl:variable name="playerFile" select="concat('../player/', $playerName, '.xml')" />
                <xsl:variable name="playerDisplayName">
                  <xsl:value-of select="lastName" />
                  <xsl:if test="firstName != '' and lastName != ''">
                    <xsl:value-of select="', '" />
                  </xsl:if>
                  <xsl:value-of select="firstName" />
                </xsl:variable>
                <tr>
                  <xsl:attribute name="class">
                    <xsl:choose>
                      <xsl:when test="$winners = ''">
                        unannounced
                      </xsl:when>
                      <xsl:when test="contains($winners, concat('|', guess, '|'))">
                        correct
                      </xsl:when>
                      <xsl:otherwise>
                        incorrect
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:attribute>
                  <td class="header">
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="$playerFile" />
                      </xsl:attribute>
                      <xsl:value-of select="$playerDisplayName" />
                    </a>
                  </td>
                  <xsl:variable name="guess" select="guess" />
                  <xsl:for-each select="$guesses/guess">
                    <td id="selection">
                      <xsl:if test="$guess = .">
                        X
                      </xsl:if>
                    </td>
                  </xsl:for-each>
                </tr>
              </xsl:for-each>
            </tbody>
            <tfoot>
              <tr>
                <th class="header">Total</th>
                <xsl:for-each select="$categoryData/guesses/guess">
                  <th>
                    <xsl:attribute name="class">
                      <xsl:choose>
                        <xsl:when test="$winners = ''">
                          unannounced
                        </xsl:when>
                        <xsl:when test="contains($winners, concat('|', ., '|'))">
                          correct
                        </xsl:when>
                        <xsl:otherwise>
                          incorrect
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:attribute>
                    <xsl:variable name="name" select="." />
                    <xsl:value-of select="count($categoryData/players/player[guess=$name])" />
                  </th>
                </xsl:for-each>
              </tr>
            </tfoot>
          </table>
          <br />
          <a href="all.xml" id="return">All Categories</a>
          <br />
          <br />
          <a href="../index.html" id="return">Return to Main Page</a>
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
</xsl:stylesheet>
