FROM openjdk:11

COPY saint-images/* /root/saint/data/

COPY jar/* /app/

WORKDIR /app

EXPOSE 8099

CMD java -jar saint.jar

