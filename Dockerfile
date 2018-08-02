FROM clojure:tools-deps-alpine

# Install npm
RUN apk add --update nodejs nodejs-npm

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . /usr/src/app

RUN npm install

RUN clojure -A:client:client/build

# RUN clojure -A:uberjar

CMD ["clojure", "-A:server:client", "-m", "lilactown.core", "3001"]

# CMD ["java", "-jar", "dist/lilactown.jar"]