#!/bin/bash

host="http://127.0.0.1:9090"
curl -v --location --request GET "${host}/annotation/sync-trace"
curl -v --location --request GET "${host}/annotation/sync-trace-nested" 
curl -v --location --request GET "${host}/annotation/async-trace" 
curl -v --location --request GET "${host}/annotation/async-trace-nested" 
curl -v --location --request GET "${host}/annotation/async-trace-nested-for" 

curl -v --location --request GET "${host}/dsl/async-trace" 
curl -v --location --request GET "${host}/dsl/async-trace-nested" 
curl -v --location --request GET "${host}/dsl/async-trace-nested-for" 
curl -v --location --request GET "${host}/dsl/async-trace-nested-chain" 

date
