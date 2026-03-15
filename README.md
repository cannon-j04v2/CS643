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

## Cloud Infrastructure and Runtime Setup

This project uses:
- **2 Amazon EC2 instances**
- **1 Amazon SQS FIFO queue**
- **AWS Rekognition**
- **A public S3 bucket**: `cs643-njit-project1`

### 1. Create the SQS Queue
Create an **SQS FIFO queue**.

Recommended settings:
- **Queue type**: FIFO
- **Content-based deduplication**: Enabled
- **Purpose**: preserve message order and avoid manually generating deduplication IDs

After creating the queue, save the queue URL. It will be used later as `SQS_QUEUE_URL`.

### 2. Launch the EC2 Instances
Launch **two Amazon Linux EC2 instances**.

Suggested names:
- `node_1`
- `node_2`

Recommended setup:
- **AMI**: Amazon Linux
- **Instance type**: a small instance is sufficient for this project
- **Public IP**: enabled
- **Key pair**: use the same key pair for both instances
- **Security group**: select the default security group, then update inbound rules

### 3. Update the Security Group
Edit the inbound rules for the security group used by both instances.

Required rule:
- **SSH**
  - Protocol: TCP
  - Port: `22`
  - Source: **My IP**

Outbound rules can remain at the default settings.

### 4. SSH Into Each EC2 Instance
Use **PuTTY** and the `.ppk` file for your key pair.

For Amazon Linux, log in as:
```bash
ec2-user
```

### 5. Install Required Software
Run these commands on both VMs:

```bash
whoami
sudo dnf update -y
sudo dnf install -y java-17-amazon-corretto-devel maven git awscli

java -version
mvn -version
aws --version
git --version
```

### 6. Configure AWS Credentials
Create the AWS configuration directory:

```bash
mkdir -p ~/.aws
```

Edit the credentials file:

```bash
vim ~/.aws/credentials
```

Add:

```ini
[default]
aws_access_key_id=YOUR_ACCESS_KEY_ID
aws_secret_access_key=YOUR_SECRET_ACCESS_KEY
aws_session_token=YOUR_SESSION_TOKEN
```

These values come from the AWS Details section in Learner Lab.

Edit the config file:

```bash
vim ~/.aws/config
```

Add:

```ini
[default]
region=us-east-1
output=json
```

Make sure the region matches where your EC2 instances and SQS queue were created.

### 7. Verify AWS Access
```bash
aws sts get-caller-identity
```

### 8. Set SQS Queue URL
Temporary:

```bash
export SQS_QUEUE_URL=YOUR_FIFO_QUEUE_URL
```

Persistent:

```bash
echo 'export SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/YOUR_ACCOUNT_ID/YOUR_QUEUE_NAME.fifo' >> ~/.bashrc
source ~/.bashrc
```

### 9. Clone Repository
Clone the project on both EC2 instances:

```bash
git clone https://github.com/cannon-j04v2/CS643.git
cd CS643
```

### 10. Build the Project
```bash
mvn clean package
```

### 11. Run Applications
On `node_2`:

```bash
java -cp target/project1-1.0-SNAPSHOT-all.jar com.njit.project1.appb.AppB
```

On `node_1`:

```bash
java -cp target/project1-1.0-SNAPSHOT-all.jar com.njit.project1.appa.AppA
```

### 12. Expected Runtime Behavior
- AppA checks images `1.jpg` through `10.jpg` from the public S3 bucket.
- If a car is detected with confidence greater than 80, AppA sends the image name to SQS.
- After all images are processed, AppA sends `-1` to the queue.
- AppB reads messages from SQS.
- If text is detected with confidence greater than 80, AppB writes the image name and text to `output.txt`.
- When AppB receives `-1`, it shuts down cleanly.
