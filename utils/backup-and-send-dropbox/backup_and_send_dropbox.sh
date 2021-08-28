pg_dump -Fc --no-acl --no-owner -h ${PG_HOST} -U ${PG_USER} ${PG_DB} > family_budget_db_backup_`date +"%s"`.dump && \
curl --location --request POST 'https://content.dropboxapi.com/2/files/upload' \
--header 'Dropbox-API-Arg: {"path":"/backups/'`ls`'","mode": "add","autorename":true,"mute":true,"strict_conflict":false}' \
--header 'Authorization: Bearer '${DROPBOX_APP_TOKEN} \
--header 'Content-Type: application/octet-stream' \
--data-binary @`ls` && \
rm `ls`