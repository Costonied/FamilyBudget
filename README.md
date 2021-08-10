# Introduction
Family Budget App working on Vaadin

# Build
`mvn clean install`

# Environment variables
* DATABASE_URL 
  * PostgreSQL example `DATABASE_URL=postgresql://localhost:5432/db_name`

# Local start
* Spring Boot
* Main class `ru.savini.fb.FamilyBudgetApplication`
* Select DB type:
  * If you want start with own PostgreSQL DB then just setup DATABASE_URL in environment
  * If you want start with embedded H2 DB then just setup VM options `-Dspring.profiles.active=local`

# Heroku
## Deploy on Heroku
[Full tutorial from Heroku](https://vaadin.com/learn/tutorials/cloud-deployment/heroku)
* Setup PostgreSQL addon on Heroku and check DATABASE_URL environment variable
* Deploy: `heroku deploy:jar family-budget-1.0.jar --app APP_NAME`  
* Check that webserver (heroku dyno) is started OR start it
## Useful Heroku CLI commands
* See logs: `heroku logs --tail --app APP_NAME`  
* Open application from command line: `heroku open --app APP_NAME`  
