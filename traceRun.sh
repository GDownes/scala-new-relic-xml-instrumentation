#!/bin/bash

host="http://127.0.0.1:9090"

curl -v --location --request GET "${host}/xml/xml-sync-trace"
curl -v --location --request GET "${host}/xml/xml-sync-trace-nested"
curl -v --location --request GET "${host}/xml/xml-async-trace"
curl -v --location --request GET "${host}/xml/xml-async-trace-nested"
curl -v --location --request GET "${host}/xml/xml-async-trace-nested-for"

date
