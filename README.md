# ilivalidator-web-service-lambda

- ENV Variablen müssen gesetzt sein: `-e AWS_ACCESS_KEY_ID=XXXXXXXXXXXXX -e AWS_SECRET_ACCESS_KEY=YYYYYYYYYYYY -e AWS_REGION=eu-central-1` 
- 


## todo
- Lambda in Gradle build integrieren.
- Lambda Qualifiers
- Lambda max / reserved concurrency
- ~~Ilivalidator~~
- ~~Ilivalidator extension functions (a al GRETL?)~~
- ci/cd
- ~~testing~~
- tests mit den Daten von ilivalidator-websocket. Dort dann nur noch, ob der web service als solches funktioniert.
- doku
  * policy, role...
  * ili_cache directory (env oder im Code setzen -> siehe Blog)
- AWS Parameter Store (v.a. für Spring Boot Teil)
- Cloudwatch logs aufräumen? kosten?
- Cloudformation?