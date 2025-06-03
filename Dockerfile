# Etap 1: Budowanie aplikacji Spring Boot
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Etap 2: Obraz produkcyjny
FROM eclipse-temurin:21-jdk

WORKDIR /app

EXPOSE 8080

# --- Instalacja zależności systemowych ---
RUN apt-get update && apt-get install -y \
    curl \
    gnupg \
    ca-certificates \
    libfontconfig1 \
    libjpeg-dev \
    libpng-dev \
    libtiff-dev \
    libopenjp2-7 \
    xfonts-base \
    xfonts-75dpi \
    --no-install-recommends

# --- Instalacja Node.js LTS + Puppeteer dependencies ---
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g npm && \
    npm install -g puppeteer

# --- Instalacja pdf2htmlEX z pliku .deb ---
COPY lib/pdf2htmlEX.deb .
RUN dpkg -i pdf2htmlEX.deb || apt-get install -f -y

# --- Dodanie aplikacji + skryptów ---
COPY --from=builder /app/target/*.jar Neuralingo.jar
COPY scripts/package.json .
COPY scripts/package-lock.json .
COPY scripts/html2pdf.js .

# Instalacja lokalnych zależności Node.js
RUN npm install

# Ustawienie domyślnej komendy
CMD ["java", "-jar", "Neuralingo.jar"]
