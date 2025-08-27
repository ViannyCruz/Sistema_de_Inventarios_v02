#!/bin/bash
echo "Ejecutando checks de calidad..."

echo "Ejecutando tests con cobertura..."
mvn clean test jacoco:report

echo "Ejecutando Checkstyle..."
mvn checkstyle:check

echo "Ejecutando PMD..."
mvn pmd:check

echo "Ejecutando SpotBugs..."
mvn spotbugs:check

echo "Ejecutando OWASP Dependency Check..."
mvn org.owasp:dependency-check-maven:check

echo "Quality checks completados!"
echo "Reportes disponibles en:"
echo "   - Cobertura: target/site/jacoco/index.html"
echo "   - Checkstyle: target/checkstyle-result.xml"
echo "   - PMD: target/pmd.html"
echo "   - SpotBugs: target/spotbugsXml.xml"
echo "   - OWASP: target/dependency-check-report.html"