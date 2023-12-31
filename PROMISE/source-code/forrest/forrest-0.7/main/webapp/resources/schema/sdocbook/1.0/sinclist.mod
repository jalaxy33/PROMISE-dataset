<!-- ...................................................................... -->
<!-- Simplified DocBook Include Module V1.0 ............................... -->
<!-- File sinclist.mod .................................................... -->

<!-- Copyright 1992-2002 HaL Computer Systems, Inc.,
     O'Reilly & Associates, Inc., ArborText, Inc., Fujitsu Software
     Corporation, Norman Walsh, Sun Microsystems, Inc., and the
     Organization for the Advancement of Structured Information
     Standards (OASIS).

     Permission to use, copy, modify and distribute the DocBook XML DTD
     and its accompanying documentation for any purpose and without fee
     is hereby granted in perpetuity, provided that the above copyright
     notice and this paragraph appear in all copies.  The copyright
     holders make no representation about the suitability of the DTD for
     any purpose.  It is provided "as is" without expressed or implied
     warranty.

     If you modify the Simplified DocBook DTD in any way, except for
     declaring and referencing additional sets of general entities and
     declaring additional notations, label your DTD as a variant of
     DocBook.  See the maintenance documentation for more information.

     Please direct all questions, bug reports, or suggestions for
     changes to the docbook@lists.oasis-open.org mailing list. For more
     information, see http://www.oasis-open.org/docbook/.
-->

<!ENTITY % biblioset.module "IGNORE">
<!ENTITY % isbn.module "IGNORE">
<!ENTITY % issn.module "IGNORE">
<!ENTITY % pagenums.module "IGNORE">
<!ENTITY % simplelist.content.module "IGNORE">
<!ENTITY % anchor.module "IGNORE">
<!ENTITY % procedure.content.module "IGNORE">
<!ENTITY % publisher.module "IGNORE">
<!ENTITY % printhistory.module "IGNORE">
<!ENTITY % address.content.module "IGNORE">
<!ENTITY % glossterm.module "IGNORE">
<!ENTITY % bibliomixed.element "IGNORE">
<!ENTITY % bibliography.element "IGNORE">
<!ENTITY % bibliodiv.element "IGNORE">
<!ENTITY % biblioentry.module "IGNORE">
<!ENTITY % glossary.content.module "IGNORE">
<!ENTITY % glossentry.content.module "IGNORE">
<!ENTITY % ssscript.module "IGNORE">
<!ENTITY % caution.element "IGNORE">
<!ENTITY % caution.attlist "IGNORE">
<!ENTITY % important.element "IGNORE">
<!ENTITY % important.attlist "IGNORE">
<!ENTITY % tip.element "IGNORE">
<!ENTITY % tip.attlist "IGNORE">
<!ENTITY % warning.element "IGNORE">
<!ENTITY % warning.attlist "IGNORE">
<!ENTITY % remark.module "IGNORE">
<!ENTITY % otherinfo.module "IGNORE">
<!ENTITY % set.content.module "IGNORE">
<!ENTITY % set.module "IGNORE">
<!ENTITY % setinfo.module "IGNORE">
<!ENTITY % book.content.module "IGNORE">
<!ENTITY % book.module "IGNORE">
<!ENTITY % bookinfo.module "IGNORE">
<!ENTITY % dedication.module "IGNORE">
<!ENTITY % colophon.module "IGNORE">
<!ENTITY % toc.content.module "IGNORE">
<!ENTITY % toc.module "IGNORE">
<!ENTITY % tocfront.module "IGNORE">
<!ENTITY % tocentry.module "IGNORE">
<!ENTITY % tocpart.module "IGNORE">
<!ENTITY % tocchap.module "IGNORE">
<!ENTITY % toclevel1.module "IGNORE">
<!ENTITY % toclevel2.module "IGNORE">
<!ENTITY % toclevel3.module "IGNORE">
<!ENTITY % toclevel4.module "IGNORE">
<!ENTITY % toclevel5.module "IGNORE">
<!ENTITY % tocback.module "IGNORE">
<!ENTITY % lot.content.module "IGNORE">
<!ENTITY % lot.module "IGNORE">
<!ENTITY % lotentry.module "IGNORE">
<!ENTITY % chapter.module "IGNORE">
<!ENTITY % part.module "IGNORE">
<!ENTITY % preface.module "IGNORE">
<!ENTITY % reference.module "IGNORE">
<!ENTITY % partintro.module "IGNORE">
<!ENTITY % sect1.module "IGNORE">
<!ENTITY % sect2.module "IGNORE">
<!ENTITY % sect3.module "IGNORE">
<!ENTITY % sect4.module "IGNORE">
<!ENTITY % sect5.module "IGNORE">
<!ENTITY % simplesect.module "IGNORE">
<!ENTITY % index.content.module "IGNORE">
<!ENTITY % indexes.module "IGNORE">
<!ENTITY % indexdiv.module "IGNORE">
<!ENTITY % indexentry.module "IGNORE">
<!ENTITY % primsecterie.module "IGNORE">
<!ENTITY % seeie.module "IGNORE">
<!ENTITY % seealsoie.module "IGNORE">

<![ %exclude.refentry; [
<!ENTITY % refentry.content.module "IGNORE">
<!ENTITY % refentry.module "IGNORE">
<!ENTITY % refmeta.module "IGNORE">
<!ENTITY % refmiscinfo.module "IGNORE">
<!ENTITY % refnamediv.module "IGNORE">
<!ENTITY % refdescriptor.module "IGNORE">
<!ENTITY % refname.module "IGNORE">
<!ENTITY % refpurpose.module "IGNORE">
<!ENTITY % refclass.module "IGNORE">
<!ENTITY % refsynopsisdiv.module "IGNORE">
<!ENTITY % refsect1.module "IGNORE">
<!ENTITY % refsect2.module "IGNORE">
<!ENTITY % refsect3.module "IGNORE">
<!ENTITY % refentrytitle.module "IGNORE">
<!ENTITY % manvolnum.module "IGNORE">
]]>

<!ENTITY % bookbiblio.module "IGNORE">
<!ENTITY % seriesinfo.module "IGNORE">
<!ENTITY % itermset.module "IGNORE">
<!ENTITY % msgset.content.module "IGNORE">
<!ENTITY % msgset.module "IGNORE">
<!ENTITY % msgentry.module "IGNORE">
<!ENTITY % msg.module "IGNORE">
<!ENTITY % msgmain.module "IGNORE">
<!ENTITY % msgsub.module "IGNORE">
<!ENTITY % msgrel.module "IGNORE">
<!ENTITY % msginfo.module "IGNORE">
<!ENTITY % msglevel.module "IGNORE">
<!ENTITY % msgorig.module "IGNORE">
<!ENTITY % msgaud.module "IGNORE">
<!ENTITY % msgexplan.module "IGNORE">
<!ENTITY % qandaset.content.module "IGNORE">
<!ENTITY % qandaset.module "IGNORE">
<!ENTITY % qandadiv.module "IGNORE">
<!ENTITY % qandaentry.module "IGNORE">
<!ENTITY % question.module "IGNORE">
<!ENTITY % answer.module "IGNORE">
<!ENTITY % label.module "IGNORE">
<!ENTITY % bridgehead.module "IGNORE">
<!ENTITY % highlights.module "IGNORE">
<!ENTITY % formalpara.module "IGNORE">
<!ENTITY % simpara.module "IGNORE">
<!ENTITY % glosslist.module "IGNORE">
<!ENTITY % segmentedlist.content.module "IGNORE">
<!ENTITY % segmentedlist.module "IGNORE">
<!ENTITY % segtitle.module "IGNORE">
<!ENTITY % seglistitem.module "IGNORE">
<!ENTITY % seg.module "IGNORE">
<!ENTITY % calloutlist.content.module "IGNORE">
<!ENTITY % calloutlist.module "IGNORE">
<!ENTITY % callout.module "IGNORE">
<!ENTITY % informalexample.module "IGNORE">
<!ENTITY % programlistingco.module "IGNORE">
<!ENTITY % areaspec.content.module "IGNORE">
<!ENTITY % areaspec.module "IGNORE">
<!ENTITY % area.module "IGNORE">
<!ENTITY % areaset.module "IGNORE">
<!ENTITY % screenco.module "IGNORE">
<!ENTITY % screen.module "IGNORE">
<!ENTITY % screenshot.content.module "IGNORE">
<!ENTITY % screenshot.module "IGNORE">
<!ENTITY % screeninfo.module "IGNORE">
<!ENTITY % informalfigure.module "IGNORE">
<!ENTITY % graphicco.module "IGNORE">
<!ENTITY % graphic.module "IGNORE">
<!ENTITY % equation.module "IGNORE">
<!ENTITY % informalequation.module "IGNORE">
<!ENTITY % inlineequation.module "IGNORE">
<!ENTITY % inlinegraphic.module "IGNORE">
<!ENTITY % alt.module "IGNORE">
<!ENTITY % synopsis.module "IGNORE">
<!ENTITY % cmdsynopsis.content.module "IGNORE">
<!ENTITY % cmdsynopsis.module "IGNORE">
<!ENTITY % arg.module "IGNORE">
<!ENTITY % group.module "IGNORE">
<!ENTITY % sbr.module "IGNORE">
<!ENTITY % synopfragmentref.module "IGNORE">
<!ENTITY % synopfragment.module "IGNORE">
<!ENTITY % funcsynopsis.content.module "IGNORE">
<!ENTITY % funcsynopsis.module "IGNORE">
<!ENTITY % funcsynopsisinfo.module "IGNORE">
<!ENTITY % funcprototype.module "IGNORE">
<!ENTITY % funcdef.module "IGNORE">
<!ENTITY % void.module "IGNORE">
<!ENTITY % varargs.module "IGNORE">
<!ENTITY % paramdef.module "IGNORE">
<!ENTITY % funcparams.module "IGNORE">
<!ENTITY % ackno.module "IGNORE">
<!ENTITY % affiliation.element "IGNORE">
  <!ENTITY % shortaffil.module "IGNORE">
  <!ENTITY % orgdiv.module "IGNORE">
<!ENTITY % artpagenums.module "IGNORE">
  <!ENTITY % collab.content.module "IGNORE">
  <!ENTITY % collab.module "IGNORE">
    <!ENTITY % collabname.module "IGNORE">
<!ENTITY % confgroup.content.module "IGNORE">
<!ENTITY % confgroup.module "IGNORE">
  <!ENTITY % confdates.module "IGNORE">
  <!ENTITY % conftitle.module "IGNORE">
  <!ENTITY % confnum.module "IGNORE">
  <!ENTITY % confsponsor.module "IGNORE">
<!ENTITY % contractnum.module "IGNORE">
<!ENTITY % contractsponsor.module "IGNORE">
<!ENTITY % corpname.module "IGNORE">
<!ENTITY % invpartnumber.module "IGNORE">
<!ENTITY % modespec.module "IGNORE">
  <!ENTITY % contrib.module "IGNORE">
<!ENTITY % productname.module "IGNORE">
<!ENTITY % productnumber.module "IGNORE">
<!ENTITY % pubsnumber.module "IGNORE">
<!ENTITY % seriesvolnums.module "IGNORE">
<!ENTITY % accel.module "IGNORE">
<!ENTITY % action.module "IGNORE">
<!ENTITY % application.module "IGNORE">
<!ENTITY % classname.module "IGNORE">
<!ENTITY % co.module "IGNORE">
<!ENTITY % database.module "IGNORE">
<!ENTITY % envar.module "IGNORE">
<!ENTITY % errorcode.module "IGNORE">
<!ENTITY % errorname.module "IGNORE">
<!ENTITY % errortype.module "IGNORE">
<!ENTITY % function.module "IGNORE">
<!ENTITY % guibutton.module "IGNORE">
<!ENTITY % guiicon.module "IGNORE">
<!ENTITY % guilabel.module "IGNORE">
<!ENTITY % guimenu.module "IGNORE">
<!ENTITY % guimenuitem.module "IGNORE">
<!ENTITY % guisubmenu.module "IGNORE">
<!ENTITY % hardware.module "IGNORE">
<!ENTITY % interface.module "IGNORE">
<!ENTITY % interfacedefinition.module "IGNORE">
<!ENTITY % keycap.module "IGNORE">
<!ENTITY % keycode.module "IGNORE">
<!ENTITY % keycombo.module "IGNORE">
<!ENTITY % keysym.module "IGNORE">
<!ENTITY % constant.module "IGNORE">
<!ENTITY % varname.module "IGNORE">
<!ENTITY % markup.module "IGNORE">
<!ENTITY % mediaobjectco.module "IGNORE">
<!ENTITY % imageobjectco.module "IGNORE">
<!ENTITY % medialabel.module "IGNORE">
<!ENTITY % menuchoice.content.module "IGNORE">
<!ENTITY % menuchoice.module "IGNORE">
<!ENTITY % shortcut.module "IGNORE">
<!ENTITY % mousebutton.module "IGNORE">
<!ENTITY % msgtext.module "IGNORE">
<!ENTITY % optional.module "IGNORE">
<!ENTITY % parameter.module "IGNORE">
<!ENTITY % prompt.module "IGNORE">
<!ENTITY % property.module "IGNORE">
<!ENTITY % returnvalue.module "IGNORE">
<!ENTITY % sgmltag.module "IGNORE">
<!ENTITY % structfield.module "IGNORE">
<!ENTITY % structname.module "IGNORE">
<!ENTITY % symbol.module "IGNORE">
<!ENTITY % token.module "IGNORE">
<!ENTITY % type.module "IGNORE">
<!ENTITY % citation.module "IGNORE">
<!ENTITY % citerefentry.module "IGNORE">
<!ENTITY % firstterm.module "IGNORE">
<!ENTITY % foreignphrase.module "IGNORE">
<!ENTITY % wordasword.module "IGNORE">
<!ENTITY % olink.module "IGNORE">
<!ENTITY % footnoteref.module "INCLUDE">
<!ENTITY % beginpage.module "IGNORE">
<!ENTITY % indexterm.content.module "IGNORE">
<!ENTITY % indexterm.module "IGNORE">
<!ENTITY % primsecter.module "IGNORE">
<!ENTITY % seeseealso.module "IGNORE">

<!-- new in 4.0 -->
<!ENTITY % classsynopsis.content.module "IGNORE">

<!ENTITY % bibliographyinfo.module "IGNORE">
<!ENTITY % chapterinfo.module "IGNORE">
<!ENTITY % glossaryinfo.module "IGNORE">
<!ENTITY % indexinfo.module "IGNORE">
<!ENTITY % partinfo.module "IGNORE">
<!ENTITY % prefaceinfo.module "IGNORE">
<!ENTITY % refentryinfo.module "IGNORE">
<!ENTITY % referenceinfo.module "IGNORE">
<!ENTITY % refsect1info.module "IGNORE">
<!ENTITY % refsect2info.module "IGNORE">
<!ENTITY % refsect3info.module "IGNORE">
<!ENTITY % refsynopsisdivinfo.module "IGNORE">
<!ENTITY % sect1info.element "IGNORE">
<!ENTITY % sect1info.attlist "IGNORE">
<!ENTITY % sect2info.element "IGNORE">
<!ENTITY % sect2info.attlist "IGNORE">
<!ENTITY % sect3info.element "IGNORE">
<!ENTITY % sect3info.attlist "IGNORE">
<!ENTITY % sect4info.element "IGNORE">
<!ENTITY % sect4info.attlist "IGNORE">
<!ENTITY % sect5info.element "IGNORE">
<!ENTITY % sect5info.attlist "IGNORE">
<!ENTITY % sectioninfo.module "IGNORE">
<!ENTITY % setindexinfo.module "IGNORE">
<!ENTITY % sidebarinfo.module "IGNORE">

<!-- new in 4.2 -->
<!ENTITY % blockinfo.module "IGNORE">
<!ENTITY % refsectioninfo.module "IGNORE">
<!ENTITY % refsection.module "IGNORE">
<!ENTITY % personname.module "IGNORE">
<!ENTITY % personblurb.module "IGNORE">
<!ENTITY % bibliocoverage.module "IGNORE">
<!ENTITY % biblioid.module "IGNORE">
<!ENTITY % bibliorelation.module "IGNORE">
<!ENTITY % bibliosource.module "IGNORE">
<!ENTITY % citebiblioid.module "IGNORE">
<!ENTITY % coref.module "IGNORE">
<!ENTITY % errortext.module "IGNORE">

<!-- End of Simplified DocBook Include Module V1.0 ........................ -->
<!-- ...................................................................... -->
