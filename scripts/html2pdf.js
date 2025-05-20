const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');
const os = require('os');

(async () => {
    const [, , htmlPath, targetPdfPath] = process.argv;

    if (!htmlPath || !targetPdfPath) {
        console.error("❌ Usage: node html2pdf_gui.js input.html output.pdf");
        process.exit(1);
    }

    const absoluteHtmlPath = 'file://' + path.resolve(htmlPath);
    const downloadsDir = path.join(os.homedir(), 'Downloads');

    const browser = await puppeteer.launch({
        headless: 'new',
        args: [
            '--kiosk-printing',
            '--no-sandbox'
        ]
    });

    const page = await browser.newPage();
    await page.goto(absoluteHtmlPath, {waitUntil: 'networkidle0'});

    // Wywołanie drukowania
    await page.evaluate(() => window.print());

    // Szukaj najnowszego PDF w ~/Downloads
    let downloadedFile = null;
    for (let i = 0; i < 20; i++) {
        const files = fs.readdirSync(downloadsDir)
            .filter(f => f.endsWith('.pdf'))
            .map(f => ({
                name: f,
                time: fs.statSync(path.join(downloadsDir, f)).mtime.getTime()
            }))
            .sort((a, b) => b.time - a.time);

        if (files.length > 0) {
            downloadedFile = path.join(downloadsDir, files[0].name);
            break;
        }
        await new Promise(res => setTimeout(res, 300));
    }

    await browser.close();

    if (!downloadedFile || !fs.existsSync(downloadedFile)) {
        console.error("❌ PDF was not found in Downloads.");
        process.exit(1);
    }

    // Skopiuj do katalogu docelowego
    fs.copyFileSync(downloadedFile, targetPdfPath);
    fs.unlinkSync(downloadedFile); // Usuń z Downloads

})();
