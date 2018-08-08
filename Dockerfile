FROM clojure:tools-deps-alpine

# Pass in git commit SHA from build command
ARG GIT_COMMIT=unknown

ENV GIT_COMMIT=$GIT_COMMIT

# Install npm
RUN apk add --update nodejs nodejs-npm

RUN mkdir -p /usr/app

WORKDIR /usr/app

COPY package.json .

RUN npm install

COPY deps.edn .

RUN clojure -A:server -Spath

COPY . /usr/app

RUN npx shadow-cljs release client

RUN clojure -A:uberjar

CMD java -jar dist/lilactown.jar 3001 ${GIT_COMMIT}
