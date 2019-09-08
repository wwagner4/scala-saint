# scala-saint

Requires doctus 1.0.6

To get doctus follow these steps

* git clone https://github.com/wwagner4/doctus1.git
* sbt publish-local

That should create the necessary jars in your local repository 

# docker
```
git clone https://github.com/wwagner4/scala-saint.git
docker build -t saint .

docker run -it --rm -p 8885:8099 saint
docker run -p 8885:8099 saint &
```
