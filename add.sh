#!/bin/bash
EPUB=$1
REPORTFILE=$2
EPUBFILENAME=${EPUB##*/}

if [ "x$REPORTFILE" = "x" ]; then
	if [ -f "${EPUB%.epub}-report.html" ]; then
		REPORTFILE="${EPUB%.epub}-report.html"
	fi
fi
if [ -f "$REPORTFILE" ]; then
	echo "Updating report"
	cp $REPORTFILE reports/
	filename=`cat $REPORTFILE | sed -n 's/.*"filename" : "\(.*\)",/\1/p'`;
	nFatal=`cat $REPORTFILE | sed -n 's/.*"nFatal" : \([^,]*\),/\1/p'`;
	nError=`cat $REPORTFILE | sed -n 's/.*"nError" : \([^,]*\),/\1/p'`;
	nWarning=`cat $REPORTFILE | sed -n 's/.*"nWarning" : \([^,]*\),/\1/p'`;
	valid=$((nFatal + nError))
	if [[ $valid -ne 0 ]]
	then
		status="Invalide";
	else
		status="Valide";
	fi
	REPORT=$(printf '<tr><td><a href="%s">%s</a></td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>' "${REPORTFILE##*/}" "$filename" "$status" "$nFatal" "$nError" "$nWarning")
	echo $REPORT
	cat report.html | sed -n '/__REPORT__/,$!p' > reports/report2.html
	echo "$REPORT" >> reports/report2.html
	cat reports/report.html | grep "^<tr" | grep -v "${EPUB##*/}" >> reports/report2.html
	cat report.html | sed -n '1,/__REPORT__/!p' >> reports/report2.html
	rm reports/report.html
	mv reports/report2.html reports/report.html
fi

cp $1 epub/
rm archives/epubs.zip
(cd epub ; zip ../archives/epubs.zip *.epub)

NAME=${EPUBFILENAME%%.*}
rm -Rf library/$NAME
mkdir library/$NAME
(cd library/$NAME ; jar -xf ../../epub/$EPUBFILENAME)

exit


echo "[" > epub_library.json
i=0
for FILE in library/* ; 
do  
    if [[ $i -ne 0 ]]
	then
		echo "," >> epub_library.json
	fi
	echo "  {" >> epub_library.json
	echo "    \"title\" : \"`cat $FILE/content.opf | sed -n 's/.*<dc:title>\(.*\)<\/dc:title>.*/\1/p'`\"," >> epub_library.json
	echo "    \"author\" : \"`cat $FILE/content.opf | sed -n 's/.*<dc:creator.*>\(.*\)<\/dc:creator>.*/\1/p'`\"," >> epub_library.json
	echo "    \"coverHref\" : \"../$FILE/OEBPS/img/cover.png\","  >> epub_library.json
	echo "    \"packagePath\" : \"../$FILE/content.opf\","  >> epub_library.json
	echo "    \"rootUrl\": \"../$FILE\"" >> epub_library.json
	echo -n "  }" >> epub_library.json
	i=`expr $i + 1`
done
echo "" >> epub_library.json
echo "]" >> epub_library.json
rm readium-js-viewer/epub_content/epub_library.json
mv epub_library.json readium-js-viewer/epub_content/

