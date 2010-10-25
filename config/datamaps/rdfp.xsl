<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:rdfp='http://www.w3.org/XML/2000/04/rdf-parse/#'
	exclude-result-prefixes='xsl rdf rdfp'>
	<!--
		an RDF parser in XSLTfor now, just the basic RDF syntax per2.2.1.
		Basic Serialization
		Syntaxofhttp://www.w3.org/TR/1999/REC-rdf-syntax-19990222 $Id:
		rdfp.xsl,v 1.8 2001/10/02 17:52:07 connolly Exp $
	-->

	<!-- @@add a switch for tinyprolog support? -->
	<xsl:output method='text' />

	<xsl:variable name='rdfNS'
		select='"http://www.w3.org/1999/02/22-rdf-syntax-ns#"' />

	<xsl:variable name='uriReferenceType'
		select='"http://www.w3.org/1999/XMLSchema#datatype_uriReference"' />

	<xsl:variable name='stringType'
		select='"http://www.w3.org/1999/XMLSchema#datatype_string"' />

	<!--
		@@argh! how do I refer to XML Schema datatypes? PLEASE can I have a
		canonical URI, in the appinfo, along with the has-facet stuff?
	-->
	<xsl:template match='rdf:RDF'>
		<!-- [1] RDF -->
		<!--
			the syntax for the results per
			http://www.ilrt.bris.ac.uk/discovery/rdf-dev/rudolf/js-rdf/
		-->

		<xsl:for-each select='text()[string-length(normalize-space())&gt;0]'>
			<xsl:call-template name='rdfp:badStuff'>
				<xsl:with-param name='expected' select='"description"' />
			</xsl:call-template>
		</xsl:for-each>

		<xsl:call-template name='rdfp:description_s' />
	</xsl:template>

	<xsl:template name='rdfp:description_s'>
		<xsl:param name='parentSubject' />

		<xsl:param name='parentPredicate' />

		<!-- do I need a parentID as well? @@-->

		<xsl:for-each select='*'>
			<!-- [2] description -->
			<xsl:variable name='node' select='.' />

			<xsl:choose>
				<!--
					REVIEW: about vs. rdf:about? reported Wed, 26 Apr 2000 05:12:05
					-0500
					http://lists.w3.org/Archives/Public/www-rdf-comments/2000AprJun/0019.html
				-->
				<xsl:when
					test='(@rdf:ID and @rdf:about) or (namespace-uri() = $rdfNS and @ID and @about)'>

					<xsl:call-template name='rdfp:badElement'>
						<xsl:with-param name='problem' select='"ID and about attribute"' />
					</xsl:call-template>
				</xsl:when>

				<xsl:when test='@rdf:ID'>
					<xsl:call-template name='rdfp:propertyElt_s'>
						<xsl:with-param name='node' select='$node' />

						<xsl:with-param name='subject' select='concat("#", @rdf:ID)' />

						<xsl:with-param name='parentSubject' select='$parentSubject' />

						<xsl:with-param name='parentPredicate' select='$parentPredicate' />
					</xsl:call-template>

					<xsl:call-template name='rdfp:propAttr_s'>
						<xsl:with-param name='subject' select='concat("#", @rdf:ID)' />
					</xsl:call-template>
				</xsl:when>

				<xsl:when test='(namespace-uri() = $rdfNS) and @ID'>
					<xsl:call-template name='rdfp:propertyElt_s'>
						<xsl:with-param name='node' select='$node' />

						<xsl:with-param name='subject' select='concat("#", @ID)' />

						<xsl:with-param name='parentSubject' select='$parentSubject' />

						<xsl:with-param name='parentPredicate' select='$parentPredicate' />
					</xsl:call-template>

					<xsl:call-template name='rdfp:propAttr_s'>
						<xsl:with-param name='subject' select='concat("#", @ID)' />
					</xsl:call-template>
				</xsl:when>

				<xsl:when test='@rdf:about'>
					<xsl:call-template name='rdfp:propertyElt_s'>
						<xsl:with-param name='node' select='$node' />

						<xsl:with-param name='subject' select='@rdf:about' />

						<xsl:with-param name='parentSubject' select='$parentSubject' />

						<xsl:with-param name='parentPredicate' select='$parentPredicate' />
					</xsl:call-template>

					<xsl:call-template name='rdfp:propAttr_s'>
						<xsl:with-param name='subject' select='@rdf:about' />
					</xsl:call-template>
				</xsl:when>

				<xsl:when test='(namespace-uri() = $rdfNS) and @about'>
					<xsl:call-template name='rdfp:propertyElt_s'>
						<xsl:with-param name='node' select='$node' />

						<xsl:with-param name='subject' select='@about' />

						<xsl:with-param name='parentSubject' select='$parentSubject' />

						<xsl:with-param name='parentPredicate' select='$parentPredicate' />
					</xsl:call-template>

					<xsl:call-template name='rdfp:propAttr_s'>
						<xsl:with-param name='subject' select='@about' />
					</xsl:call-template>
				</xsl:when>

				<xsl:otherwise>
					<!-- use _:foo ala n-triples -->
					<xsl:variable name='genid' select='concat("_:", generate-id())' />

					<!--
						@@hmm... what subject to use for an anonymous node?TimBL mentioned
						that RDF syntax for nodes denotes an existentialquantifier... it
						didn't make sense to me at first, but yesterday(21 apr 2000) I
						realized that anonymous nodes are like skolemfunctions, and skolem
						functions are used to represent existentialquantifiers in horn
						clauses (cf discussion with Boyer in Austin).... which reminds me:
						the skolem function needs to varyw.r.t. all the universally
						quantified variables at this point inthe expression. So... @@when
						we add variables/forall,don't forget to tweak this. We probably
						need a "free variables"parameter to most of the templates
						here.Hmm... why should only anonymous nodes get "skolemized"?I
						wonder if IDentified nodes also represent existentials.I suppose
						about='..' should be treated as a constant,not an existential.
					-->
					<xsl:call-template name='rdfp:propertyElt_s'>
						<xsl:with-param name='node' select='$node' />

						<xsl:with-param name='subject' select='$genid' />

						<xsl:with-param name='parentSubject' select='$parentSubject' />

						<xsl:with-param name='parentPredicate' select='$parentPredicate' />
					</xsl:call-template>

					<xsl:call-template name='rdfp:propAttr_s'>
						<xsl:with-param name='subject' select='$genid' />
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name='rdfp:propertyElt_s'>
		<xsl:param name='subject' />

		<!-- @@expand w.r.t. base? -->
		<xsl:param name='parentSubject' />

		<xsl:param name='parentPredicate' />

		<xsl:param name='node' />

		<xsl:if test='$parentPredicate'>
			<xsl:call-template name='rdfp:statement'>
				<xsl:with-param name='subject' select='$parentSubject' />

				<xsl:with-param name='predicate' select='$parentPredicate' />

				<xsl:with-param name='object' select='$subject' />

				<xsl:with-param name='objectType' select='$uriReferenceType' />
			</xsl:call-template>
		</xsl:if>

		<!-- [17] typedNode -->
		<xsl:if
			test='$node and (namespace-uri($node) != $rdfNS or local-name($node) != "Description")'>

			<xsl:call-template name='rdfp:statement'>
				<xsl:with-param name='subject' select='$subject' />

				<xsl:with-param name='predicate' select='concat($rdfNS, "type")' />

				<xsl:with-param name='object'
					select='concat(namespace-uri($node), local-name($node))' />

				<xsl:with-param name='objectType' select='$uriReferenceType' />
			</xsl:call-template>
		</xsl:if>

		<xsl:for-each select='*'>
			<!-- [6] propertyElt -->
			<xsl:variable name='predicate'
				select='concat(namespace-uri(), local-name())' />
			<xsl:variable name="stID" select="@rdf:ID" /> <!-- or just ID@@ -->

			<xsl:choose>
				<!-- [8] value -->
				<xsl:when test='text()[string-length(normalize-space())&gt;0] and *'>
					<xsl:call-template name='rdfp:badElement'>
						<xsl:with-param name='problem'
							select='"text and subelements mixed in property value"' />
					</xsl:call-template>
				</xsl:when>

				<xsl:when test='text()[string-length(normalize-space())&gt;0]'>
					<!-- @@ barf if any attrs except ID -->
					<xsl:call-template name='rdfp:statement'>
						<!-- @@parameterize the template to call for each statement? -->
						<xsl:with-param name="stID" select="$stID" />

						<xsl:with-param name='subject' select='$subject' />

						<xsl:with-param name='predicate' select='$predicate' />

						<xsl:with-param name='object'>
							<xsl:copy-of select='text()' />
						</xsl:with-param>

						<xsl:with-param name='objectType' select='$stringType' />
					</xsl:call-template>
				</xsl:when>

				<xsl:when test='*'>
					<!-- @@ barf if any attrs except ID -->
					<xsl:call-template name='rdfp:description_s'>
						<xsl:with-param name='parentSubject' select='$subject' />

						<xsl:with-param name='parentPredicate' select='$predicate' />
						<!-- pass $stID as $parentID? -->
					</xsl:call-template>
				</xsl:when>

				<!-- @@parseLiteral and parseResource -->
				<xsl:when test='@rdf:resource or (namespace-uri()=$rdfNS and @resource)'>

					<xsl:for-each select='text()[string-length(normalize-space())&gt;0]|*'>
						<xsl:call-template name='rdfp:badElement'>
							<xsl:with-param name='problem'
								select='"propertyElt with resource attribute should be empty"' />
						</xsl:call-template>
					</xsl:for-each>

					<xsl:variable name='resAttr'
						select='@*[local-name()="resource" and namespace-uri()=$rdfNS or namespace-uri(current()) = $rdfNS]' />

					<xsl:call-template name='rdfp:statement'>
						<xsl:with-param name="stID" select="$stID" />
						<xsl:with-param name='subject' select='$subject' />

						<xsl:with-param name='predicate' select='$predicate' />

						<xsl:with-param name='object' select='$resAttr' />

						<xsl:with-param name='objectType' select='$uriReferenceType' />
					</xsl:call-template>

					<!-- [16] propAttr -->
					<xsl:call-template name='rdfp:propAttr_s'>
						<xsl:with-param name='subject' select='$resAttr' />
					</xsl:call-template>
				</xsl:when>

				<xsl:otherwise>
					<!-- [16] propAttr -->
					<!--@@ idAttr, bagIdAttr -->
					<xsl:variable name='genid' select='concat("_:", generate-id())' />

					<xsl:call-template name='rdfp:propAttr_s'>
						<xsl:with-param name='subject' select='$genid' />
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>

		<xsl:for-each select='text()[string-length(normalize-space())&gt;0]'>
			<xsl:call-template name='rdfp:badStuff'>
				<xsl:with-param name='expected' select='"description"' />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name='rdfp:propAttr_s'>
		<xsl:param name='subject' />

		<!-- @@expand w.r.t. base? -->
		<xsl:for-each
			select='@*[not ((local-name() = "about" or local-name() = "resource" or local-name() = "ID" or local-name() = "bagID" or local-name() = "aboutEach" or local-name() = "aboutEachPrefix") and (namespace-uri() = $rdfNS or namespace-uri(current()) = $rdfNS)) ]'>

			<xsl:call-template name='rdfp:statement'>
				<xsl:with-param name='subject' select='$subject' />

				<xsl:with-param name='predicate'
					select='concat(namespace-uri(.), local-name(.))' />

				<xsl:with-param name='object'>
					<xsl:value-of select='.' />
				</xsl:with-param>

				<xsl:with-param name='objectType' select='$stringType' />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name='rdfp:statement'>
		<xsl:param name="stID" />

		<xsl:param name='subject' />

		<!-- @@expand w.r.t. base? -->
		<xsl:param name='predicate' />

		<xsl:param name='object' />

		<xsl:param name='objectType' />

		<xsl:if test="$stID"> <!-- reification, at least a little bit -->
			<xsl:call-template name="rdfp:statement">
				<xsl:with-param name="subject" select='concat("#", $stID)' />
				<xsl:with-param name="predicate" select='concat($rdfNS, "type")' />
				<xsl:with-param name="object" select='concat($rdfNS, "Statement")' />
				<xsl:with-param name="objectType" select="$uriReferenceType" />
			</xsl:call-template>

			<xsl:call-template name="rdfp:statement">
				<xsl:with-param name="subject" select='concat("#", $stID)' />
				<xsl:with-param name="predicate" select='concat($rdfNS, "subject")' />
				<xsl:with-param name="object" select='$subject' />
				<xsl:with-param name="objectType" select="$uriReferenceType" />
			</xsl:call-template>

			<xsl:call-template name="rdfp:statement">
				<xsl:with-param name="subject" select='concat("#", $stID)' />
				<xsl:with-param name="predicate" select='concat($rdfNS, "predicate")' />
				<xsl:with-param name="object" select='$predicate' />
				<xsl:with-param name="objectType" select="$uriReferenceType" />
			</xsl:call-template>

			<xsl:call-template name="rdfp:statement">
				<xsl:with-param name="subject" select='concat("#", $stID)' />
				<xsl:with-param name="predicate" select='concat($rdfNS, "object")' />
				<xsl:with-param name="object" select='$object' />
				<xsl:with-param name="objectType" select="$objectType" />
			</xsl:call-template>
		</xsl:if>

		<xsl:call-template name="assert-n-triples">
			<xsl:with-param name="subject" select="$subject" />
			<xsl:with-param name="predicate" select="$predicate" />
			<xsl:with-param name="object" select="$object" />
			<xsl:with-param name="objectType" select="$objectType" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="assert-tinyprolog">
		<xsl:param name='subject' />
		<xsl:param name='predicate' />
		<xsl:param name='object' />
		<xsl:param name='objectType' />

		triple(
		<xsl:value-of select='concat("{", $predicate, "}")' />
		,
		<xsl:value-of select='concat("{", $subject, "}")' />
		,
		<xsl:choose>
			<xsl:when test='$objectType = $uriReferenceType'>
				<xsl:value-of select='concat("{", $object, "}")' />
			</xsl:when>
			<xsl:when test='$objectType = $stringType'>
				<xsl:value-of select="concat('&quot;', $object, '&quot;')" />
			</xsl:when>
			<!-- @@parsetype literal will give us content here -->
			<xsl:otherwise>
				<xsl:message>
					unknown object type:
					<xsl:value-of select='$objectType' />
				</xsl:message>
			</xsl:otherwise>
		</xsl:choose>
		).
	</xsl:template>

	<xsl:template name='assert-minxml'>
		<xsl:param name='subject' />
		<xsl:param name='predicate' />
		<xsl:param name='object' />
		<xsl:param name='objectType' />

		<arc>
			<subject>
				<xsl:value-of select='$subject' />
			</subject>

			<predicate>
				<xsl:value-of select='$predicate' />
			</predicate>

			<xsl:choose>
				<xsl:when test='$objectType = $uriReferenceType'>
					<webobject>
						<xsl:value-of select='$object' />
					</webobject>
				</xsl:when>

				<xsl:when test='$objectType = $stringType'>
					<object>
						<xsl:value-of select='$object' />
					</object>
				</xsl:when>

				<!-- @@parsetype literal will give us content here -->
				<xsl:otherwise>
					<xsl:message>
						unknown object type:
						<xsl:value-of select='$objectType' />
					</xsl:message>
				</xsl:otherwise>
			</xsl:choose>
		</arc>
	</xsl:template>

	<xsl:template name='assert-n-triples'>
		<xsl:param name='subject' />
		<xsl:param name='predicate' />
		<xsl:param name='object' />
		<xsl:param name='objectType' />

		<xsl:choose>
			<xsl:when test='starts-with($subject, "_:")'> <!-- bNode kludge -->
				<xsl:value-of select='$subject' />
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>&lt;</xsl:text>
				<xsl:value-of select='$subject' />
				<xsl:text>&gt;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>

		<xsl:text> </xsl:text>

		<xsl:text>&lt;</xsl:text>
		<xsl:value-of select='$predicate' />
		<xsl:text>&gt;</xsl:text>
		<xsl:text> </xsl:text>

		<xsl:choose>
			<xsl:when
				test='$objectType = $uriReferenceType and starts-with($object, "_:")'> <!-- bNode kludge -->
				<xsl:value-of select='$object' />
			</xsl:when>

			<xsl:when test='$objectType = $uriReferenceType'>
				<xsl:text>&lt;</xsl:text>
				<xsl:value-of select='$object' />
				<xsl:text>&gt;</xsl:text>
			</xsl:when>

			<xsl:when test='$objectType = $stringType'>
				<xsl:text>"</xsl:text>
				<xsl:value-of select='$object' />
				<!-- xsl:message>
					@@string quoting
				</xsl:message -->
				<xsl:text>"</xsl:text>
			</xsl:when>

			<!-- @@parsetype literal will give us content here -->
			<xsl:otherwise>
				<xsl:message>
					unknown object type:
					<xsl:value-of select='$objectType' />
				</xsl:message>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>.
</xsl:text>
	</xsl:template>

	<xsl:template name='rdfp:badStuff'>
		<xsl:param name='expected' />

		<xsl:message>
			expected
			<xsl:value-of select='$expected' />

			but got: [[
			<xsl:copy-of select='.' />

			]]
		</xsl:message>
	</xsl:template>

	<xsl:template name='rdfp:badElement'>
		<xsl:param name='problem' />

		<xsl:message>problem
			in &lt;
			<xsl:value-of select='name(.)' />
			&gt;:
			<xsl:value-of select='$problem' />
		</xsl:message>
	</xsl:template>
</xsl:stylesheet>
