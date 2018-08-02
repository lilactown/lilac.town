FROM clojure:tools-deps-alpine

# Pass in git commit SHA from build command
ARG GIT_COMMIT=unknown

ENV GIT_COMMIT=$GIT_COMMIT

# Install npm
# RUN apk add --update nodejs nodejs-npm

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . /usr/src/app

# RUN npm install

# RUN clojure -A:client:client/build

RUN clojure -A:uberjar

CMD java -jar dist/lilactown.jar 3001 ${GIT_COMMIT}
