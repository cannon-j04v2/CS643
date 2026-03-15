# Execution Steps for the CS643 Project1 Codebase

This repository implements two apps for EC2 deployment:
- `AppA` on node_1
- `AppB` on node_2

## 1) Prerequisites
- Java 17 installed on both EC2 nodes.
- Maven installed on both EC2 nodes.
- AWS credentials configured in `~/.aws/credentials` (or an EC2 IAM role).
- AWS region configured in `~/.aws/config` (or instance metadata). Optional override: `AWS_REGION` env var.
- Existing FIFO SQS queue URL.
- S3 bucket `cs643-njit-project1` with images `1.jpg` through `10.jpg`.

## 2) Build on each node
```bash
mvn clean package
```

Build output includes runnable fat jar:
- `target/project1-1.0-SNAPSHOT-all.jar`

## 3) Set required environment variable on each node
```bash
export SQS_QUEUE_URL=<FMI_5>
```

Optional region override:
```bash
export AWS_REGION=<FMI_4>
```

## 4) Start AppB on node_2 first (or AppA first; either order is valid)
```bash
java -cp target/project1-1.0-SNAPSHOT-all.jar com.njit.project1.appb.AppB
```

## 5) Start AppA on node_1
```bash
java -cp target/project1-1.0-SNAPSHOT-all.jar com.njit.project1.appa.AppA
```

## 6) Expected behavior
- AppA scans `1.jpg` to `10.jpg`, detects `Car` labels with confidence > 80, and sends matching keys to SQS.
- AppA sends `-1` termination message after processing all images.
- AppB long-polls SQS, downloads images from S3, runs text detection, and writes high-confidence text (>80) to `output.txt`.
- AppB exits cleanly after receiving `-1`.

## 7) Credential and config examples
`~/.aws/credentials`
```ini
[default]
aws_access_key_id=<FMI_1>
aws_secret_access_key=<FMI_2>
aws_session_token=<FMI_3>
```

`~/.aws/config`
```ini
[default]
region=<FMI_4>
output=json
```
