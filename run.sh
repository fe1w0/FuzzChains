mvn clean package -DskipTests=true
rm -rf DataSet/fuzz
java -jar target/FuzzChains-1.0-modified.jar -c DataSet -f DataSet/targets/xyz-xzaslxr-1.0.jar -m chains -r DataSet/failures -o DataSet/fuzz -t 3s