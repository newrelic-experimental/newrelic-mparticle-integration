[![New Relic Experimental header](https://github.com/newrelic/opensource-website/raw/master/src/images/categories/Experimental.png)](https://opensource.newrelic.com/oss-category/#new-relic-experimental)

# newrelic-mparticle-integration

[AWS Lambda outbound mParticle integration to New Relic.](https://docs.mparticle.com/developers/partners/outbound-integrations/#aws-lambda-integrations)

##  Architecture
mParticle Firehose Listener -> LogStreamHandler (Lambda) -> SQS FIFO Queue -> MessageProcessor (SQS Listener) -> Insights (New Relic)

NOTE:
- The code must run in an AWS Region that supports SQS FIFO queues.

## Installation

### FIFO Queue
Create an SQS FIFO queue

### LogStreamHandler (Lambda)
1. Create a new Java Lambda
#### Configuration
1. Upload mParticleIntegration.jar
1. Environment variables

   | Key | Value |
   | :--- | :-----: |
   | EUInsightsEndpoint | insights-collector.eu01.nr-data.net |
   | EventBucketName | <Your_bucket_name> |
   | EventBucketPrefix | <Your_bucket_prefix> |
   | EventBucketRegion | <AWS_Region_Lambda_is_running_in> |
   | FifoQueue | <Your_FIFO_Queue_Name>.fifo |
   | LogLevel | [<java.util.logging.Level>](https://docs.oracle.com/javase/7/docs/api/java/util/logging/Level.html) |
   | SaveMessages | <true_or_false> |
   | USInsightsEndpoint | insights-collector.newrelic.com |
1. Tags
   - none  required, as desired
1. Basic settings

   |      |      |
   | :--- | :--- |
   | Description | - |
   | Runtime | <Java_runtime_version>
   | HandlerInfo | com.nr.logging.mparticle.LogStreamHandler::handleRequest |
   | Memory (MB) | 512 |
   | Timeout | 3min0sec |

1. Monitoring tools
   - none required, as desired
   
1. VPC
   - none required, as needed
   
1. File system
   - none required, as needed
   
1. Concurrency
   - Provisioned concurrency: 5 is a good starting point

1. Asynchronous invocation

   |      |      |
   | :--- | :--- |
   | Maximum age of event | 6h0min0sec |
   | Retry attempts | 2 |
   | Dead-letter queue service | none |
  
#### Permissions
1. Execution role

   At a minimum:
   - AmazonSQSFullAccess
   - AWSLambdaFullAccess
   - AmazonS3FullAccess 
   
1. [Grant mParticle permission on the Lambda](https://docs.mparticle.com/developers/partners/firehose/#step-1-aws-lambda---grant-permissions)

### MessageProcessor (SQS Listener Lambda)
1. Create a new AWS Java Lambda

#### Configuration
1. Upload mParticleIntegration.jar
1. Designer
   - Add this Lambda as a trigger to the FIFO Queue created earlier
1. Environment variables

   | Key | Value |
   | :--- | :-----: |
   | EUInsightsEndpoint | insights-collector.eu01.nr-data.net |
   | EventBucketName | <Your_bucket_name> |
   | EventBucketPrefix | <Your_bucket_prefix> |
   | EventBucketRegion | <AWS_Region_Lambda_is_running_in> |
   | FifoQueue | <Your_FIFO_Queue_Name>.fifo |
   | LogLevel | [<java.util.logging.Level>](https://docs.oracle.com/javase/7/docs/api/java/util/logging/Level.html) |
   | SaveMessages | <true_or_false> |
   | USInsightsEndpoint | insights-collector.newrelic.com |
1. Tags
   - none required, as desired
1. Basic settings

   |      |      |
   | :--- | :--- |
   | Description | - |
   | Runtime | <Java_runtime_version>
   | HandlerInfo | com.nr.logging.mparticle.MessageProcessor::handleRequest |
   | Memory (MB) | 512 |
   | Timeout | 3min0sec |

1. Monitoring tools
   - none required, as desired
   
1. VPC
   - none required, as needed
   
1. File system
   - none required
   
1. Concurrency
   - none required, as needed

1. Asynchronous invocation

   |      |      |
   | :--- | :--- |
   | Maximum age of event | 6h0min0sec |
   | Retry attempts | 2 |
   | Dead-letter queue service | none |
  
#### Permissions
1. Execution role

   At a minimum:
   - AmazonSQSFullAccess
   - AWSLambdaFullAccess
   - AmazonS3FullAccess 


### [mParticle Deployment](https://docs.mparticle.com/developers/partners/firehose/#deployment)

## Getting Started
### mParticle configuration (via the mParticle UI)
- accountRegion
- insightsInsertKey
- rpmId
- appId

## Building
To generate the 3rd party notices file use  `gradle downloadLicenses` and use one of the resulting files in `build/reports/license/`
## Support

New Relic has open-sourced this project. This project is provided AS-IS WITHOUT WARRANTY OR DEDICATED SUPPORT. Issues and contributions should be reported to the project here on GitHub.

We encourage you to bring your experiences and questions to the [Explorers Hub](https://discuss.newrelic.com) where our community members collaborate on solutions and new ideas.

## Contributing

We encourage your contributions to improve Salesforce Commerce Cloud for New Relic Browser! Keep in mind when you submit your pull request, you'll need to sign the CLA via the click-through using CLA-Assistant. You only have to sign the CLA one time per project. If you have any questions, or to execute our corporate CLA, required if your contribution is on behalf of a company, please drop us an email at opensource@newrelic.com.

**A note about vulnerabilities**

As noted in our [security policy](../../security/policy), New Relic is committed to the privacy and security of our customers and their data. We believe that providing coordinated disclosure by security researchers and engaging with the security community are important means to achieve our security goals.

If you believe you have found a security vulnerability in this project or any of New Relic's products or websites, we welcome and greatly appreciate you reporting it to New Relic through [HackerOne](https://hackerone.com/newrelic).

## License

`newrelic-mparticle-integration` is licensed under the [Apache 2.0](http://apache.org/licenses/LICENSE-2.0.txt) License.

`newrelic-mparticle-integration` also uses source code from third-party libraries. You can find full details on which libraries are used and the terms under which they are licensed in the [third party notices](./THIRD_PARTY_NOTICES.html) document.
