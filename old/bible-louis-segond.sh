#!/bin/bash                
                 
AUTHOR="Louis Segond"
ABBR="bls" 
UUID="3de381d5-a71f-4bdf-962a-2bc36abab4dE"
OUTPUT="Bible-Louis-Segond.epub"     
      
if [ ! -d target ]
then
  mkdir target                                                            
fi                                                                        
cd target
if [ ! -d batik-1.7 ]
then
  echo "Downloading batik"
  wget http://apache.mirrors.multidist.eu//xmlgraphics/batik/batik-1.7.zip
  jar -xf batik-1.7.zip
fi             
if [ ! -d epubcheck-3.0-RC-1 ]
then
  echo "Downloading epubcheck"
  wget http://epubcheck.googlecode.com/files/epubcheck-3.0-RC-1.zip
  jar -xf epubcheck-3.0-RC-1.zip
fi
if [ ! -d $ABBR ]
then
  mkdir $ABBR
fi 
cd $ABBR 
if [ ! -d org ]
then
  mkdir org
fi         
cd org
if [ ! -e bible_lsg_complete.zip ]
then
  echo "Downloading"
  cp ../../../bible_lsg_complete.zip .
#  wget http://www.europepourchrist.org/bible/iso_album/bible_lsg_complete.zip
fi
cd ..
if [ ! -d out ]
then
  mkdir out
fi        
echo "preserve-entities: yes" > tidy1.conf 
echo "preserve-entities: no" > tidy2.conf 
echo "input-encoding: latin1" >> tidy2.conf
echo "output-encoding: utf-8" >> tidy2.conf   

cat >quotes.sed <<EOF
#----------------------------------------------------------------------
# proper_quotes_nonwin.sed
# Copyright Stephen Poley.  May be freely used for non-commercial purposes.
# http://www.xs4all.nl/~sbpoley/
#----------------------------------------------------------------------
#
# This sed script changes plain quotation marks in HTML to proper
# typographical quotation marks, and hyphens used as dashes to dash
# characters. It also replaces numeric character references
# with character entity references for consistency.
#
# Non-Windows version: line-feed terminators 
#
# Any quotes which must not be changed can be represented by &#39; (single) or 
# &#34; or &quot; (double).
#
# A few characters will be left unchanged in situations where it is hard to 
# determine if a left or right quotation mark is appropriate.
#
# Running a file through this script multiple times should produce the same
# result as running it once; it doesn't matter if a file has already been fully
# or partially converted to proper quotation marks. 
#
# Restrictions:
# 1) Doesn't recognise multi-line HTML comments, so any quotes in such
#    comments will also be converted.
# 2) Within the body, can't handle HTML attributes which span a line break
#    (these are probably rare and arguably bad practice anyway).
# 3) Within the head, HTML attributes spanning a line break (Meta elements)
#    are handled OK provided the <HEAD> and </HEAD> tags are present.
#
# Last updates: 
#   29-9-2003, handle multi-line Meta's
#   1-4-2006, handle digit-quote-punctuation sequence
#----------------------------------------------------------------------

# skip DOCTYPE
/<![Dd][Oo][Cc]/n

# Handle HEAD. 'h' in hold space signifies processing head element
/<[Hh][Ee][Aa][Dd]>/ {
x
s/^/h/
x
}

/<\/[Hh][Ee][Aa][Dd]>/ {
x
s/h//
x
}

x
/h/ {
x
b endscr
}
x

s/«/\&laquo;/g    
s/»/\&raquo;/g
s/“/\&ldquo;/g
s/”/\&rdquo;/g
s/‹/\&lsaquo;/g
s/›/\&rsaquo;/g
s/„/\&bdquo;/g
s/“/\&ldquo;/g
s/‚/\&sbquo;/g
s/‘/\&lsquo;/g
s//\&rsquo;/g
#s/\&#39;/\&rsquo;/g  
#s/\&#171;/\&laquo;/g        
#s/\&#187;/\&raquo;/g        
#s/\&#156;/\&oelig;/g   
#s/\&#151;/\&ndash;/g    
#s/\&#339;/\&oelig;/g   
#s/í/\&rsquo;/g
#s/ó/\&ndash;/g 
#s/ñ/\&ndash;/g 
s//\&oelig;/g
#s/ú/\&oelig;/g

# replace any degree characters, so can use them temporarily
s/°/\&deg;/g

# temporarily replace quotes in attributes and comments 
s/="\([^"]*\)"/=°\1°/g
: commdq
s/\(<!--[^>]*\)"\([^>]*>\)/\1°\2/g
t commdq

# update real quotes
s/"\([A-Za-z]\)/\&ldquo;\1/g
s/\([A-Za-z]\)"/\1\&rdquo;/g
s/ "\([^ ]\)/ \&ldquo;\1/g
s/^"\([^ ]\)/\&ldquo;\1/
s/\([^ ]\)" /\1\&rdquo; /g
s/\([^ ]\)"$/\1\&rdquo;/
s/\([^ ]\)"\([ ;,:.]\)/\1\&rdquo;\2/g
# digit-quote-punctuation sequence
s/\([0-9]\)"\([;,:.]\)/\1\&rdquo;\2/g
                  
: innerquotes
s/\(\&laquo;.*\)\&quot;\(.*\)\&quot;\(.*\&raquo;\)/\1\&ldquo;\2\&rdquo;\3/g
t innerquotes

# put attribute quotes back
s/°/"/g

# now repeat for single quotes. Remember right single quote is also an
# apostrophe, so do right quotes first.
s/='\([^']*\)'/=°\1°/g
: commsq
s/\(<!--[^>]*\)'\([^>]*>\)/\1°\2/g
t commsq
s/\([A-Za-z]\)'/\1\&rsquo;/g
s/'\([A-Za-z]\)/\&lsquo;\1/g
s/\([^ ]\)' /\1\&rsquo; /g
s/\([^ ]\)'$/\1\&rsquo;/
s/ '\([^ ]\)/ \&lsquo;\1/g
s/^'\([^ ]\)/\&lsquo;\1/
s/\([0-9]\)'\([;,:.]\)/\1\&rsquo;\2/g
s/°/'/g

# dashes: 
# - replace a hyphen surrounded by spaces with an en-dash;
# - replace a double hyphen which isn't part of an HTML comment delimiter
#   with an em-dash.
s/ - / \&ndash; /g
s/\(.\)_ /\1\&ndash; /g
s/ _\(.\)/ \&ndash;\1/g
s/\([^-!]\)[ ]*--[ ]*\([^->]\)/\1\&mdash;\2/g

# And finally tidy up any numerical character references for consistency
s/\&#8211;/\&ndash;/g
s/\&#8212;/\&mdash;/g
s/\&#8216;/\&lsquo;/g
s/\&#8217;/\&rsquo;/g
s/\&#8220;/\&ldquo;/g
s/\&#8221;/\&rdquo;/g
: endscr
                               

EOF

cat >whitespaces.sed <<EOF
# Fix whitespaces   
/<body/,/<\/body>/ {

  # Fix ellipsis
  s/\.\.\./\&hellip;/g

  s/\&laquo;\([[:graph:]]\)/\&laquo;\&nbsp;\1/g
  s/\([[:graph:]]\)\&raquo;/\1\&nbsp;\&raquo;/g
  s/\&raquo;/\&nbsp;\&raquo;/g
  s/\([[:graph:]]\)<\/i>\&raquo;/\1<\/i>\&nbsp;\&raquo;/g
  s/\([[:graph:]]\)\.\([[:graph:]]\)/\1. \2/g
  s/\([[:graph:]]\):/\1\&nbsp;:/g
  s/\([[:graph:]]\),\([[:graph:]]\)/\1, \2/g
  s/\&laquo; /\&laquo;\&nbsp;/g
  s/ \&raquo;/\&nbsp;\&raquo;/g
  s/\&ldquo; /\&ldquo;/g
  s/ \&rdquo;/\&rdquo;/g
  s/\&lsaquo; /\&lsaquo;\&nbsp;/g
  s/ \&rsaquo;/\&nbsp;\&rsaquo;/g
  s/\&bdquo; /\&bdquo;\&nbsp;/g
  s/ \&ldquo;/\&nbsp;\&ldquo;/g
  s/\&sbquo; /\&sbquo;\&nbsp;/g
  s/ \&lsquo;/\&nbsp;\&lsquo;/g  
  s/\([A-Za-z)>]\)\.\([A-Za-z]\)/\1. \2/g
  s/\([^\s]\): /\1\&nbsp;: /g               
  s/\([^\s]\)!/\1\&nbsp;!/g
  s/\([^\s]\)\?/\1\&nbsp;?/g
  s/ : /\&nbsp;: /g             
  s/\([^\s]\):/\1\&nbsp;:/g  
  s/\([ ;][^\& ]*\); /\1\&nbsp;; /g                  
  s/\([ ;][^\& ]*\);\([[:graph:]]\)/\1\&nbsp;; \2/g                  
  s/\([ ;][^\& ]*\): /\1\&nbsp;: /g                  
  s/\([ ;][^\& ]*\):\([[:graph:]]\)/\1\&nbsp;: \2/g                  
  s/\([[:graph:]]\)!/\1\&nbsp;!/g
  s/\([[:graph:]]\)\?/\1\&nbsp;?/g
  s/\([A-Za-z]\)\?\&nbsp;/\1\&nbsp;?\&nbsp;/g   
  s/\([[:graph:]]\)<i>/\1 <i>/g
  s/( /(/g
  s/ )/)/g
  s/\([[:graph:]]\)- /\1 \&ndash; /g
  s/ -\([[:graph:]]\)/ \&ndash; \1/g
  s/ ,/,/g                                         
  s/\&rsquo; /\&rsquo;/g
  
  s/\( *\&nbsp; *\)\( *\&nbsp; *\)*/\&nbsp;/g

  # Fix wrongly modified href
  :l0
  s/\(="[^"]*\)\&nbsp;\([^"]*"\)/\1\2/g
  t l0
  :l1
  s/\(="[^"]*\) \([^"]*"\)/\1\2/g
  t l1
}  

EOF

cd out
mkdir OEBPS 2>/dev/null     
cd OEBPS
jar -xf ../../org/bible_lsg_complete.zip
for FILE in *.css
do
  echo "Processing $FILE"             
  perl -e '                                                                                
      # Load file
      $file = $ARGV[0];  local( *FH ) ;  open( FH, $file ); my $text = do { local( $/ ) ; <FH> } ; 
      # CSS
      $text =~ s/margin-right: 5%; margin-left: 5%; //;
	  print $text;' $FILE > $FILE.tmp ; cp $FILE.tmp $FILE ; rm $FILE.tmp	
done
for FILE in *.htm*
do           
  echo "Processing $FILE"             
  iconv -f ISO-8859-1 -t UTF-8 $FILE > $FILE.tmp ; cp $FILE.tmp $FILE ; rm $FILE.tmp
  perl -e '                                                                                
      # Load file
      $file = $ARGV[0];  local( *FH ) ;  open( FH, $file ); my $text = do { local( $/ ) ; <FH> } ; 
      # Typos and accents
	  $text =~ s/Evangile/\&Eacute;vangile/g;
      # CSS
      $text =~ s/<TR><TD WIDTH="5%">\&nbsp;<\/TD><TD WIDTH="5%">\&nbsp;<\/TD><TD WIDTH="5%">\&nbsp;<\/TD><TD WIDTH="75%">\&nbsp;<TD>&nbsp;<\/TD><\/TR>/<TR><TD WIDTH="5%">\&nbsp;<\/TD><TD WIDTH="1%">\&nbsp;<\/TD><TD WIDTH="1%">\&nbsp;<\/TD><TD WIDTH="92%">\&nbsp;<\/TD><TD>&nbsp;<\/TD><\/TR>/;
      # set charset
      $text =~ s/<head>/<head><meta http-equiv="Content-Type" content="text\/html; charset=utf-8" \/>/i;
	  print $text;' $FILE > $FILE.tmp ; cp $FILE.tmp $FILE ; rm $FILE.tmp	
  gsed -f ../../quotes.sed $FILE > $FILE.tmp ; cp $FILE.tmp $FILE ; rm $FILE.tmp 
  sed -f ../../whitespaces.sed $FILE > $FILE.tmp ; cp $FILE.tmp $FILE ; rm $FILE.tmp 
  tidy -config ../tidy2.conf -asxml -i -n -c -m -w 9999 -utf8 $FILE 2> /dev/null    
done 
cd ..
                                         
echo -n "application/epub+zip" > mimetype
mkdir META-INF 2>/dev/null
cat > META-INF/container.xml <<EOF
<?xml version="1.0"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
  <rootfiles>
    <rootfile full-path="content.opf" media-type="application/oebps-package+xml"/>
  </rootfiles>
</container>
EOF
cat > titlepage.xhtml <<EOF
<?xml version='1.0' encoding='utf-8'?>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>Cover</title>
        <style type="text/css" title="override_css">
            @page {padding: 0pt; margin:0pt}
            body { text-align: center; padding:0pt; margin: 0pt; }
        </style>
    </head>
    <body>
        <div>
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" width="100%" height="100%" viewBox="0 0 531 751" preserveAspectRatio="none">
                <image width="531" height="751" xlink:href="cover.jpeg"/>
            </svg>
        </div>
    </body>
</html>
EOF
                       
if [ ! -f cover.jpeg ]
then                        
  echo "Creating cover page"     
  cat >cover.svg <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" 
	 id="svg2" version="1.1"
     width="531" height="751">
    <rect x="0" y="0" width="100%" height="100%" fill="white" />
	<text style="font-size:72px;font-style:normal;font-weight:bold;fill:#000000;font-family:Brush Script MT" 
		  x="265" y="240">
		<tspan style="text-align:center;text-anchor:middle">Bible</tspan>
	</text>
	<text style="font-size:72px;font-style:normal;font-weight:bold;fill:#000000;font-family:Brush Script MT" 
		  x="265" y="374">
        <tspan style="text-align:center;text-anchor:middle">Louis Segond</tspan>
    </text>
</svg>
EOF
  java -jar ../../batik-1.7/batik-rasterizer.jar -m image/jpeg -q 0.9 cover.svg > /dev/null
  rm cover.svg                                                             
  mv cover.jpg cover.jpeg
fi

cat >content.opf <<EOF                                         
<?xml version='1.0' encoding='utf-8'?>
<package xmlns="http://www.idpf.org/2007/opf" version="2.0" unique-identifier="uuid_id">
  <metadata xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:opf="http://www.idpf.org/2007/opf" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:calibre="http://calibre.kovidgoyal.net/2009/metadata" xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:title>Bible de Louis Segond</dc:title>
    <dc:language>fr</dc:language>
    <dc:creator opf:file-as="$AUTHOR" opf:role="aut">$AUTHOR</dc:creator>
    <meta name="cover" content="cover"/>
    <dc:identifier id="uuid_id" opf:scheme="uuid">$UUID</dc:identifier>
  </metadata>
  <manifest>
    <item id="cover" href="cover.jpeg" media-type="image/jpeg"/>
    <item id="titlepage" href="titlepage.xhtml" media-type="application/xhtml+xml"/>
    <item id="index" href="OEBPS/index.htm" media-type="application/xhtml+xml"/>
EOF
FILES=`sed -e '/href/!d ; s/.*href=.// ; s/. text.*//' ../../../bible-toc.xml `
i=0
for FILE in $FILES
do
  i=$(($i + 1))
  is=`printf "%02.f" $i`
  echo "    <item id=\"s$is\" media-type=\"application/xhtml+xml\" href=\"OEBPS/$FILE\"/>" >> content.opf
done
cat >>content.opf <<EOF       
    <!--                                  
    <item href="page_styles.css" id="page_css" media-type="text/css"/>
    -->
    <item href="toc.ncx" media-type="application/x-dtbncx+xml" id="ncx"/>
    <item href="OEBPS/style_bible.css" id="css" media-type="text/css"/>
  </manifest>
  <spine toc="ncx"> 
    <itemref idref="titlepage"/>
    <itemref idref="index"/>
EOF
i=0
for FILE in $FILES
do
  if [ $FILE != "OEBPS/index.htm" ]
  then
    i=$(($i + 1))
    is=`printf "%02.f" $i`
    echo "    <itemref idref=\"s$is\"/>" >> content.opf
  fi
done
cat >>content.opf <<EOF                                         
  </spine>
  <guide>
    <reference href="titlepage.xhtml" type="cover" title="Cover"/>
  </guide>
</package>
EOF

echo "    Generating index"
xsltproc ../../../bible-index.xsl ../../../bible-toc.xml > OEBPS/index.htm

echo "    Generating toc"
xsltproc ../../../bible-toc.xsl ../../../bible-toc.xml > toc.ncx

echo "    Zipping"
zip -X $OUTPUT mimetype
zip -rg $OUTPUT META-INF -x \*.DS_Store
zip $OUTPUT content.opf
zip $OUTPUT cover.jpeg
zip -rg $OUTPUT OEBPS -x \*.DS_Store
#zip $OUTPUT page_styles.css
#zip $OUTPUT stylesheet.css
zip $OUTPUT titlepage.xhtml
zip $OUTPUT toc.ncx     

/Applications/calibre.app/Contents/MacOS/ebook-convert $OUTPUT ../../$OUTPUT --extra-css OEBPS/style_bible.css

#cp $OUTPUT ../..




