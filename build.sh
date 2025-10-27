
# Compile java files to 
javac -d bin -cp ./lib/mysql-connector.jar ./src/com/itsapp/*.java

# Create the jar file
jar cvfm ITS_APP.jar Manifest.txt -C bin .

mv ITS_APP.jar dist
cp -r lib dist


