## start flow
- minikube start
- minikube addons enable ingress
- minikube addons enable ingress-dns
- kubectl apply -f ./charts/zookeeper-deployment.yaml
- kubectl apply -f ./charts/kafka-deployment.yaml
- kubectl apply -f ./charts/schema-registry-deployment.yaml
- kubectl apply -f ./charts/walker-backend-service-deployment.yaml

- kubectl apply -f ./charts/zookeeper-deployment.yaml && kubectl apply -f ./charts/kafka-deployment.yaml && kubectl apply -f ./charts/kafka-deployment.yaml && kubectl apply -f ./charts/schema-registry-deployment.yaml && kubectl apply -f ./charts/walker-backend-service-deployment.yaml && kubectl apply -f  ./charts/ingress-deployment.yaml


## alternative way
- kubectl expose deployment walker-backend-service --type=LoadBalancer --port=9000
- OR
- kubectl expose deployment walker-backend-service --type=NodePort --port=9000

## For testing
- minikube dashboard
- kubectl port-forward deployments/kafka  9093:9093
- kubectl port-forward deployments/schema-registry 8081:8081
- kubectl port-forward deployments/walker-backend-service 9000:9000


## Kafka
kubectl exec -it deployments/kafka  -- kafka-topics.sh --bootstrap-server kafka-service:9092 --topic device-data.location --describe
kubectl exec -it deployments/kafka  -- kafka-topics.sh --bootstrap-server kafka-service:9092 --topic device-data.location --delete

kubectl exec -it deployments/kafka -- kafka-topics.sh --bootstrap-server kafka-service:9092 --topic device-data.location --create --replication-factor 1 --partitions 2


cd loadtests && bzt scenario.yml