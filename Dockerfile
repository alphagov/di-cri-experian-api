FROM gradle:jdk11@sha256:d7c6aafd580ad027f529fe611e4f0ac9d29fd949af889d5f2625f8a7091497b3
WORKDIR /app
COPY build.gradle ./
COPY src ./src
EXPOSE 5007
CMD ["gradle","run"]