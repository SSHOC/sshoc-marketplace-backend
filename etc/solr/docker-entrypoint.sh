#!/bin/bash

SOLR_MARKETPLACE_ITEMS_DIR="/var/solr/data/marketplace-items"
SOLR_MARKETPLACE_CONCEPTS_DIR="/var/solr/data/marketplace-concepts"

if [ ! -d $SOLR_MARKETPLACE_ITEMS_DIR ]; then
	mkdir -p /var/solr/data
	cp -r /usr/solr/marketplace-items /var/solr/data
fi

if [ ! -d $SOLR_MARKETPLACE_CONCEPTS_DIR ]; then
	mkdir -p /var/solr/data
	cp -r /usr/solr/marketplace-concepts /var/solr/data
fi

# Execute parent entrypoint script
exec /opt/docker-solr/scripts/docker-entrypoint.sh "$@"
