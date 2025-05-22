const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');
const os = require('os');

(async () => {
    const [, , htmlPath, targetPdfPath] = process.argv;

    if (!htmlPath || !targetPdfPath) {
        console.error("❌ Usage: node html2pdf.js input.html output.pdf");
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

    // Wywołaj drukowanie
    await page.evaluate(() => window.print());

    // Czekaj aż plik się zapisze
    let downloadedFile = null;

    for (let i = 0; i < 20; i++) {
        const pdfFiles = fs.readdirSync(downloadsDir)
            .filter(f => f.endsWith('.pdf'))
            .map(f => ({
                name: f,
                fullPath: path.join(downloadsDir, f),
                time: fs.statSync(path.join(downloadsDir, f)).mtime.getTime()
            }))
            .sort((a, b) => b.time - a.time);

        if (pdfFiles.length > 0) {
            downloadedFile = pdfFiles[0].fullPath;
            break;
        }

        await new Promise(res => setTimeout(res, 500));
    }

    await browser.close();

    if (!downloadedFile || !fs.existsSync(downloadedFile)) {
        console.error("❌ PDF not found in Downloads.");
        process.exit(1);
    }

    // Skopiuj PDF do targetu
    fs.copyFileSync(downloadedFile, targetPdfPath);
    console.log(`✅ PDF copied to: ${targetPdfPath}`);
})();
