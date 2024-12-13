### AWS Lambda 

## Testing the lambda function in Docker using LocalStack
**[Baeldung: Testing Lambda Function Locally Using LocalStack](https://www.baeldung.com/java-aws-lambda#testing-lambda-function-locally-using-localstack)**

1. From the root of this project, start a LocalStack container using Docker:
```
docker run \
    --rm -it \
    -p 127.0.0.1:4566:4566 \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v ./target:/opt/code/localstack/target \
    localstack/localstack
```

2. Exec into container 
```
docker exec -it localstack /bin/sh
```

3. In the container's shell, create our lambda function:
```
awslocal lambda create-function \
    --function-name spotify-function \
    --runtime java17 \
    --handler com.app.lambda.LambdaHandler\
    --role arn:aws:iam::000000000000:role/lambda-role \
    --zip-file fileb:///opt/code/localstack/target/spotify-playlist-updater-1.0.0-SNAPSHOT.jar
```

4. With the function created, it can now be invoked:
```
awslocal lambda invoke \
    --function-name spotify-function \
    --payload '{ "clientId": "<client-id>", \ 
                 "clientSecret": "<client-secret>" \
                 "refreshToken": "<refresh-token>" \
                 "activePlaylistId": "<active-playlist-id>" \
                 "archivePlaylistId": "<archive-playlist-id>"
               }' output.txt
```
