
## Chapter 2
1. Run the code
```
sbt clean package
```

2. ~/personal/spark/spark-1.4.0-bin-hadoop2.6/bin/spark-submit --class Chapter2 ./target/scala-2.11/spark-scala-program_2.11-0.0.1.jar ./src/main/resources/README.md ./src/main/resources/wordcounts


## Setup Raspberry
### Log into the new Raspberry Pi from your machine

* `ssh pi@192.168.1.XXX` (default password for `pi` user is `raspberry`)

### Configure RPi
   * Enter config:  `sudo raspi-config`
   * Change the hostname of the device to something like **rpi007** (under advanced options)
   * When exiting the config, choose to **reboot** so that changes take effect

### Config a spark user
A Spark cluster will need ssh access between nodes using the same username, so let's configure a `spark` user for this node.
   * add new user: `sudo adduser spark` (for simplificty, password should be same for all RPis)
   * add spark user to sudo group: `sudo adduser spark sudo`
   * `CTRL+D` to log out of SSH (we'll log in as *spark* user)
