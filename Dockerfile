FROM clojure:openjdk-11-tools-deps as builder

WORKDIR /usr/src/app

# install cambada builder
RUN clj -Sdeps '{:deps {luchiniatwork/cambada {:mvn/version "1.0.0"}}}' -e :ok 

# install main deps
COPY deps.edn /usr/src/app/deps.edn
RUN clj -e :ok

# build
COPY src/ /usr/src/app/src
RUN clj -A:uberjar

# use clean image
FROM openjdk:11

COPY --from=builder /usr/src/app/target/app-1.0.0-SNAPSHOT-standalone.jar /usr/src/app/app.jar

CMD ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=90","-jar","/usr/src/app/app.jar"]
