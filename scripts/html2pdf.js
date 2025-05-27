#!/usr/bin/env node
const path = require('path');
const puppeteer = require('puppeteer');

(async () => {
  const [, , htmlPath, pdfPath] = process.argv;
  if (!htmlPath || !pdfPath) {
    console.error('❌  Użycie: node html2pdf.js input.html output.pdf');
    process.exit(1);
  }

  const browser = await puppeteer.launch({headless: 'new', args: ['--no-sandbox']});
  try {
    const page = await browser.newPage();

    // tryb „print” – identyczny zestaw CSS co w oknie drukowania
    await page.emulateMediaType('print');

    // ładowanie dokumentu i oczekiwanie na sieć + gotowe fonty
    await page.goto('file://' + path.resolve(htmlPath), {waitUntil: 'networkidle0'});
    await page.evaluateHandle('document.fonts.ready');

    // zapis do PDF-a; preferCSSPageSize – kluczowe, żeby nie skalować przez viewport
    await page.pdf({
      path: path.resolve(pdfPath),
      printBackground: true,
      preferCSSPageSize: true, // zostaw rozmiar stron drukarce
      margin: {top: 0, bottom: 0, left: 0, right: 0}, // zero dodatkowych marginesów
      scale: 1 // bez niepotrzebnego zmniejszania
    });
  } finally {
    await browser.close();
  }
})();
