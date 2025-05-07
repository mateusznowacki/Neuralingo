from docx import Document

doc = Document("file.docx")

for para in doc.paragraphs:
    print("Tekst:", para.text)
    print("Styl:", para.style.name)
    print("-----------")
