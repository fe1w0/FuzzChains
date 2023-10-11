rm -rf demo.jar log.log
mvn clean package -DskipTests=true
java -jar target/instrument-1.0.jar -i ../../DataSet/targets/xyz-xzaslxr-1.0.jar -s ../../DataSet/sinks.csv -o ./demo.jar -l log.log