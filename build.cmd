:: NOTE: I kinda just gemini'd this from my bash script so if it doesn't work, tell me to update it - Gian
:: NOTE2: RUN THIS IN THE PROJECT ROOT DIRECTORY

:: remake the bin folder
rd /s /q bin
md bin

:: compile java files to 
javac -d bin -cp ./lib/mysql-connector.jar ./src/com/itsapp/*.java

:: create the jar file
jar cvfm ITS_APP.jar Manifest.txt -C bin .

:: move jar and copy lib to dist
move ITS_APP.jar dist
xcopy lib dist\lib /s /e /i
