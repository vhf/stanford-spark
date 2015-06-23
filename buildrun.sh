#!/bin/bash
sbt assembly
spark-submit --master local target/scala-2.10/NLPAnnotator-assembly-1.0.jar --input ./input.txt --output ./output --properties tokenize,ssplit
