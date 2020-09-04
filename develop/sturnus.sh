mvn clean package -DskipTests=true

docker build --build-arg JAR_FILE=sturnus-v0.1.jar -t sturnus .

docker save sturnus > sturnus.tar

docker load < sturnus.tar
