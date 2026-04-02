#!/bin/bash
set -e

# Lê credenciais de .nexus-credentials (não versionado)
CREDENTIALS_FILE=".nexus-credentials"
if [ ! -f "$CREDENTIALS_FILE" ]; then
  echo "Arquivo $CREDENTIALS_FILE não encontrado."
  echo "Crie o arquivo com base em .nexus-credentials.example"
  exit 1
fi
# Lê credenciais removendo \r (Windows line endings)
while IFS='=' read -r key value; do
  key=$(echo "$key" | tr -d '\r')
  value=$(echo "$value" | tr -d '\r')
  [[ -z "$key" || "$key" == \#* ]] && continue
  export "$key=$value"
done < "$CREDENTIALS_FILE"

# Valida variáveis obrigatórias
: "${NEXUS_URL:?NEXUS_URL não definido em $CREDENTIALS_FILE}"
: "${NEXUS_REPO:?NEXUS_REPO não definido em $CREDENTIALS_FILE}"
: "${NEXUS_USER:?NEXUS_USER não definido em $CREDENTIALS_FILE}"
: "${NEXUS_PASS:?NEXUS_PASS não definido em $CREDENTIALS_FILE}"

# Lê versão do pom.xml
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
GROUP_ID="io.github.moraesdelima"
ARTIFACT_ID="template-engine"
JAR="target/${ARTIFACT_ID}-${VERSION}.jar"
POM="pom.xml"

echo "Buildando ${ARTIFACT_ID}-${VERSION}..."
mvn package -DskipTests

if [ ! -f "$JAR" ]; then
  echo "JAR não encontrado: $JAR"
  exit 1
fi

echo "Publicando ${ARTIFACT_ID}-${VERSION} em ${NEXUS_URL} (repo: ${NEXUS_REPO})..."

HTTP_STATUS=$(curl -s -o /tmp/nexus-response.txt -w "%{http_code}" \
  -u "${NEXUS_USER}:${NEXUS_PASS}" \
  -X POST "${NEXUS_URL}/service/rest/v1/components?repository=${NEXUS_REPO}" \
  -F "maven2.groupId=${GROUP_ID}" \
  -F "maven2.artifactId=${ARTIFACT_ID}" \
  -F "maven2.version=${VERSION}" \
  -F "maven2.asset1=@${JAR};type=application/java-archive" \
  -F "maven2.asset1.extension=jar" \
  -F "maven2.asset2=@${POM};type=application/xml" \
  -F "maven2.asset2.extension=pom")

if [ "$HTTP_STATUS" -eq 204 ]; then
  echo "Deploy realizado com sucesso!"
else
  echo "Falha no deploy. HTTP status: $HTTP_STATUS"
  cat /tmp/nexus-response.txt
  exit 1
fi
