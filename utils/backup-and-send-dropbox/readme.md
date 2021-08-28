# Script info

## Main steps
1. Get DB dump using `pg_dump`
2. Send dump to Dropbox using `curl` and Dropbox API
3. Delete local dump file using `rm`

## Script implementation notes
* use `date +"%s"` to get timestamp and add it to dump filename
* use `ls` to get dump file name and add it to `curl` and `rm` commands

## Environment variables
* `PG_DB` Postgres database name
* `PG_HOST` Postgres host
* `PG_USER` Postgres user
* `PGPASSWORD` password to Postgres
* `DROPBOX_APP_TOKEN` Dropbox app token

# Additional behaviour
## IFTTT applet
* **Trigger**: new file added into Dropbox subdirectory
* **Action**: send email