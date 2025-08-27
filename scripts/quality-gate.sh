#!/bin/bash
set -e

COVERAGE_THRESHOLD=80
PMD_VIOLATIONS=0
CHECKSTYLE_VIOLATIONS=50

echo "Ejecutando Quality Gate..."

# Ejecutar todos los checks
mvn clean verify -Pquality-full

# Verificar cobertura
COVERAGE=$(grep -Po 'Total.*?([0-9]+)%' target/site/jacoco/index.html | tail -1 | grep -Po '[0-9]+')
if [ "$COVERAGE" -lt "$COVERAGE_THRESHOLD" ]; then
    echo "Cobertura insuficiente: $COVERAGE% < $COVERAGE_THRESHOLD%"
    exit 1
fi

echo "Quality Gate PASSED!"
echo "Cobertura: $COVERAGE%"