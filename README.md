# CS643 Project1 (AppA + AppB with S3, SQS FIFO, Rekognition)

## Project Overview
This project contains two Java applications for an AWS class assignment:

- **AppA (node_1):**
  1. Iterates through `1.jpg` to `10.jpg` in S3 bucket `cs643-njit-project1`.
  2. Calls Rekognition `DetectLabels`.
  3. Checks whether a label named `Car` exists with confidence `> 80`.
  4. Sends qualifying image keys to SQS FIFO queue.
  5. Sends final termination message `-1`.

- **AppB (node_2):**
  1. Continuously long-polls SQS.
  2. If message body is `-1`, exits cleanly.
  3. Otherwise downloads that image from S3.
  4. Calls Rekognition `DetectText`.
  5. Keeps text detections with confidence `> 80`.
  6. Appends results to `output.txt`.

The design works whether AppA starts first or AppB starts first.

## Architecture Summary
- Java 17
- Maven
- AWS SDK for Java v2
- Shared helper classes in `com.njit.project1.common`
- AWS credentials resolved automatically from `~/.aws/credentials` (or EC2 role)
- AWS region resolved automatically from `~/.aws/config` (or EC2 metadata)
- Optional `AWS_REGION` env var override
- Required `SQS_QUEUE_URL` env var
- SQS FIFO send uses fixed message group id: `project1`

## Repository Structure
```text
.
├── pom.xml
├── README.md
├── .gitignore
└── src/main/java/com/njit/project1/
    ├── appa/
    │   └── AppA.java
    ├── appb/
    │   └── AppB.java
    └── common/
        ├── AwsClients.java
        ├── Config.java
        ├── FileHelper.java
        ├── RekognitionHelper.java
        ├── S3Helper.java
        └── SqsHelper.java
```

## Prerequisites
On both EC2 instances:
1. Install Java 17.
2. Install Maven.
3. Configure AWS credentials and config files.
4. Ensure SQS FIFO queue already exists and has content-based deduplication enabled.
5. Ensure bucket `cs643-njit-project1` contains `1.jpg` through `10.jpg`.

## AWS Credentials and Config
AWS SDK v2 uses the default provider chains. You do **not** need to hardcode credentials or region in Java code.

Example `~/.aws/credentials`:
```ini
[default]
aws_access_key_id=<FMI_1>
aws_secret_access_key=<FMI_2>
aws_session_token=<FMI_3>
```

Example `~/.aws/config`:
```ini
[default]
region=<FMI_4>
output=json
```

## Environment Variables
`SQS_QUEUE_URL` is required.

Optional region override:
```bash
export AWS_REGION=<FMI_4>
```

Required queue URL:
```bash
export SQS_QUEUE_URL=<FMI_5>
```

## Build
From the repository root:
```bash
mvn clean package
```

This produces a runnable shaded jar:
- `target/project1-1.0-SNAPSHOT-all.jar`

## Run Instructions
### 1) Recommended (region from `~/.aws/config`)
Run AppB on node_2:
```bash
mvn clean package
export SQS_QUEUE_URL=<FMI_5>
java -cp target/project1-1.0-SNAPSHOT-all.jar com.njit.project1.appb.AppB
```

Run AppA on node_1:
```bash
mvn clean package
export SQS_QUEUE_URL=<FMI_5>
java -cp target/project1-1.0-SNAPSHOT-all.jar com.njit.project1.appa.AppA
```

### 2) Optional (manual env region override)
Run AppB on node_2:
```bash
mvn clean package
export AWS_REGION=<FMI_4>
export SQS_QUEUE_URL=<FMI_5>
java -cp target/project1-1.0-SNAPSHOT-all.jar com.njit.project1.appb.AppB
```

Run AppA on node_1:
```bash
mvn clean package
export AWS_REGION=<FMI_4>
export SQS_QUEUE_URL=<FMI_5>
java -cp target/project1-1.0-SNAPSHOT-all.jar com.njit.project1.appa.AppA
```

## FIFO Queue Notes
- Use an **SQS FIFO queue** (URL should end with `.fifo`).
- App messages use message group id `project1`.
- Queue creation is not handled by this code.
- Assume the queue already has content-based deduplication enabled.

## output.txt Notes
- File is created automatically if missing.
- Written by AppB in this format:
  - `3.jpg : ABC123, PARKING, LOT 7`
- A line is appended only when high-confidence text is detected.

## Troubleshooting
- **Missing env var error:** Ensure `SQS_QUEUE_URL` is exported before running AppA/AppB.
- **Credentials errors:** Verify `~/.aws/credentials` is set for the executing user or attach an EC2 IAM role.
- **Region errors:** Verify `~/.aws/config` has a default region, or export `AWS_REGION=<FMI_4>`.
- **No messages in AppB:** Confirm AppA is running and queue URL is correct.
- **No output lines:** Could be no car detections from AppA or no high-confidence text from AppB.
