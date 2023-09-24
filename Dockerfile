FROM maven AS build
COPY . .
RUN mvn -f ./manager/pom.xml clean package

FROM openjdk
COPY --from=build ./manager/target/manager-1.0-SNAPSHOT.jar manager.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","manager.jar"]
