# This is the instruction for akka in lagom

1. The akka has to firstly be configured to have the correct netty host. Check in application.conf
```
akka.cluster.seed-nodes = [
  "akka.tcp://application@"${HOST_IP}":"${HOST_PORT},
  "akka.tcp://application@"${CLUSTER_IP2}":"${CLUSTER_PORT2}
]
akka {
  remote {
    netty.tcp {
      hostname = ${HOST_IP}      # external (logical) hostname
      port = ${HOST_PORT}                   # external (logical) port

      bind-hostname = ${CLUSTER_IP} # internal (bind) hostname
      bind-port = ${HOST_PORT}              # internal (bind) port
    }
 }
}
```
2. The bind hostname must be unique.
3. Execute first server
```
docker run -p9000:9000 -p2551:2551 -e CLUSTER_IP=172.17.0.3 -e HOST_PORT=2551 -e CLUSTER_IP2=192.168.1.244 -e CLUSTER_PORT2=2552 -e HOST_IP=192.168.1.244 justice-league-impl:1.0-SNAPSHOT
```
4. Execute second server
```
docker run -p2552:2552 -e CLUSTER_IP=172.17.0.4 -e HOST_PORT=2552 -e CLUSTER_IP2=192.168.1.244 -e CLUSTER_PORT2=2551 -e HOST_IP=192.168.1.244 justice-league-impl:1.0-SNAPSHOT
```
