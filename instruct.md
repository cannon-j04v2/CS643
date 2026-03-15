Create a complete Java Maven project for an AWS class assignment. Build the full repository with source code, Maven config, helper classes, run instructions, and a README. Keep it simple, readable, and assignment-focused.

Project summary:
- There are two EC2 machines:
  - node_1 runs AppA
  - node_2 runs AppB
- Public S3 bucket: cs643-njit-project1
- Images are named 1.jpg through 10.jpg
- AppA:
  - iterates over 1.jpg to 10.jpg
  - uses Amazon Rekognition DetectLabels
  - determines whether a car is present by checking for a label named "Car" with confidence > 80
  - if true, sends the image key (example: 3.jpg) to SQS
  - after processing all 10 images, sends a termination message: -1
- AppB:
  - continuously polls SQS using long polling
  - if the received message body is -1, it exits cleanly
  - otherwise it downloads the referenced image from S3
  - uses Amazon Rekognition DetectText
  - keeps only text detections with confidence > 80
  - if any qualifying text is found, appends a line to output.txt containing the image key and the detected text
- The system must work even if AppA starts before AppB or AppB starts before AppA

Important architecture requirements:
- Use Java 17
- Use Maven
- Use AWS SDK for Java v2
- Use one shared codebase with reusable helper classes
- Do NOT hardcode AWS credentials anywhere in Java source
- Use the AWS default credentials provider chain so credentials come from ~/.aws/credentials on EC2
- Read region and queue URL from environment variables, with a simple config helper
- Bucket name may be hardcoded as cs643-njit-project1 because it is part of the assignment
- Use SQS FIFO queue semantics because message order matters for the -1 termination message
- Assume the queue already exists and AppA/AppB will receive the queue URL through an environment variable
- For FIFO queue sends, use a fixed message group id such as "project1"
- Prefer content-based deduplication on the queue, but do not assume queue creation is done in code
- Use long polling for receives
- Use clear console logging throughout both apps
- Keep the code easy for a student to explain in a demo video

Repository structure to generate:
project1/
  pom.xml
  README.md
  .gitignore
  src/main/java/com/njit/project1/common/
    AwsClients.java
    Config.java
    S3Helper.java
    SqsHelper.java
    RekognitionHelper.java
    FileHelper.java
  src/main/java/com/njit/project1/appa/
    AppA.java
  src/main/java/com/njit/project1/appb/
    AppB.java

Implementation details:
1. pom.xml
- Add dependencies for AWS SDK v2 modules:
  - s3
  - sqs
  - rekognition
- Add compiler settings for Java 17
- Add a Maven exec configuration or keep it simple and document java -cp / mvn exec usage in README

2. Config.java
- Provide methods to read:
  - AWS_REGION from environment
  - SQS_QUEUE_URL from environment
- Hardcode:
  - bucket name = cs643-njit-project1
  - image keys = 1.jpg through 10.jpg generated in code
- Validate required env vars and throw a clean error if missing

3. AwsClients.java
- Create and return S3Client, SqsClient, and RekognitionClient
- Use Region from Config
- Use DefaultCredentialsProvider
- Do not use static credentials in code

4. S3Helper.java
- Method to download an object from S3 into bytes
- Method signature should be simple and reusable

5. SqsHelper.java
- Method to send a normal image-key message to FIFO SQS
- Method to send the termination message -1
- Include required FIFO send parameters:
  - message group id = "project1"
- Method to receive messages with long polling
- Method to delete a processed message
- Keep receive batch size small and logic easy to follow

6. RekognitionHelper.java
- Method detectCarInImage(byte[] imageBytes): boolean
  - use DetectLabels
  - return true if label name equals "Car" and confidence > 80
- Method detectHighConfidenceText(byte[] imageBytes): List<String>
  - use DetectText
  - include only detections where detected text confidence > 80
  - filter out blanks
  - deduplicate repeated strings while preserving order if easy to do
- Keep parsing logic straightforward and readable

7. FileHelper.java
- Append output lines to output.txt
- Ensure the file is created if missing

8. AppA.java
- Main method
- Create clients/helpers
- Loop from 1 to 10, building image key as i + ".jpg"
- Download image from S3
- Check for car
- If car is found, send image key to SQS
- Log each major action
- After loop ends, send -1
- Print a final completion message

9. AppB.java
- Main method
- Create clients/helpers
- Start an infinite polling loop
- Receive messages from SQS
- If none, continue polling
- For each message:
  - if body equals -1:
    - log shutdown
    - delete the message
    - exit the loop
  - else:
    - treat body as image key
    - download image bytes from S3
    - detect text
    - if text list is not empty:
      - append a line to output.txt
    - delete the SQS message after successful processing
- Use readable output format in output.txt, for example:
  3.jpg : ABC123, PARKING, LOT 7
- Keep behavior simple and deterministic

10. README.md
The README must be high quality and written for a student running this on two EC2 instances.
Include:
- project overview
- architecture summary
- prerequisites
- how to build
- how to run AppA on node_1
- how to run AppB on node_2
- example environment variable exports
- explanation of AWS credentials location
- notes about FIFO queue requirement
- notes about output.txt
- brief troubleshooting section

Very important README placeholder rules:
- Use numbered placeholders in the exact form <FMI_1>, <FMI_2>, <FMI_3>, etc.
- Do NOT replace these placeholders with guessed values
- Do NOT invent fake account values
- Preserve the placeholders exactly
- Use them only where the user must fill in their own machine/account-specific information

Use the following placeholder meanings in README:
- <FMI_1> = your AWS access key id
- <FMI_2> = your AWS secret access key
- <FMI_3> = your AWS session token
- <FMI_4> = your AWS region
- <FMI_5> = your SQS queue URL

README must include these exact examples:

Example ~/.aws/credentials:
[default]
aws_access_key_id=<FMI_1>
aws_secret_access_key=<FMI_2>
aws_session_token=<FMI_3>

Example ~/.aws/config:
[default]
region=<FMI_4>
output=json

Example environment variables before running either app:
export AWS_REGION=<FMI_4>
export SQS_QUEUE_URL=<FMI_5>

README run examples should be concrete, such as:
mvn clean package
export AWS_REGION=<FMI_4>
export SQS_QUEUE_URL=<FMI_5>
java -cp target/project1-1.0-SNAPSHOT.jar com.njit.project1.appb.AppB
java -cp target/project1-1.0-SNAPSHOT.jar com.njit.project1.appa.AppA

Additional requirements:
- Add .gitignore for target/, .idea/, *.class, output.txt, and OS/editor junk
- Keep package names consistent
- Avoid unnecessary frameworks
- No Spring Boot
- No Docker
- No tests required unless very lightweight and helpful
- Make sure imports compile cleanly
- Keep exception handling practical and not overcomplicated
- Prefer clear code over clever code

Acceptance criteria:
- Project compiles with Maven
- AppA and AppB are separate runnable main classes
- Code uses AWS SDK v2 correctly
- No hardcoded credentials in source code
- README contains the exact numbered <FMI_#> placeholders listed above
- The repository looks ready for a student to clone onto two EC2 nodes and run

When done, output the full repository contents file by file.