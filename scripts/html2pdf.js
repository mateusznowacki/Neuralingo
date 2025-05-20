const fs = require('fs');
const puppeteer = require('puppeteer');

(async () => {
    const html = fs.readFileSync('input.html', 'utf8');

    const browser = await puppeteer.launch({
        headless: "new",
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });

    const page = await browser.newPage();

    // Zastosuj skalowanie i pełne renderowanie
    await page.setViewport({
        width: 1280,             // szersze niż domyślna szerokość A4
        height: 1024,
        deviceScaleFactor: 2     // zwiększa jakość (DPI)
    });

    await page.setContent(html, {waitUntil: 'networkidle0'});
    await page.emulateMediaType('screen');

    // Zmierz faktyczną wysokość strony HTML
    const bodyHeight = await page.evaluate(() => {
        return document.body.scrollHeight;
    });

    // Wygeneruj PDF z rozmiarami odpowiadającymi faktycznej zawartości
    await page.pdf({
        path: 'output.pdf',
        width: '1280px',
        height: `${bodyHeight}px`,
        printBackground: true,
        scale: 1.25,  // lekko powiększa zawartość
        margin: {top: '0mm', bottom: '0mm', left: '0mm', right: '0mm'}
    });

    await browser.close();
})();
