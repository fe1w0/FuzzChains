rm -rf DataSet/fuzz

cd "$(dirname "$0")"

#mvn package -DskipTests=true

./tools/instrument/run.sh

java -jar target/FuzzChains-1.0-modified.jar -c DataSet -f DataSet/targets/xyz-xzaslxr-1.0.jar -m chains -r DataSet/failures -o DataSet/fuzz -t 1s