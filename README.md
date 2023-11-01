<h1 align="center">🐳 Spring Boot dockerized, deployment on Kubernetes and EKS 🐳</h1>


## 💻 Compile and run

- Compile with `mvn clean install`

- Run in the IDE `DemoApplication` class
 
- Run outside the IDE `java -jar ./target/springboot-dockerized-0.0.1-SNAPSHOT.jar`


## 💻 Docker image

##### 💻 Create Docker image with JIB

##### Make sure you can connect to the Docker registry

- Login to the docker registry, by default we login to docker hub (registry.hub.docker.com), note that this registry is defined in the file `pom.xml`:

```
docker login
```

##### Create and push the Docker image

- Build and push the image, run the following in the project root, this will crate and push the Docker image to the Docker repository since we are using `jib-maven-plugin` the Java Image Builder:

```
mvn package -P docker
```

- The image is pushed to this url [https://hub.docker.com/repository/docker/nbodev/springboot-dockerized](https://hub.docker.com/repository/docker/nbodev/springboot-dockerized)


##### Check the Docker image

- Pull the Docker image:

```
docker image pull nbodev/springboot-dockerized
```
  
- Run a Docker container for testing purpose, we validate that there is no error when we run the image:

```
docker run -p 9090:9090 --rm nbodev/springboot-dockerized
```

- Go to the following url: [http://localhost:9090](http://localhost:9090)

##### 💻 Create Docker image with Dockerfile

##### Make sure you can connect to the Docker registry

- Login to the docker registry, by default we login to docker hub (registry.hub.docker.com), note that this registry is defined in the file `pom.xml`:

```
docker login
```

##### Create and push the Docker image

- Build the image, run this the in the project root folder where you see the `Dockerfile`:

```
docker build -t nbodev/springboot-dockerized .
```

- Push the image:

```
docker push nbodev/springboot-dockerized
```

##### Check the Docker image

- Run a Docker container for testing purpose, we validate that there is no error when we run the image:

```
docker run -p 9090:9090 --rm nbodev/springboot-dockerized
```

- Go to the following url: [http://localhost:9090](http://localhost:9090)


##### 💻 Build a Docker image for a specific platform

- If you are building the Docker image from a MacOS computer, you will face this issue in case you want to deploy your image to AWS later.

- You will see this error in your pod logs `exec format error`.

- In order to avoid such issue, make sure you build the image for a specific platform, the platform I choose here (`linux/arm64`) is the one of the AWS node of my future cluster.

- Run the command `docker buildx ls` so then you can see the supported platforms, then let's say you build the image for the `linux/arm64` platform, for that run:

```
docker buildx build --platform linux/arm64 -t nbodev/springboot-dockerized .
```

- Then push your image:

```
docker push nbodev/springboot-dockerized
```

### 💻 Kubernetes

- Now that our image looks valid let's move to a deployment with kubernetes.


##### The YAML file

- The file content `springboot-k8s.yml`:

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-app-deployment
  namespace: playground
  labels:
    app: springboot-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: springboot-app
  template:
    metadata:
      labels:
        app: springboot-app
    spec:
      containers:
      - name: springboot-app
        image: nbodev/springboot-dockerized
        ports:
        - containerPort: 9090
---
apiVersion: v1
kind: Service
metadata:
  name: springboot-app-svc
  namespace: playground
spec:
  type: LoadBalancer
  selector:
    app: springboot-app
  ports:
    - protocol: TCP
      port: 8888
      targetPort: 9090
```

##### Prerequisites

- Create the namespace:

```
kubectl create namespace playground
```

##### Deploy the pod and the service

- Use the YAML file for this:

```
kubectl apply -f <path_to>/springboot-k8s.yml
```

- connect to [http://localhost:8888](http://localhost:8888)

##### Delete everything

- Run the following so you delete the service and the deployment:

```
kubectl delete deploy springboot-app-deployment -n playground
kubectl delete svc springboot-app-svc -n playground
```

### 💻 EKS

##### Prerequisites

- Make sure you correctly setup the configuration and credential file settings for AWS, see the links below and install `eksctl`.

##### Setup your cluster

- Run the following so you will create a cluster with name `nbodev-eks-test-cluster` having one node of type `r6g.large` located in the `ap-southeast-1` region, this will take a while:

```
eksctl create cluster --name nbodev-eks-test-cluster --region ap-southeast-1 --nodegroup-name my-nodes --node-type r6g.large --nodes 1
```

##### Change the Kubernetes context to the EKS cluster

- Then switch the kubernetes context to the newly created one:

```
kubectl config use-context nbo@nbodev-eks-test-cluster.ap-southeast-1.eksctl.io
```

- Run the following in order to get all the existing contexts:

```
kubectl config get-contexts
```

##### Deploy the pod and the service

- Run:

```
kubectl apply -f <path_to>/springboot-k8s.yml
```

##### Test that all works fine

- Get the deployed service on EKS:

```
kubectl get svc -n playground
```

- The command above returns:

```
NAME                 TYPE           CLUSTER-IP       EXTERNAL-IP                                                                    PORT(S)          AGE
springboot-app-svc   LoadBalancer   10.100.174.197   a5a4417f7748642b5bebf6846XXXXXXXXX.ap-southeast-1.elb.amazonaws.com   8888:31322/TCP   5s
```

- Visit: [http://a5a4417f7748642b5bebf6846XXXXXXXXX.ap-southeast-1.elb.amazonaws.com:8888](http://a5a4417f7748642b5bebf6846XXXXXXXXX.ap-southeast-1.elb.amazonaws.com:8888), you will see the result of the Rest service, the EXTERNAL-IP could take a while to become public.


##### Change the nodegroup (optional)

- You can delete the existing `nodegroup` without deleting the whole cluster, in short you delete the workers and you provision a new ones:

```
eksctl delete nodegroup --cluster=nbodev-eks-test-cluster --name=my-nodes
```

- Then provision the new one:

```
eksctl create nodegroup --config-file=<path_to>/my-cluster.yaml
```

- See below a sample file where we increase the number of workers to 2 and use a larger instance type:

```
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: nbodev-eks-test-cluster
  region: ap-southeast-1

managedNodeGroups:
  - name: my-new-nodes
    labels: { role: workers }
    instanceType: r6g.2xlarge
    desiredCapacity: 2
    volumeSize: 80
    privateNetworking: true
```

##### Delete all the resources

- Delete the cluster, this will delete the related `nodegroup`:

```
eksctl delete cluster --name=nbodev-eks-test-cluster
```

### 🔗 Links

Spring Boot
- [Setting up a Spring Boot project](https://start.spring.io/)

Docker and Kubernetes
- [Install Docker and Kubernetes](https://www.docker.com/products/docker-desktop/)

Docker multi platform image build
- [https://www.docker.com/blog/multi-platform-docker-builds/](https://www.docker.com/blog/multi-platform-docker-builds/)
- [https://blog.jaimyn.dev/how-to-build-multi-architecture-docker-images-on-an-m1-mac/](https://blog.jaimyn.dev/how-to-build-multi-architecture-docker-images-on-an-m1-mac/)

AWS
- [Configuration and credential file settings](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html)
- [Installing or updating the latest version of the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
- [Instance Types](https://aws.amazon.com/ec2/instance-types/)

eksctl
- [Installing or upgrading eksctl](https://docs.aws.amazon.com/eks/latest/userguide/eksctl.html)
- [https://www.eksworkshop.com/](https://www.eksworkshop.com/)
- [https://eksctl.io/](https://eksctl.io/)






