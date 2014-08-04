#!/bin/bash
sbt compile
LD_LIBRARY_PATH=target/native sbt assembly
