FROM clojure:alpine

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . /usr/src/app
RUN lein uberjar
CMD ["java", "-jar", "target/uberjar/lilactown.jar", "3001"]