
# Deploying Quarkus LangChain4j App with Ray on GKE
This section will walk you through the process of deploying a Quarkus LangChain4j application, consuming Ray endpoint on Google Kubernetes Engine (GKE).
# Testing the app locally
In the app folder, you will find a quarkus project that you can run locally. To run the app, you need to have Java 21 or later, maven 3.9.9 installed on your machine. You can run the app using the following command:
```bash
$ mvn compile quarkus:dev
```
The application is expecting Ray (vLLM) endpoint to be running on `http://localhost:8000`. You can change the endpoint in the `application.properties` file.
In this first step, we are going to port forward the Ray endpoint to our local machine. To do that, run the following command:
```bash
$ kubectl port-forward svc/gemma-2b-finetuned-serve-svc 8000:8000
```
Use the webpreview button (top right corner in the cloud shell) to open `http://localhost:8080` to test the app. You will need to change the port to 8080

# Deploying the app on GKE

## Setup gcloud CLI credential helper
The gcloud CLI credential helper provides secure, short-lived access to your project resources. It configures Docker to authenticate to Artifact Registry hosts in any environment where the Google Cloud CLI is installed. 

```bash
$ gcloud auth configure-docker us-docker.pkg.dev
```

To accept the configuration changes, enter y.

Your credentials are saved in your user home directory.

- Linux: `$HOME/.docker/config.json`
- Windows: `%USERPROFILE%/.docker/config.json`
Docker requires credential helpers to be in the system `PATH`. Ensure that the `gcloud` command is in the system `PATH`.

## Generating and pushing Docker image to registry
We will use quarkus container image to generate and push our Container image to Google Artifact registry. Quarkus container image extension uses JIB under the hood to build and push the image to the registry. 
To build and push the image, run the following command:

Open the `application.properties` file and replace `$PROJECT_ID` with your project id.

```bash
$ mvn clean verify -Dquarkus.container-image.push=true
```
Open the `k8s/java-ray-k8s.yaml` file and replace `$PROJECT_ID` with your project id.

Then, apply the Kubernetes manifest:
```bash
$ kubectl apply -f quarkus-langchain4j.yaml
```
The final step is to test the Deployment. You'd need to get the external IP address of the Quarkus LangChain4j service:
```bash
$ kubectl get service quarkus-langchain4j
```
Access the application in your web browser using the external IP address.
