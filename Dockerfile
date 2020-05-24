FROM clojure:openjdk-14-tools-deps-slim-buster as builder

WORKDIR /usr/src/app

# install cambada builder
RUN clj -Sdeps '{:deps {luchiniatwork/cambada {:mvn/version "1.0.0"}}}' -e :ok 

# install main deps
COPY deps.edn /usr/src/app/deps.edn
RUN clj -e :ok

# build
COPY resources/ /usr/src/app/resources
COPY src/ /usr/src/app/src
RUN clj -A:uberjar

# use clean image
FROM openjdk:13-slim-buster

ENV PORT 8080
EXPOSE 8080

COPY --from=builder /usr/src/app/target/app-1.0.0-SNAPSHOT-standalone.jar /usr/src/app/app.jar

CMD ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=85","-XX:+UnlockExperimentalVMOptions","-XX:+UseZGC","-jar","/usr/src/app/app.jar"]
