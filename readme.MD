Prerequisites cmd
gradle wrapper

To Test
./gradlew test

Get latest build
./gradlew shadowJar

Execute your program
java -jar build/libs/kafka-streams-application-\*.jar configuration/dev.properties
