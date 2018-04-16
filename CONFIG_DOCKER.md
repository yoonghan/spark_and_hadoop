## Stop
kubeadm reset

# create a  network
docker network create my-network
```
docker run -d --net=my-network --name=mydatabase mysql
docker run -d --net=my-network --name=myapp myappimage
```

# Docker ip
docker inspect <container_id>

# Exposing
Neither specify EXPOSE nor -p.
Only specify EXPOSE.
Specify EXPOSE and -p.
If you do not specify any of those, the service in the container will not be accessible from anywhere except from inside the container itself.

If you EXPOSE a port, the service in the container is not accessible from outside Docker, but from inside other Docker containers. So this is good for inter-container communication.

If you EXPOSE and -p a port, the service in the container is accessible from anywhere, even outside Docker.

# Local registry
docker run -d -p 5000:5000 --name registry registry:2
https://docs.docker.com/registry/deploying/#copy-an-image-from-docker-hub-to-your-registry
For insecure registry:
*Both servers*:
Edit /etc/docker/daemon.json with
```
{
  "insecure-registries" : ["myregistrydomain.com:5000"]
}
```
For Mac book follow https://docs.docker.com/registry/insecure/#deploy-a-plain-http-registry

*Both servers*:
service docker restart

*Server A:*
docker tag justice-league localhost:5000/justice-league
docker push 192.168.1.244:5000/justice-league

*Server B:*
docker pull 192.168.1.244:5000/justice-league

# Calling bridging access
Use --net="host" in your docker run command, then 127.0.0.1 in your docker container will point to your docker host.
Use --net="bridge" (default). Docker creates a bridge named docker0 by default. Both the docker host and the docker containers have an IP address on that bridge.
New solution is to use host.docker.internal, for mac it's docker.for.mac.host.internal with -hdocker.for.mac.localhost

# SBT Docker Calling
//Make sure sbt-native [https://www.scala-sbt.org/sbt-native-packager/] is imported.
sbt clean docker:publishLocal
