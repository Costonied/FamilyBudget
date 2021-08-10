# Introduction
Family Budget App working on Vaadin

# Build
`mvn clean install`

# Environment variables
* DATABASE_URL 
  * PostgreSQL example `postgresql://localhost:5432/db_name`
* DATABASE_USERNAME
* DATABASE_PASSWORD

# Local start
* Spring Boot
* Main class `ru.savini.fb.FamilyBudgetApplication`
* VM options `-Dspring.profiles.active=local` OR `-Dspring.profiles.active=local`
* Setup environment variables

# Deploy on Heroku
[Full tutorial from Heroku](https://vaadin.com/learn/tutorials/cloud-deployment/heroku)
* Setup environment variables on Heroku
* Deploy: `heroku deploy:jar family-budget-1.0.jar --app APP_NAME`  
* Check that webserver (heroku dyno) is started OR start it
* See logs: `heroku logs --tail --app APP_NAME`  
* Open from command line: `heroku open --app APP_NAME`  
