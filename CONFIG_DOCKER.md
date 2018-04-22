## Installing docker
Execute the following commands
```
curl -sSL https://get.docker.com | sh
sudo systemctl enable docker
sudo systemctl start docker
# Enable pi user to run docker with superuser privilege
sudo usermod -aG docker pi
sudo apt-get install docker-compose
```

## Stop
kubeadm reset

## create a  network
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
  "insecure-registries" : ["192.168.1.244:5000"]
}
```
For Mac book follow https://docs.docker.com/registry/insecure/#deploy-a-plain-http-registry

*Both servers*:
service docker restart

*Server A:*
docker tag justice-league 192.168.1.244:5000/justice-league
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

# Passing variables
Use the -e command, i.e.
```
docker run -p2551:2551 -e CLUSTER_IP=172.17.0.3 -e CLUSTER_PORT=2551 -e CLUSTER_IP2=192.168.1.244 -e CLUSTER_PORT2=2552 justice-league-impl:1.0-SNAPSHOT
```

Then the codes in application.conf are configured as
```
# Seeds should be open like this, there must be 2 or more seeds defined
akka.cluster.seed-nodes = [
  "akka.tcp://application@"${CLUSTER_IP}":"${CLUSTER_PORT},
  "akka.tcp://application@"${CLUSTER_IP2}":"${CLUSTER_PORT2}
]
akka.remote.netty.tcp.port=${CLUSTER_PORT}
```

# Creating network
docker network create --subnet=172.20.0.0/16 mynet123

# Finding all the ipv4_address
sudo nmap -sn 192.168.1.0/24

# Docker Swarm
**NOTE** If there is exception task: non-zero exit (1)" in docker service, refer to https://github.com/moby/moby/issues/26083
1. Execute
```
docker swarm init
```
2. With docker-compose.yml created, deployed run
```
docker stack deploy -c docker-compose.yml getstartedlab
```

# Running command promp input
```
#Running cqlsh for cassandra
docker container ls
docker exec -it <containerid> cqlsh
```

# Docker visualizer
To check the status of the docker
```
docker service create \
--name viz \
--publish 8080:8080/tcp \
--constraint node.role==manager \
--mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock \
alexellis2/visualizer-arm:latest
```
