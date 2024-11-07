# Fine-tuning and Serving an Open LLM with Transformers and Ray on GKE

This guide demonstrates how to fine-tune large language models (LLM) using the HuggingFace Transformers Library and how to use Ray to Serve the LLM on GKE

In this guide we will fine-tune a Gemma2 base model using a HuggingFace Dataset and Serve it two ways. One with the vLLM container and a second way with Ray.

## Prepare your environment

1. In the Google Cloud console, launch a Cloud Shell session by clicking the Activate Cloud Shell in the Google Cloud console (top-right corner). This launches a session in the bottom pane of Google Cloud console.

2. Set the default environment variables:

```shell
gcloud config set project PROJECT_ID
export PROJECT_ID=$(gcloud config get project)
export REGION=REGION
export CLUSTER_NAME=finetuning
export HF_TOKEN=HF_TOKEN
export HF_PROFILE=HF_PROFILE
```

Replace the following values:

```shell
PROJECT_ID: Your Google Cloud project ID. You should have gotten this one from the previous steps
REGION: A region that supports the accelerator type you want to use, for example, us-central1 or us-east1 for L4 GPU.
HF_TOKEN: The Hugging Face token you generated earlier.
HF_PROFILE: The Hugging Face Profile ID that you created earlier.
```

Clone the sample code repository from GitHub:

```shell (TODO: Fix me)
git clone https://github.com/aboullaite/k8s-ray-java.git
cd infra/llm-gemma
```

# Create and configure Google Cloud Resources.

## Enabled needed APIs

```shell
gcloud services enable container.googleapis.com
gcloud services enable artifactregistry.googleapis.com
gcloud services enable cloudbuild.googleapis.com
```

## Create a GKE Cluster

```shell
gcloud container clusters create ${CLUSTER_NAME} \
  --addons=RayOperator \
  --project=${PROJECT_ID} \
  --region=${REGION} \
  --workload-pool=${PROJECT_ID}.svc.id.goog \
  --release-channel=rapid \
  --num-nodes=1
```

## Create a GKE NodePool with L4 GPUs

```shell
gcloud container node-pools create gpupool \
  --accelerator type=nvidia-l4,count=4 \
  --project=${PROJECT_ID} \
  --location=${REGION} \
  --node-locations=${REGION}-b \
  --cluster=${CLUSTER_NAME} \
  --machine-type=g2-standard-96 \
  --num-nodes=1
```

## Get Cluster Credentials

```shell
gcloud container clusters get-credentials ${CLUSTER_NAME} --location=${REGION}
```

## Create HF Secret in Kubernetes

```shell
kubectl create secret generic hf-secret \
  --from-literal=hf_api_token=${HF_TOKEN} \
  --dry-run=client -o yaml | kubectl apply -f -
```

## Create a Container Registry to store the container images
```shell
gcloud artifacts repositories create gemma \
--project=${PROJECT_ID} \
--repository-format=docker \
--location=us \
--description="Gemma Repo"
```

## Build and push the fine-tuning container image

```shell
gcloud builds submit .
export IMAGE_URL=us-docker.pkg.dev/$PROJECT_ID/gemma/finetune-gemma-gpu:1.0.0
```

## Explore and run a fine-tune Job on GKE

In this step we will run a fine-tune job on GKE using the Hugging Face Transformers library

1. Open the finetune.py source file
2. Notice the base model defined in the MODEL_NAME env variable
3. Notice the dataset used to finetune the model `b-mc2/sql-create-context`
4. Notice the name of the output model `gemma-2b-sql-finetuned`

Deploy the `finetune.yaml` file after substituting the $IMAGE_URL variable

```shell
envsubst < finetune.yaml | kubectl apply -f -
```

Wait for the pod to come up

```shell
watch kubectl get pods
```

Check the logs of the fine-tuning job

```shell
kubectl logs job.batch/finetune-job -f
```

The Job resource downloads the model data then fine-tunes the model across all 8 GPU. This can take up to 20 minutes.

nce the Job is complete, go to your Hugging Face account. A new model named $HF_PROFILE/gemma-2b-sql-finetuned appears in your Hugging Face profile.

# Serving the fine-tuned model

Now that we have a fine-tuned model we are ready to serve it. Serving means deploying the model somewhere where we can have an endpoint to invoke the model from.

In this workshop we will explore two ways to serve the model:

- Using a (vLLM container)[https://github.com/vllm-project/vllm]
- Using (Ray)[https://www.ray.io/]

## Serving with vLLM

Open the file `vllm-deployment.yaml` and take a look. We have two objects:

- A Kubernetes Deployment object which deploys the vLLM container and run a model
- A Kubernetes Service with point to the deployment and provides an endpoint on port 8000

Let's deploy this.

### Substitute the model ID in the `vllm-deployment.yaml` file

```shell
export MODEL_ID=$HF_PROFILE/gemma-2b-sql-finetuned
sed -i "s|google/gemma-2b|$MODEL_ID|g" vllm-deployment.yaml
```

### Apply the manifest

```shell
kubectl apply -f vllm-deployment.yaml
```

### Monitor the deployment

```shell
kubectl wait --for=condition=Available --timeout=700s deployment/vllm-gemma-deployment
```

### Check the logs

```shell
kubectl logs -f -l app=gemma-server
```

The Deployment resource downloads the model data. This process can take a few minutes. The output is similar to the following:

```shell
INFO 01-26 19:02:54 model_runner.py:689] Graph capturing finished in 4 secs.
INFO:     Started server process [1]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
```

### Query the model

Once the model is running you can connect to it using the kubectl port-forward command

```shell
kubectl port-forward service/llm-service 8000:8000
```

Leave the tab open and open a new one to query the model

```shell
USER_PROMPT="Question: What is the total number of attendees with age over 30 at kubecon eu? Context: CREATE TABLE attendees (name VARCHAR, age INTEGER, kubecon VARCHAR)"

curl -X POST http://localhost:8000/generate \
  -H "Content-Type: application/json" \
  -d @- <<EOF
{
    "prompt": "${USER_PROMPT}",
    "temperature": 0.1,
    "top_p": 1.0,
    "max_tokens": 24
}
EOF
```

Depending on your query, you might have to change the max_token to get a better result.

## Clean up

```shell
kubectl delete -f vllm-deployment.yaml
```
## Serving with Ray

Open the ray-service.yaml file and explore the object. Notice that instead of a Kubernetes Deployment and a Service. We are using a RayService this time

### Substitute the env variables and send the object to the cluster

```shell
export MODEL_ID=$HF_PROFILE/gemma-2b-sql-finetuned
sed -i "s|google/gemma-2b-it|$MODEL_ID|g" ray-service.yaml
kubectl apply -f ray-service.yaml
```

### Monitor the object

```shell
kubectl get rayservice gemma-2b-finetuned -o yaml
```

When the object is ready the output should look like

```shell
status:
  activeServiceStatus:
    applicationStatuses:
      llm:
        healthLastUpdateTime: "2024-06-22T02:51:52Z"
        serveDeploymentStatuses:
          VLLMDeployment:
            healthLastUpdateTime: "2024-06-22T02:51:52Z"
            status: HEALTHY
        status: RUNNING
```

### Confirm that GKE created the Service for the Ray Serve application:

```shell
kubectl get services
```

The output should look like

```shell
NAME                    TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
gemma-2b-it-serve-svc   ClusterIP   34.118.226.104   <none>        8000/TCP   45m
```

### Query the model

```shell
kubectl port-forward svc/llama-7b-serve-svc 8000:8000
```




