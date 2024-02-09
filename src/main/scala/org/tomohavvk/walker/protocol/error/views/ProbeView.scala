package org.tomohavvk.walker.protocol.error.views

case class ProbeView(
  serviceName:    String,
  description:    String,
  serviceVersion: String,
  scalaVersion:   String,
  sbtVersion:     String)
