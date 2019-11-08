rm -rf bin
find . -type f \( -iname "*.java" \) > sources.txt
mkdir bin
javac -d bin @sources.txt
rm sources.txt
mkdir -p bin/conquest/view/resources/images
cp src/conquest/view/resources/images/* bin/conquest/view/resources/images/