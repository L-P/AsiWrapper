#!/bin/sh

WPATH=asiwrapper/
SPATH=asiwrapper_source/
WTAR=asiwrapper.tar.gz
STAR=asiwrapper_source.tar.gz
DEST=~/Desktop

make clean && make && make jar && make clean > /dev/null
echo -n Creating $WTAR…
mkdir $WPATH
cp ../AsiWrapper.jar $WPATH
cp config/* $WPATH
tar czf $WTAR $WPATH
rm -R $WPATH
echo " done."


echo -n Creating $STAR…
svn export svn://leo-peltier.fr/asiwrapper $SPATH > /dev/null
tar czf $STAR $SPATH
rm -R $SPATH
echo " done."

chmod 444 $WTAR $STAR
mv -ft $DEST $WTAR $STAR

