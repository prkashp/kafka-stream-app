#!/bin/bash
java -Xms256M -Xmx768M -Dspring.profiles.active="$ENVIRONMENT" -jar /usr/local/lib/sophia_adv_in_store_enrichment-0.0.1-SNAPSHOT.jar