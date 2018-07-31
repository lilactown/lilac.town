FROM clojure:alpine

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . /usr/src/app

RUN npm install

RUN lein run -m shadow.cljs.devtools.cli release client

RUN lein uberjar

CMD ["java", "-jar", "target/uberjar/lilactown.jar", "3001"]