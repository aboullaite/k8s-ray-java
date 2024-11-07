# Fine-tuning and Serving an Open LLM with Transformers and Ray on GKE and Integrating with a Java application

This guide demonstrates how to fine-tune large language models (LLM) using the HuggingFace Transformers Library and how to use Ray to Serve the LLM on GKE

We will also see how to integrate into a Quarkus Java application

In this guide we will fine-tune a Gemma2 base model using a HuggingFace Dataset and Serve it two ways. One with the vLLM container and a second way with Ray.