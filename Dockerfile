From openjdk
copy ./manager/target/manager-1.0-SNAPSHOT.jar manager.jar
CMD ["java","-jar","manager.jar"]
