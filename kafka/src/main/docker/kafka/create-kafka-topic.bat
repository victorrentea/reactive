docker exec -it kafka-kafka2-1 kafka-topics --zookeeper zookeeper:2181 --create --topic my-topic --partitions 1 --replication-factor 1
docker exec -it kafka-kafka2-1 kafka-topics --zookeeper zookeeper:2181 --create --topic pvs --partitions 1 --replication-factor 1
docker exec -it kafka-kafka2-1 kafka-topics --zookeeper zookeeper:2181 --create --topic pcs --partitions 1 --replication-factor 1