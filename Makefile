.PHONY: startTestEnv stopTestEnv

startTestEnv:
	docker-compose up -d
	sleep 10
	@curl -O https://download.elastic.co/demos/kibana/gettingstarted/8.x/shakespeare.json
	@curl -X PUT "localhost:9200/shakespeare?pretty" -H 'Content-Type: application/json' -d'{"mappings": {"doc": {"properties": {"speaker": {"type": "keyword"},"play_name": {"type": "keyword"},"line_id": {"type": "integer"},"speech_number": {"type": "integer"}}}}}'> logs/import_mapping_response.json
	@curl -H 'Content-Type: application/x-ndjson' -XPOST 'localhost:9200/shakespeare/doc/_bulk?pretty' --data-binary @shakespeare.json > logs/import_response.json

stopTestEnv:
	docker-compose down
