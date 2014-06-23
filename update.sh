#!/bin/bash

rm -Rf library
rm -Rf epub
cp -R ../epubs/target/site/ .
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

