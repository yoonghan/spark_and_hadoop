# Apache Spark

Assume created a user hduser:hadoop, from hadoop.

# Install and test Apache Spark on each Raspberry Pi

### Copy spark application to RPi
* `scp spark-x.x.x-bin-hadoop2.7.tgz spark@192.168.1.138:spark-x.x.x-bin-hadoop2.7.tgz`

### Test Spark in standalone mode
With the file transferred to the new RPi, let's log into the spark user we created earlier to set up spark.
   * `ssh hduser@RaspberryPiHadoopMaster`
   * Extract spark: `tar xvfz spark-x.x.x-bin-hadoop2.7.tgz`
   * sudo mv spark-x.x.x-bin-hadoop2.7 /opt/spark
   * sudo chown -R hduser:hadoop /opt/spark

### Add property
Configure /etc/bash.bashrc by adding on the top, repeat for all servers.
```
export SPARK_HOME=/opt/spark
export PATH=$PATH:$SPARK_HOME/sbin
export PATH=$PATH:$SPARK_HOME/bin
```

### Configure cluster
1. Login to the first computer that will run as master cluster
   * Copy conf/spark-env.sh.template as conf/spark-env.sh
   * Edit file and override the command
```
SPARK_MASTER_HOST='RaspberryPiHadoopMaster'
SPARK_MASTER_PORT=7707
```
2. Configure spark
```
mv $SPARK_HOME/conf/spark-defaults.conf.template $SPARK_HOME/conf/spark-defaults.conf
spark.master    yarn
```
3. Run the master server with
```
$SPARK_HOME/sbin/start-master.sh
```
4. Check http://localhost:8080 (or from the logs) to see the cluster status.
5. Login to slave server and execute the same command as Step 1. Then run the slave with the command
```
/sbin/start-slave.sh  spark://RaspberryPiHadoopMaster:7707
```

### Run program
**NOTE: 6066 port is rest call, you can read from console**
```
//THIS COMMAND CANNOT WORK, the reason being is that the codes will start to read from hdfs automatically if HADOOP_CONF_DIR or YARN_CONF_DIR is in the directory.
/Users/mmpkl05/personal/spark/spark-2.3.0-bin-hadoop2.7/bin/spark-submit --class Chapter2 --master spark://192.168.1.38:6066 --deploy-mode cluster --executor-memory 500m /home/hduser/spark-scala-program_2.11-0.0.1.jar /home/spark/README.md ./wordcount

spark-submit --class Chapter2 --master spark://192.168.1.38:6066 --deploy-mode cluster --executor-memory 500m /home/hduser/spark-scala-program_2.11-0.0.1.jar /smallfile.txt /wordcount4 3 spark://192.168.1.38:6066
```
1. Run the spark on the REST port (this can be check from the UI console 8080)
2. Copy the JAR files into the slave pc.
3. Copy README.md to the slave pc, make sure the path specified is the path to be found in the slave pc.

### Running on hadoop
1. Write files to hadoop
```
echo "This is a word we do"|hadoop fs -appendToFile - /dir/hadoop/hello_world.txt
hdfs dfs -text /dir/hadoop/hello_world.txt
```
2. Check hadoop dfs's interface http://<ip>:50070
3. Execute the code of writing
```
spark-submit --class Chapter2 --master spark://192.168.1.38:6066 /home/hduser/spark-scala-program_2.11-0.0.1.jar /dir/hadoop/hello_world.txt /wordcounts
```
4. If submitted from slaves, or master, the --master yarn has to be in front, make sure yarn-site.xml have set
'yarn.scheduler.maximum-allocation-mb' and/or 'yarn.nodemanager.resource.memory-mb' to 1024 above in ALL NODES.
Restart with stop-yarn.sh && start-yarn.sh
The queue is a configuration of yarn, default is the default value. It can be set in mapred-queues.xml
This job will take alot of time too as it is uploading to hadoop. To configure by uploading according to https://stackoverflow.com/questions/31254320/spark-assembly-file-uploaded-despite-spark-yarn-conf-being-set?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
Memory cannot be less than 512m
Running via yarn cluster, one does not need to copy the jar file over.
```
spark-submit --class Chapter2 \
    --master yarn \
    --deploy-mode cluster \
    --driver-memory 512m \
    --executor-memory 512m \
    --executor-cores 1 \
    --queue default \
/home/hduser/spark-scala-program_2.11-0.0.1.jar /smallfile.txt /wordcount4
```

5. You can see the task running in yarn via
http://192.168.1.38:8088/cluster

6. To make hadoop faster, upload the spark file. We can add other jars to make it faster as well.
```
cd /opt/spark
jar cv0f spark-libs.jar -C $SPARK_HOME/jars/ .
hdfs dfs -mkdir /share/
hdfs dfs -mkdir /share/lib
hdfs dfs -put spark-libs.jar /share/lib/
```
```
spark-submit --class Chapter2 \
    --master yarn \
    --deploy-mode cluster \
    --driver-memory 512m \
    --executor-memory 512m \
    --executor-cores 1 \
    --queue default \
    --conf spark.yarn.archive=hdfs:///share/lib/spark-libs.jar \
    /home/hduser/spark-scala-program_2.11-0.0.1.jar /smallfile.txt /wordcount6
```
7. It is also an alternative to add all the jars, though i had not tried with these
```
spark-submit --class Chapter2 \
    --master yarn \
    --deploy-mode cluster \
    --driver-memory 512m \
    --executor-memory 512m \
    --executor-cores 1 \
    --queue default \
    --conf spark.yarn.archive=hdfs:///share/lib/spark-libs.jar \
    --conf spark.yarn.jars=hdfs://spark-scala-program_2.11-0.0.1.jar:spark_conf5554896961452842797.zip
    /home/hduser/spark-scala-program_2.11-0.0.1.jar /smallfile.txt /wordcount6
```
8. To make sure parallelism is correctly configured
**NOTE:  Ensure the program has partition, e.g. sc.textFile(inputFile, partition)**
**NOTE: --executor-cores 1 command should be more than 1.**
```
  time spark-submit --class Chapter2 \
    --master yarn \
    --deploy-mode cluster \
    --driver-memory 512m \
    --executor-memory 512m \
    --executor-cores 2 \
    --queue default \
    --conf spark.yarn.archive=hdfs:///share/lib/spark-libs.jar \
    /home/hduser/spark-scala-program_2.11-0.0.1.jar /smallfile.txt /wordcount10 2 yarn
```
