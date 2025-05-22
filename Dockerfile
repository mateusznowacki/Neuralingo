FROM debian:bookworm

# Ustaw środowisko
ENV DEBIAN_FRONTEND=noninteractive

# Zainstaluj zależności systemowe, JDK, Maven, Node.js i Puppeteer dependencies
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    maven \
    curl \
    unzip \
    fontconfig \
    ca-certificates \
    gnupg \
    wget \
    xfonts-base \
    xfonts-75dpi \
    libx11-6 \
    libxcomposite1 \
    libxdamage1 \
    libxrandr2 \
    libgbm1 \
    libasound2 \
    libatk-bridge2.0-0 \
    libgtk-3-0 \
    libnss3 \
    libxss1 \
    libxshmfence1 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    && apt-get clean

# Zainstaluj Node.js LTS
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && apt-get install -y nodejs

# Ustaw katalog roboczy
WORKDIR /app

# Skopiuj projekt
COPY pom.xml .
COPY src ./src
COPY lib/pdf2htmlEX.deb ./pdf2htmlEX.deb
COPY scripts /app/scripts

# Zainstaluj pdf2htmlEX
RUN dpkg -i ./pdf2htmlEX.deb || apt-get install -f -y

# Zainstaluj puppeteer i inne zależności
WORKDIR /app/scripts
RUN npm install

# Buduj aplikację
WORKDIR /app
RUN mvn clean package -DskipTests

# Finalny ENTRYPOINT
CMD ["java", "-jar", "target/Neuralingo-1.jar"]
