1. `ssh pi@192.168.1.XX`.
2. Create `adduser hduser`.
3. Enter config:  `sudo raspi-config`
  * Change the hostname of the device to **rpi001** (under advanced options)
  * When exiting the config, choose to **reboot** so that changes take effect
  | Server | Hostname |
  | 192.168.1.38 | rpi001 | **
  | 192.168.1.193 | rpi002 |
  | 192.168.1.4 | rpi003| **

  Edit /etc/hosts
  192.168.1.38  RaspberryPiHadoopMaster
  192.168.1.193  RaspberryPiHadoopSlave1
  192.168.1.4  RaspberryPiHadoopSlave2
4. Install the correct version, hadoop is relying on protobuf 2.5.0
Download https://github.com/google/protobuf/releases/download/v2.5.0/protobuf-2.5.0.tar.gz (protobuf cpp)
tar xzvf protobuf-2.5.0.tar.gz
cd protobuf-2.5.0
./configure --prefix=/usr
make
make check
sudo make install
sudo apt-get -y install protobuf-compiler

Install software
sudo apt-get install software-properties-common

Install maven
sudo apt-get update
sudo apt-get -y install maven

Install native library
sudo apt-get -y install build-essential autoconf automake libtool cmake zlib1g-dev pkg-config libssl-dev

Download http://hadoop.apache.org/releases.html (hadoop src), this process takes very long for mvn.
Fix patching for ARM https://issues.apache.org/jira/browse/HADOOP-9320
**TODO THIS**
cd hadoop-src/hadoop-common-project/hadoop-common/
vi HadoopCommon.cmake
```
remove code from # Determine float ABI of JVM on ARM.
to endif() of CMAKE_SYSTEM_PROCESSOR MATCHES "^arm"
```
vi HadoopJNI.cmake
```
add code that was removed from HadoopCommon.cmake here, append into the last line!
```
One can refer to GIT difference in https://patch-diff.githubusercontent.com/raw/apache/hadoop/pull/224.patch
Basically the change is telling this:
 1. cd hadoop-X-src/hadoop-common-project/hadoop-common/
 2. Edit HadoopCommon.cmake (vi/emac, your preferred editor)
 3. CUT the whole IF block of # Determine float ABI of JVM on ARM.
 4. Save
 5. Edit HadoopJNI.cmake
 6. Add the line removed from HadoopCommon.cmake and append into the last line of HadoopJNI.cmake
 7. Save and let it recompile


In hadoop-X.X.X-src/pom.xml, change to <additionalparam>-Xdoclint:none</additionalparam>
export MAVEN_OPTS="-Xmx3000m"
export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=350m"
nohup sudo mvn package -Pdist,native -DskipTests -Dtar
cd hadoop-dist/target/
sudo cp -R hadoop-2.7.2 /opt/hadoop
sudo chown -R hduser:hadoop /opt/hadoop/
su hduser
cd /opt/hadoop/bin
hadoop checknative -a

41. (NEW) Execute:
wget https://archive.apache.org/dist/hadoop/core/hadoop-2.7.1/hadoop-2.7.1.tar.gz
wget http://files.minibig.io/minipublic/hadoop-2.7.1-native-libs-armv7l.tar.gz
Untar both with
```
tar xvfz
```
sudo cp -R hadoop-2.7.1 /opt/hadoop
mv hadoop-native/ /opt/hadoop/native
sudo chown -R hduser.hadoop /opt/hadoop/


5. Put keygen
#su hduser of RaspberryPiHadoopSlave1
 $ ssh-keygen -t rsa
 $ ssh-copy-id -i ~/.ssh/id_rsa.pub hduser@RaspberryPiHadoopMaster
 $ ssh-copy-id -i ~/.ssh/id_rsa.pub hduser@RaspberryPiHadoopSlave1
 $ ssh-copy-id -i ~/.ssh/id_rsa.pub hduser@RaspberryPiHadoopSlave2
6. Configure the followings
In /etc/bash.bashrc, add to top of file:
export JAVA_HOME=$(readlink -f /usr/bin/java|sed "s:jre/bin/java::")
export HADOOP_INSTALL=/opt/hadoop
export PATH=$PATH:$HADOOP_INSTALL/bin
export PATH=$PATH:$HADOOP_INSTALL/sbin
export HADOOP_MAPRED_HOME=$HADOOP_INSTALL
export HADOOP_COMMON_HOME=$HADOOP_INSTALL
export HADOOP_HDFS_HOME=$HADOOP_INSTALL
export YARN_HOME=$HADOOP_INSTALL
export HADOOP_HOME=$HADOOP_INSTALL
export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_INSTALL/native/
export HADOOP_OPTS="-Djava.library.path=$HADOOP_INSTALL/native/ -Djava.net.preferIPv4Stack=true"
export SPARK_HOME="/opt/spark"
export HADOOP_CONF_DIR="/opt/hadoop/etc/hadoop/"
export LD_LIBRARY_PATH="$HADOOP_INSTALL/native/"
##dfs.namenode.servicerpc-address or dfs.namenode.rpc-address is not configured, error if conf_dir is not configured properly

hadoop_env.sh (/opt/hadoop/etc/hadoop/)
export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt
export HADOOP_OPTS="$HADOOP_OPTS -Djava.library.path=$HADOOP_INSTALL/native -Djava.net.preferIPv4Stack=true"


core-site.xml
<configuration>
  <property>
    <name>hadoop.tmp.dir</name>
    <value>/hdfs/tmp</value>
  </property>
  <property>
    <name>fs.defaultFS</name>
    <value>hdfs://RaspberryPiHadoopMaster:54310</value>
  </property>
</configuration>

yarn-site.xml
<configuration>
    <property>
        <name>yarn.resourcemanager.resource-tracker.address</name>
        <value>RaspberryPiHadoopMaster:8025</value>
    </property>
    <property>
        <name>yarn.resourcemanager.scheduler.address</name>
        <value>RaspberryPiHadoopMaster:8035</value>
    </property>
    <property>
        <name>yarn.resourcemanager.address</name>
        <value>RaspberryPiHadoopMaster:8050</value>
    </property>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
    <property>
        <name>yarn.nodemanager.resource.cpu-vcores</name>
        <value>4</value>
    </property>
    <property>
        <name>yarn.nodemanager.resource.memory-mb</name>
        <value>1024</value>
    </property>
    <property>
        <name>yarn.scheduler.minimum-allocation-mb</name>
        <value>64</value>
    </property>
    <property>
        <name>yarn.scheduler.maximum-allocation-mb</name>
        <value>1024</value>
    </property>
    <property>
        <name>yarn.scheduler.minimum-allocation-vcores</name>
        <value>1</value>
    </property>
    <property>
        <name>yarn.scheduler.maximum-allocation-vcores</name>
        <value>4</value>
    </property>
    <property>
        <name>yarn.nodemanager.vmem-check-enabled</name>
        <value>true</value>
    </property>
    <property>
        <name>yarn.nodemanager.pmem-check-enabled</name>
        <value>true</value>
    </property>
    <property>
        <name>yarn.nodemanager.vmem-pmem-ratio</name>
        <value>2.1</value>
    </property>
</configuration>

mapred-site.xml
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
    <property>
        <name>mapreduce.map.memory.mb</name>
        <value>256</value>
    </property>
    <property>
        <name>mapreduce.map.java.opts</name>
        <value>-Xmx204m</value>
    </property>
    <property>
        <name>mapreduce.map.cpu.vcores</name>
        <value>2</value>
    </property>
    <property>
        <name>mapreduce.reduce.memory.mb</name>
        <value>128</value>
    </property>
    <property>
        <name>mapreduce.reduce.java.opts</name>
        <value>-Xmx102m</value>
    </property>
    <property>
        <name>mapreduce.reduce.cpu.vcores</name>
        <value>2</value>
    </property>
    <property>
        <name>yarn.app.mapreduce.am.resource.mb</name>
        <value>128</value>
    </property>
    <property>
        <name>yarn.app.mapreduce.am.command-opts</name>
        <value>-Xmx102m</value>
    </property>
    <property>
        <name>yarn.app.mapreduce.am.resource.cpu-vcores</name>
        <value>1</value>
    </property>
    <property>
        <name>mapreduce.job.maps</name>
        <value>4</value>
    </property>
    <property>
        <name>mapreduce.job.reduces</name>
        <value>4</value>
    </property>
</configuration>

7. Clear cluster
sudo mkdir -p /hdfs/tmp
sudo chown hduser:hadoop /hdfs/tmp
sudo chmod 750 /hdfs/tmp
hdfs namenode -format

8. Start hadoop, there are issue though esp asking for SecondaryNameNode and /home/hduser/hadoop-2.7.5/lib/native/libhadoop.so.1.0.0
start-dfs.sh
start-yarn.sh

9. Check everything is running via
jps

10. If Jps run check if all resources are running
1920 ResourceManager
2066 Jps
1480 NameNode
1578 DataNode
2013 NodeManager
1775 SecondaryNameNode


11. Server journal
http://RaspberryPiHadoopMaster:8088/cluster - main
http://RaspberryPiHadoopMaster:50090/status.html
http://RaspberryPiHaddopMaster:50070/ - namenode # It it in hdfs-site.xml
http://RaspberryPiHadoopMaster:50075/ - datanode information

12. Stop all
stop-dfs.sh
stop-yarn.sh

13. Create /opt/hadoop/slaves
RaspberryPiHadoopSlave1
RaspberryPiHadoopSlave2
Create /opt/hadoop/master
RaspberryPiHadoopMaster

14. Synch codes
rsync -avxP /opt/hadoop/ hduser@RaspberryPiHadoopSlave1:/opt/hadoop/
rsync -avxP /opt/hadoop/ hduser@RaspberryPiHadoopSlave2:/opt/hadoop/

15. Make sure all environments of bashrc.bash are copied over to all nodes.
and
**When access RaspberryPiHadoopMaster:50070 and the node is 0, make sure ALL NODES core-site is pointing the correct fs.defaultFS**

core-site.xml
<configuration>
  <property>
    <name>hadoop.tmp.dir</name>
    <value>/hdfs/tmp</value>
  </property>
  <property>
    <name>fs.defaultFS</name>
    <value>hdfs://RaspberryPiHadoopMaster:54310</value>
  </property>
</configuration>

hdfs-site.xml
<configuration>
  <property>
    <name>dfs.replication</name>
    <value>3</value>
  </property>
  <property>
    <name>dfs.blocksize</name>
    <value>5242880</value>
  </property>
  <property>
      <name>dfs.namenode.http-address</name>
      <value>RaspberryPiHadoopMaster:50070</value>
  </property>
</configuration>

mapred-site.xml
<configuration>
    <property>
        <name>mapreduce.job.tracker</name>
        <value>RaspberryPiHadoopMaster:5431</value>
    </property>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
    <property>
        <name>mapreduce.map.memory.mb</name>
        <value>256</value>
    </property>
    <property>
        <name>mapreduce.map.java.opts</name>
        <value>-Xmx204m</value>
    </property>
    <property>
        <name>mapreduce.map.cpu.vcores</name>
        <value>2</value>
    </property>
    <property>
        <name>mapreduce.reduce.memory.mb</name>
        <value>128</value>
    </property>
    <property>
        <name>mapreduce.reduce.java.opts</name>
        <value>-Xmx102m</value>
    </property>
    <property>
        <name>mapreduce.reduce.cpu.vcores</name>
        <value>2</value>
    </property>
    <property>
        <name>yarn.app.mapreduce.am.resource.mb</name>
        <value>128</value>
    </property>
    <property>
        <name>yarn.app.mapreduce.am.command-opts</name>
        <value>-Xmx102m</value>
    </property>
    <property>
        <name>yarn.app.mapreduce.am.resource.cpu-vcores</name>
        <value>1</value>
    </property>
    <property>
        <name>mapreduce.job.maps</name>
        <value>4</value>
    </property>
    <property>
        <name>mapreduce.job.reduces</name>
        <value>4</value>
    </property>
</configuration>


16. In both data node servers Create
sudo mkdir hadoop
sudo chown -R hduser:hadoop hadoop/
sudo mkdir -p /hdfs/tmp
sudo chown hduser:hadoop /hdfs/tmp
sudo chmod 750 /hdfs/tmp

17. Start with
start-dfs.sh
start-yarn.sh

//Make sure these are started
1413 NodeManager
838 NameNode
1451 Jps
1085 SecondaryNameNode
942 DataNode
1311 ResourceManager

18. Download
wget http://repo1.maven.org/maven2/org/apache/hadoop/hadoop-mapreduce-examples/2.7.1/hadoop-mapreduce-examples-2.7.1.jar
hadoop fs -put mediumfile.txt /mediumfile.txt
hadoop fs -put smallfile.txt /smallfile.txt
time hadoop jar hadoop-mapreduce-examples-2.7.1.jar wordcount /smallfile.txt /smallfile-out
time hadoop jar hadoop-mapreduce-examples-2.7.1.jar wordcount /mediumfile.txt /mediumfile-out
**If encountered the error "The number of live datanodes 2 has reached the minimum number 0. Safe mode will be turned off automatically once the thresholds have been reached." - this means the node is no longer synch the best way is to do a rm -rf /hdfs/tmp/* ON all servers. Then re-run hdfs namenode -format on RaspberryPiHadoopMaster ***


19. Monitor with
hdfs dfsadmin -report
//Make sure the data nodes alive. It should display something like
```
Configured Capacity: 30443814912 (28.35 GB)
Present Capacity: 16284471296 (15.17 GB)
DFS Remaining: 16281038848 (15.16 GB)
DFS Used: 3432448 (3.27 MB)
DFS Used%: 0.02%
Under replicated blocks: 8
Blocks with corrupt replicas: 0
Missing blocks: 0
Missing blocks (with replication factor 1): 0

-------------------------------------------------
Live datanodes (2):

Name: 192.168.1.193:50010 (RaspberryPiHadoopSlave2)
Hostname: rpi002
Decommission Status : Normal
Configured Capacity: 15279587328 (14.23 GB)
DFS Used: 1716224 (1.64 MB)
Non DFS Used: 6779387904 (6.31 GB)
DFS Remaining: 8498483200 (7.91 GB)
DFS Used%: 0.01%
DFS Remaining%: 55.62%
Configured Cache Capacity: 0 (0 B)
Cache Used: 0 (0 B)
Cache Remaining: 0 (0 B)
Cache Used%: 100.00%
Cache Remaining%: 0.00%
Xceivers: 1
Last contact: Sun Apr 08 14:29:24 UTC 2018


Name: 192.168.1.4:50010 (RaspberryPiHadoopSlave1)
Hostname: rpi003
Decommission Status : Normal
Configured Capacity: 15164227584 (14.12 GB)
DFS Used: 1716224 (1.64 MB)
Non DFS Used: 7379955712 (6.87 GB)
DFS Remaining: 7782555648 (7.25 GB)
DFS Used%: 0.01%
DFS Remaining%: 51.32%
Configured Cache Capacity: 0 (0 B)
Cache Used: 0 (0 B)
Cache Remaining: 0 (0 B)
Cache Used%: 100.00%
Cache Remaining%: 0.00%
Xceivers: 1
Last contact: Sun Apr 08 14:29:23 UTC 2018
```

20. You can see the task running in yarn via
```
mr-jobhistory-daemon.sh --config $HADOOP_CONF_DIR start historyserver
```
Then wait 2 minutes before using browser to query http://192.168.1.38:19888

SparkConf
1. mv $SPARK_HOME/conf/spark-defaults.conf.template $SPARK_HOME/conf/spark-defaults.conf
2. spark.master    yarn
