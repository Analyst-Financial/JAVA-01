1. compile Hello.java
javac Hello.java

2. use fileconvert.jar to get xlass file
java -cp fileconvert.jar io.github.kimmking.Main Hello.class

rename Hello.class.fck to Hello.xlass

3. write a class loader to load xlass file and covnert it to Hello.class
javac HelloClassLoader.java
java HelloClassLoader
