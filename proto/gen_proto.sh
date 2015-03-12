#!/bin/bash
protoc --java_out=./generated report.proto 
protoc --java_out=./generated image.proto 
