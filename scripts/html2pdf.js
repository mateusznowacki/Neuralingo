const fs = require('fs');
const puppeteer = require('puppeteer');

(async () => {
    const html = fs.readFileSync('input.html', 'utf8');

    const browser = await puppeteer.launch({headless: "new"});
    const page = await browser.newPage();

    await page.setContent(html, {waitUntil: 'load'});

    await page.pdf({
        path: 'output.pdf',
        format: 'A4',
        printBackground: true,
        margin: {
            top: '0mm',
            bottom: '0mm',
            left: '0mm',
            right: '0mm'
        }
    });

    await browser.close();
})();
