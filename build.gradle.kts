plugins {
  id("java-library")
  id("com.formkiq.gradle.graalvm-native-plugin")
}

nativeImage {
  imageVersion = "24.1.2"
  javaVersion = "23"
}

repositories {
  mavenCentral()
}

dependencies {
  api("com.fasterxml.jackson.core:jackson-annotations:2.19.0")
  api("com.fasterxml.jackson.core:jackson-core:2.19.0")
  api("com.fasterxml.jackson.core:jackson-databind:2.19.0")
  api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.19.0")

  api("com.google.guava:guava:33.0.0-jre")

  api(platform("io.opentelemetry:opentelemetry-bom:1.53.0"))
  api("io.opentelemetry:opentelemetry-api")
  api("io.opentelemetry:opentelemetry-sdk")
  api("io.opentelemetry:opentelemetry-exporter-otlp")
  api("io.opentelemetry:opentelemetry-semconv:1.30.1-alpha")
  api("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure")

  api("org.apache.commons:commons-collections4:4.5.0-M3")
  api("org.apache.commons:commons-csv:1.14.0")
  api("org.apache.commons:commons-math3:3.6.1")

  api("org.ejml:ejml-core:0.44.0")
  api("org.ejml:ejml-simple:0.44.0")

  api("org.jgrapht:jgrapht-core:1.5.2")
  api("org.jgrapht:jgrapht-io:1.5.2")

  api("org.moeaframework:moeaframework:5.0")
}

tasks.test {
  useJUnitPlatform()
}