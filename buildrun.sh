#!/bin/bash
sbt assembly
spark-submit target/scala-2.11/NLPAnnotator-assembly-1.0.jar
