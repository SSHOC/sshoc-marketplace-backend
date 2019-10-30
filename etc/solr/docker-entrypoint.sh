#!/bin/bash

SOLR_MARKETPLACE_ITEMS_DIR="/var/solr/data/marketplace-items"

if [ ! -d $SOLR_MARKETPLACE_ITEMS_DIR ]; then
	mkdir -p /var/solr/data
	cp -r /usr/solr/marketplace-items /var/solr/data
fi

# Execute parent entrypoint script
/opt/docker-solr/scripts/docker-entrypoint.sh "$@"

