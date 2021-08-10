# Introduction
Family Budget App working on Vaadin

# Build
`mvn clean install`

# Local start
* Spring Boot
* Main class `ru.savini.fb.FamilyBudgetApplication`
* VM options `-Dspring.profiles.active=local`

# Deploy on Heroku
[Full tutorial from Heroku](https://vaadin.com/learn/tutorials/cloud-deployment/heroku)
* Deploy: `heroku deploy:jar family-budget-1.0.jar --app APP_NAME`  
* See logs: `heroku logs --tail --app APP_NAME`  
* Open from command line: `heroku open --app APP_NAME`  
