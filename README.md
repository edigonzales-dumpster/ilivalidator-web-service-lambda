# ilivalidator-web-service-lambda

- ENV Variablen müssen gesetzt sein: `-e AWS_ACCESS_KEY_ID=XXXXXXXXXXXXX -e AWS_SECRET_ACCESS_KEY=YYYYYYYYYYYY -e AWS_REGION=eu-central-1` 
- 


## todo
- Lambda in Gradle build integrieren.
- Lambda Qualifiers
- ~~Lambda max / reserved concurrency~~
- ~~Ilivalidator~~
- ~~Ilivalidator extension functions (a al GRETL?)~~
- CI/CD Github Action
- Tests
  * ~~Grundlage~~
  * Testfälle von ilivalidator-webservice. In diesem nur noch einfachste Fälle.
- Dokumentation
  * policy, role...
  * ili_cache directory (env oder im Code setzen -> siehe Blog)
  * ili und toml
- AWS Parameter Store (v.a. für Spring Boot Teil)
- Cloudwatch logs aufräumen? kosten?
- Cloudformation
  * Parametrisieren
  * Aliasnamen (Zuerst das System verstehen und wie Deployment funktioniert).
  * ...
- Welcher User? Welche Policies?


```
aws cloudformation create-stack --stack-name hello-lambda-stack \
  --template-body file://ilivalidator_stack.yml \
  --capabilities CAPABILITY_NAMED_IAM
```

```
aws cloudformation delete-stack --stack-name hello-lambda-stack
```