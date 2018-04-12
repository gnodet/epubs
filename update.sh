#!/bin/bash
mkdir temp
mv epub/le_maitre_de_la_terre.epub temp
rm -Rf archives
rm -Rf library
rm -Rf epub
rm -Rf reports
mv temp epub

echo "Copying files"
cp -R ../epubs/target/site/ .
rm -Rf svgs
rm various.html

echo "Creating reports"
../epubs/check.sh epub/*.epub
mkdir reports
cd reports
mv ../epub/*.html .
REPORT=""
for FILE in *-report.html;
do
	filename=`cat $FILE | sed -n 's/.*"filename" : "\(.*\)",/\1/p'`;
	nFatal=`cat $FILE | sed -n 's/.*"nFatal" : \([^,]*\),/\1/p'`;
	nError=`cat $FILE | sed -n 's/.*"nError" : \([^,]*\),/\1/p'`;
	nWarning=`cat $FILE | sed -n 's/.*"nWarning" : \([^,]*\),/\1/p'`;
	valid=$((nFatal + nError))
	if [[ $valid -ne 0 ]]
	then
		status="Invalide";
	else
		status="Valide";
	fi
	REPORT=$(printf '%s\n<tr><td><a href="%s">%s</a></td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>' "$REPORT" "$FILE" "$filename" "$status" "$nFatal" "$nError" "$nWarning")
#	REPORT="$REPORT<tr><td><a href=\"$FILE\">$filename</a></td><td>$status</td><td>$nFatal</td><td>$nError</td><td>$nWarning</td></tr>\n";
done
cat ../report.html | sed -n '/__REPORT__/,$!p' > report.html
echo "$REPORT" >> report.html
cat ../report.html | sed -n '1,/__REPORT__/!p' >> report.html
cd ..

echo "Creating library"
mkdir library
cd library
for FILE in ../epub/*.epub ;
do
	NAME=`echo "$FILE" | cut -d '/' -f 3 | cut -d '.' -f 1`
	mkdir $NAME
	(cd $NAME ; jar -xf ../$FILE)
done
cd ..

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

echo "Creating archive"
mkdir archives
cd epub
zip ../archives/epubs.zip *.epub 2>/dev/null
cd ..
