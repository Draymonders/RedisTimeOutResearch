mvn clean package -DskipTests=true

docker build --build-arg JAR_FILE=sturnus-v0.1.jar -t sturnus .

docker save sturnus > sturnus.tar

scp ./sturnus.tar root@10.40.58.64:/root/yubing/
# 1

# 停掉 sturnus, 删除服务

docker load < sturnus.tar
