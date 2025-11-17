# VS CODE: What you need
1. Java 17
2. Maven
3. Scenebuilder (Optional)
Okay so when you install Maven it automatically downloads JavaFX
 
# Install
1.  Maven
```sh
winget install Apache.Maven
```
# Open VS Code and install these extensions:
	1.	Java Extension Pack
	2.	Maven for Java
	3.	Debugger for Java
# Open the JavaFX Project in VS Code
	1.	Open VS Code
	2.	Select “File > Open Folder”
	3.	Choose the project folder (example: eqlab2)
	4.	Wait for Maven to load dependencies

# FOR LINUX
ensure:
	•	Java 17 is installed
	•	Maven is installed
	•	pom.xml contains JavaFX modules

```sh
mvn javafx:run
```
