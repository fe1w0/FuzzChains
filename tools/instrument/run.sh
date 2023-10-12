rm -rf demo.jar log.log
mvn package -DskipTests=true
java -jar target/instrument-1.0.jar -i ../../DataSet/targets/xyz-xzaslxr-1.0.jar -s ../../DataSet/sinks.csv -o ../../DataSet/targets/xyz-xzaslxr-1.0.jar -l log.log