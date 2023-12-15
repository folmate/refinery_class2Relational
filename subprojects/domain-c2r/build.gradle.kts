/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */

plugins {
	id("tools.refinery.gradle.java-library")
	id("tools.refinery.gradle.jmh")
}

dependencies {
	api(project(":refinery-store"))
	api(project(":refinery-store-query"))
	api(project(":refinery-store-query-interpreter"))
	api(project(":refinery-store-dse"))
	api(project(":refinery-language-semantics"))
	api(project(":refinery-generator"))
	implementation(libs.eclipseCollections.api)
	runtimeOnly(libs.eclipseCollections)
}

tasks{
	register<JavaExec>("runClass2Relational"){
		val mainRuntimeClasspath = sourceSets.main.map { it.runtimeClasspath }
		dependsOn(mainRuntimeClasspath)
		classpath(mainRuntimeClasspath)
		mainClass.set("c2r.refinery.C2RRefineryMain")
		standardInput = System.`in`
		group = "domain"
		description = "Execute model transformation from Class domain to Relational domain."
	}
}

