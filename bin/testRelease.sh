#!/usr/bin/env bash
set -eux

VERSION=$1
coursier fetch \
  com.lihaoyi:sourcecode_2.10:$VERSION \
  com.lihaoyi:sourcecode_2.11:$VERSION \
  com.lihaoyi:sourcecode_2.12:$VERSION \
  com.lihaoyi:sourcecode_2.13.0-RC1:$VERSION \
  com.lihaoyi:sourcecode_2.13.0-RC1:$VERSION \
  com.lihaoyi:sourcecode_native0.3_2.11:$VERSION \
  com.lihaoyi:sourcecode_sjs0.6_2.10:$VERSION \
  com.lihaoyi:sourcecode_sjs0.6_2.11:$VERSION \
  com.lihaoyi:sourcecode_sjs0.6_2.12:$VERSION \
  com.lihaoyi:sourcecode_sjs0.6_2.13.0-RC1:$VERSION \
  com.lihaoyi:sourcecode_sjs0.6_2.13.0-RC1:$VERSION \
  -r sonatype:public
