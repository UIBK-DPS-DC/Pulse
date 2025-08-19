# Pulse Scheduler

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE.md)
![Issues](https://img.shields.io/github/issues/UIBK-DPS-DC/Pulse)
[![Version](https://img.shields.io/badge/Version-1.0-green)]()

Pulse is a research prototype scheduler for multi-objective scheduling of service-based 
applications across multi-cluster Cloud–Edge–IoT infrastructures. It is based on the paper:

> *Pulse: Multi-objective Scheduling for Service-Based Applications in Multi-Cluster Cloud-Edge-IoT
> Infrastructures*  
> (see citation below)

## Overview

The scheduler models scheduling as two interconnected optimization problems:

- Local assignment problem—assigning services to resources within a cluster.
- Global composition problem—composing assignments across multiple clusters.

To explore trade-offs between objectives, Pulse leverages selectors to choose solutions from
Pareto fronts.

For multi-objective optimization, Pulse integrates with the 
[MOEA Framework](https://github.com/MOEAFramework/MOEAFramework), a Java library for evolutionary
multi-objective optimization.

## Usage

Pulse is implemented as a Java-based Gradle project. It can be used directly as a library within
other Java projects. To build and install the library locally:

```bash
./gradlew clean build
./gradlew publishToMavenLocal
```

After publishing, you can include Pulse in your own Java project via your build.gradle:

```kotlin
dependencies {
    implementation 'org.pulse:scheduler:0.1.0'
}
```

The paper describes a Kubernetes-based implementation of Pulse, realized through a custom 
orchestration system for Kubernetes. This repository does not contain that implementation.
Instead, Pulse here is provided as an orchestration-system-independent description of the 
multi-cluster scheduling architecture and its optimization components.

For questions related to the Kubernetes-based implementation, please contact the authors of the paper.

## License

This project is licensed under the GPLv3—see the [LICENSE](LICENSE) file for details.

## Citation

If you use Pulse in your research, please cite: