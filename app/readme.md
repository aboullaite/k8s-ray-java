
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
$ kubectl port-forward svc/gemma-2b-it-serve-svc 8000:8000
```
Head to the `http://localhost:8080` to test the app.

# Deploying the app on GKE

## Setup gcloud CLI credential helper
The gcloud CLI credential helper provides secure, short-lived access to your project resources. It configures Docker to authenticate to Artifact Registry hosts in any environment where the Google Cloud CLI is installed. 

1. To authenticate to Artifact Registry:

Sign in to gcloud CLI as the user that will run Docker commands. 

```bash
$ gcloud auth login
```

Then run the following command to configure Docker to use the gcloud CLI as a credential helper:

```bash
$ gcloud auth configure-docker $HOSTNAME-LIST
```
Where `$HOSTNAME-LIST` is a comma-separated list of repository hostnames to add to the credential helper configuration.

For example, to add the regions us-west1 and asia-northeast1, run the command:

```bash
$ gcloud auth configure-docker europe-west4-docker.pkg.dev
```
The specified hostnames are added to the credential helper configuration. You can add other hostnames to the configuration later by running the command again.

To view a list of supported repository locations, run the command:
```bash
$ gcloud artifacts locations list
```

The command displays the `credHelpers` section of your current Docker configuration and the updated configuration after adding the specified hostnames.

To accept the configuration changes, enter y.

Your credentials are saved in your user home directory.

- Linux: `$HOME/.docker/config.json`
- Windows: `%USERPROFILE%/.docker/config.json`
Docker requires credential helpers to be in the system `PATH`. Ensure that the `gcloud` command is in the system `PATH`.

## Generating and pushing Docker image to registry
We will use quarkus container image to generate and push our Container image to Google Artifact registry. Quarkus container image extension uses JIB under the hood to build and push the image to the registry. 
To build and push the image, run the following command:
```bash
$ mvn clean verify -Dquarkus.container-image.push=true
```

Then, apply the Kubernetes manifest:
```bash
$ kubectl apply -f quarkus-langchain4j.yaml
```
The final step is to test the Deployment. You'd need to get the external IP address of the Quarkus LangChain4j service:
```bash
$ kubectl get service quarkus-langchain4j
```
Access the application in your web browser using the external IP address.
